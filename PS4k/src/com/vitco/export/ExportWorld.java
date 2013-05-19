package com.vitco.export;

import com.vitco.engine.data.container.Voxel;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import org.jaitools.imageutils.ImageUtils;
import org.jaitools.media.jai.vectorize.VectorizeDescriptor;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.PolygonPoint;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.util.*;

/**
 * Helps with managing the data structure, so we can easily extract the information we
 * need to generate the export file.
 */
public class ExportWorld {

    // list of all voxels
    private final ArrayList<VoxelSide> voxels = new ArrayList<VoxelSide>();

    // voxel index to determine which sides are visible
    private final HashMap<String, VoxelSide> index = new HashMap<String, VoxelSide>();

    // helper class that represents a voxel
    private final class VoxelSide {
        private final int[] pos;
        private final Color color;
        private final boolean[] visSides = new boolean[]{true, true, true, true, true, true};

        private void hideSide(int side) {
            visSides[side] = false;
        }

        // check if side is visible
        public final boolean sideVisible(int side) {
            return visSides[side];
        }

        // get position for axis
        public final int posForAxis(int axis) {
            return pos[axis];
        }

        // constructor
        public VoxelSide(Voxel voxel) {
            this.pos = voxel.getPosAsInt();
            this.color = voxel.getColor();
            // update all the sides
            for (int i = 0; i < 6; i++) {
                int add = i%2 == 0 ? 1 : -1;
                VoxelSide found = index.get(
                        (i/2 == 0 ? voxel.x + add : voxel.x) + "_" +
                                (i/2 == 1 ? voxel.y + add : voxel.y) + "_" +
                                (i/2 == 2 ? voxel.z + add : voxel.z)
                );
                if (found != null) {
                    hideSide(i);
                    found.hideSide(i%2 == 0 ? i + 1 : i - 1);
                }
            }

            index.put(voxel.getPosAsString(), this);
        }
    }

    // constructor
    public ExportWorld(Voxel[] input) {
        for (Voxel voxel : input) {
            voxels.add(new VoxelSide(voxel));
        }
    }

    // helper - converts "black and white" image into vector representation
    private static Collection<com.vividsolutions.jts.geom.Polygon> doVectorize(RenderedImage src) {
        ParameterBlockJAI pb = new ParameterBlockJAI("Vectorize");
        pb.setSource("source0", src);

        pb.setParameter("outsideValues", Collections.singleton(0));

        // Get the desintation image: this is the unmodified source image data
        // plus a property for the generated vectors
        RenderedOp dest = JAI.create("Vectorize", pb);

        // Get the vectors
        Object property = dest.getProperty(VectorizeDescriptor.VECTOR_PROPERTY_NAME);
        ArrayList<com.vividsolutions.jts.geom.Polygon> result = new ArrayList<Polygon>();
        for (Object polygon : (Collection)property) {
            result.add((com.vividsolutions.jts.geom.Polygon)polygon);
        }
        return result;
    }

    public final int analyzeSlice(Polygon poly) {
        // Note: this is needed to avoid hole detection for image-> polygon when
        // the polygon "touches" at the corners of the border (and this would
        // cause a crash for the triangle reduction algorithm)
        Geometry inside = poly.buffer(-0.1f, 0);
        Coordinate[] coords = inside.getCoordinates();

        // holds the points that were already seen, so we know
        // where a polygon (full or hole) starts
        HashSet<String> seenPoints = new HashSet<String>();
        ArrayList<PolygonPoint> points = new ArrayList<PolygonPoint>();
        org.poly2tri.geometry.polygon.Polygon polyR = null;

        for (Coordinate coord : coords) {
            String key = coord.x + ", " + coord.y;
            if (seenPoints.contains(key)) {
                // a loop just finished
                PolygonPoint[] pointArray = new PolygonPoint[points.size()];
                points.toArray(pointArray);
                if (polyR == null) { // this is the polygon itself
                    polyR = new org.poly2tri.geometry.polygon.Polygon(pointArray);
                } else { // this is a hole
                    polyR.addHole(new org.poly2tri.geometry.polygon.Polygon(pointArray));
                }
                points.clear();
            } else {
                seenPoints.add(key);
                points.add(new PolygonPoint(coord.x, coord.y));
            }
        }
        // triangulate the polygon
        Poly2Tri.triangulate(polyR);

        return polyR == null ? 0 : polyR.getTriangles().size();
    }

    // build the sides and returns the total number of triangles
    public int buildSides() {
        int triCount = 0;
        // for all sides
        for (int i = 0; i < 6; i++) {
            // holds the sides per "slice"
            HashMap<Integer, ArrayList<Point>> polygons = new HashMap<Integer, ArrayList<Point>>();
            // min,max for other axix
            Integer min2 = null;
            Integer min3 = null;
            Integer max2 = null;
            Integer max3 = null;
            // for all voxels
            for (VoxelSide voxel : voxels) {
                if (voxel.sideVisible(i)) {
                    int depthAxis = i/2;
                    ArrayList<Point> pixels = polygons.get(voxel.posForAxis(depthAxis));
                    if (pixels == null) {
                        pixels = new ArrayList<Point>();
                        polygons.put(voxel.posForAxis(depthAxis), pixels);
                    }
                    // add the other two axis as point
                    Point pixel = new Point(voxel.posForAxis((depthAxis + 1)% 3), voxel.posForAxis((depthAxis + 2)% 3));
                    min2 = Math.min(min2 == null ? pixel.x : min2, pixel.x);
                    min3 = Math.min(min3 == null ? pixel.y : min3, pixel.y);
                    max2 = Math.max(max2 == null ? pixel.x : max2, pixel.x);
                    max3 = Math.max(max3 == null ? pixel.y : max3, pixel.y);
                    pixels.add(pixel);
                }
            }
            if (min2 != null && max2 != null && min3 != null && max3 != null) {
                // analyze the polygons
                for (Map.Entry<Integer, ArrayList<Point>> entry : polygons.entrySet()) {
                    TiledImage src = ImageUtils.createConstantImage(max2 - min2 + 1, max3 - min3 + 1, 0);
                    for (Point point : entry.getValue()) {
                        src.setSample(point.x - min2, point.y - min3, 0, 1);
                    }
                    for (com.vividsolutions.jts.geom.Polygon poly : doVectorize(src)) {
                        triCount += analyzeSlice(poly);
                    }
                }
            }
        }

        return triCount;
    }
}
