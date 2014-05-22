package com.vitco.export.generic;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.export.generic.container.*;
import com.vitco.low.hull.HullManager;
import com.vitco.low.triangulate.Grid2TriGreedyOptimal;
import com.vitco.low.triangulate.Grid2TriNaive;
import com.vitco.low.triangulate.Grid2TriPolyFast;
import com.vitco.low.triangulate.util.Grid2PolyHelper;
import com.vitco.util.components.progressbar.ProgressDialog;
import com.vitco.util.components.progressbar.ProgressReporter;
import gnu.trove.list.array.TShortArrayList;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the voxel data and manages restructuring, preparing it for exporting.
 */
public class ExportDataManager extends ProgressReporter {

    // contains the current hull that we need for triangulation
    private final HullManager<Voxel> hullManager;

    // used to access voxel data (for color generation)
    private final Data data;

    // true if texture padding is enabled
    private final boolean usePadding;

    // -------------
    // Data Structure that manages the triangles
    private final TexTriangleManager triangleManager = new TexTriangleManager();

    // getter for triangle manager
    public final TexTriangleManager getTriangleManager() {
        return triangleManager;
    }

    // Data Structure that manages the textures
    private final TriTextureManager textureManager = new TriTextureManager(getProgressDialog());

    // getter for the texture manager
    public final TriTextureManager getTextureManager() {
        return textureManager;
    }

    // -------------

    // the algorithms static variables
    public static final int POLY2TRI_ALGORITHM = 0;
    public static final int MINIMAL_RECT_ALGORITHM = 1;
    public static final int NAIVE_ALGORITHM = 2;

    // constructor
    public ExportDataManager(ProgressDialog dialog, Data data, boolean usePadding, int algorithm) {
        super(dialog);

        // create hull manager that exposes hull information
        setActivity("Computing Hull...", true);
        Voxel[] voxels = data.getVisibleLayerVoxel();
        HullManager<Voxel> hullManager = new HullManager<Voxel>();
        for (Voxel voxel : voxels) {
            hullManager.update(voxel.posId, voxel);
        }

        // store references
        this.hullManager = hullManager;
        this.data = data;
        this.usePadding = usePadding;

        // extract information
        extract(algorithm);

        // combine the textures
        textureManager.combine();

        // validate uv mappings
        setActivity("Validating UV Mappings...", true);
        textureManager.validateUVMappings();
    }

    // make sure that the polygon has no 3D t-junction problems
    private short[][][] fix3DTJunctionProblems(short[][][] polys, int planeAbove, int id1, int id2, int minA, int minB) {
        // result array
        short[][][] result = new short[polys.length][][];
        // temporary arrays to do comparisons
        short[] pos1 = new short[] {(short) planeAbove, (short) planeAbove, (short) planeAbove};
        short[] pos2 = new short[] {(short) planeAbove, (short) planeAbove, (short) planeAbove};
        // loop over all polygons
        for (int i1 = 0; i1 < polys.length; i1++) {
            // create corresponding result part
            short[][] poly = polys[i1];
            result[i1] = new short[poly.length][];
            // loop over outlines (poly + holes)
            for (int i2 = 0; i2 < poly.length; i2++) {
                short[] outline = poly[i2];
                // create dynamic list that we can later convert to result
                TShortArrayList list = new TShortArrayList(outline.length + 2);
                // loop over all points
                for (int i = 0, len = outline.length - 2; i < len; i += 2) {
                    // add current point
                    list.add(outline[i]);
                    list.add(outline[i+1]);
                    // check the type of line segment
                    if (outline[i + 2] == outline[i]) { // x values are equal
                        // compute the move direction
                        int step = (outline[i + 3] > outline[i + 1]) ? 1 : -1;
                        // move over all whole "in between" steps between this and the next point
                        for (short y = (short) (outline[i + 1] + step); y != outline[i + 3]; y += step) {
                            short x = (short) (outline[i] + (step == 1 ? -1 : 0) + minA);
                            pos1[id1] = x;
                            pos1[id2] = (short) (y + minB);
                            pos2[id1] = x;
                            pos2[id2] = (short) (y-1 + minB);
                            if (hullManager.contains(pos1) != hullManager.contains(pos2)) {
                                // the "in between" point needs to be used for triangle generation
                                list.add(outline[i]);
                                list.add(y);
                            }
                        }
                    } else { // y values are equal
                        // compute the move direction
                        int step = (outline[i + 2] > outline[i]) ? 1 : -1;
                        // move over all whole "in between" steps between this and the next point
                        for (short x = (short) (outline[i] + step); x != outline[i + 2]; x += step) {
                            short y = (short) (outline[i + 1] + (step == -1 ? -1 : 0) + minB);
                            pos1[id1] = (short) (x + minA);
                            pos1[id2] = y;
                            pos2[id1] = (short) (x - 1 + minA);
                            pos2[id2] = y;
                            if (hullManager.contains(pos1) != hullManager.contains(pos2)) {
                                // the "in between" point needs to be used for triangle generation
                                list.add(x);
                                list.add(outline[i + 1]);
                            }
                        }
                    }
                }
                // add last point (same as first)
                list.add(outline[0]);
                list.add(outline[1]);
                // add to result
                short[] shortsList = new short[list.size()];
                list.toArray(shortsList);
                result[i1][i2] = shortsList;
            }
        }
        return result;
    }

    // extract the necessary information from the hull manager
    private void extract(int algorithm) {
        setActivity("Extracting Mesh...", false);
        // loop over all sides
        for (int i = 0; i < 6; i++) {
            // get borders into specific direction and
            // calculate orientation related variables
            short[][] hull = hullManager.getHull(i);
            final int directionId = i/2;
            final boolean orientationPositive = i%2 != (directionId == 1 ? 1 : 0);
            final int offset = i%2 != 1 ? 1 : 0;

            // extract planes
            HashMap<Short, ArrayList<short[]>> planes = new HashMap<Short, ArrayList<short[]>>();
            for (short[] border : hull) {
                ArrayList<short[]> plane = planes.get(border[directionId]);
                if (plane == null) {
                    plane = new ArrayList<short[]>();
                    planes.put(border[directionId], plane);
                }
                plane.add(border);
            }

            // select the corresponding ids for the orientation
            int id1;
            int id2;
            switch (directionId) {
                case 0:
                    id1 = 1;
                    id2 = 2;
                    break;
                case 1:
                    id1 = 0;
                    id2 = 2;
                    break;
                default: //case 2
                    id1 = 0;
                    id2 = 1;
                    break;
            }

            // loop over planes
            int progressCount = 0;
            float elementCount = planes.size();
            for (Map.Entry<Short, ArrayList<short[]>> entries : planes.entrySet()) {
                setProgress((i/6f) * 100 + ((progressCount/elementCount)/6f) * 100);
                progressCount++;
                // generate mesh
                short minA = Short.MAX_VALUE;
                short minB = Short.MAX_VALUE;
                short maxA = Short.MIN_VALUE;
                short maxB = Short.MIN_VALUE;
                for (short[] entry : entries.getValue()) {
                    minA = (short) Math.min(minA, entry[id1]);
                    minB = (short) Math.min(minB, entry[id2]);
                    maxA = (short) Math.max(maxA, entry[id1]);
                    maxB = (short) Math.max(maxB, entry[id2]);
                }
                boolean[][] data = new boolean[maxA - minA + 1][maxB - minB + 1];
                for (short[] entry : entries.getValue()) {
                    data[entry[id1]-minA][entry[id2]-minB] = true;
                }

                Collection<DelaunayTriangle> tris;
                switch (algorithm) {
                    case ExportDataManager.MINIMAL_RECT_ALGORITHM:
                        tris = Grid2TriGreedyOptimal.triangulate(data);
                        break;
                    case ExportDataManager.NAIVE_ALGORITHM:
                        tris = Grid2TriNaive.triangulate(data);
                        break;
                    default:
                        // generate triangles
                        short[][][] polys = Grid2PolyHelper.convert(data);
                        // fix 3D t-junction problems
                        int planeAbove = entries.getKey() + (i%2 == 0 ? 1 : -1);
                        polys = fix3DTJunctionProblems(polys, planeAbove, id1, id2, minA, minB);
                        // extract triangles
                        tris = Grid2TriPolyFast.triangulate(polys);
                        break;
                }


                for (DelaunayTriangle tri : tris) {

                    // create the triangle
                    TexTriangle texTri = new TexTriangle(tri, triangleManager, i);

                    // create the texture (wrapper) for this triangle
                    TexTriUV[] uvs = texTri.getUVs();
                    TriTexture triTexture = new TriTexture(
                            // Note: The triangulation points might have rounding errors (!)
                            // So we <need> to round these values (casting to int is not sufficient!)
                            uvs[0], Math.round(minA + tri.points[0].getXf()), Math.round(minB + tri.points[0].getYf()),
                            uvs[1], Math.round(minA + tri.points[1].getXf()), Math.round(minB + tri.points[1].getYf()),
                            uvs[2], Math.round(minA + tri.points[2].getXf()), Math.round(minB + tri.points[2].getYf()),
                            entries.getKey(),
                            usePadding,
                            texTri, this.data,
                            textureManager
                    );

                    // set the texture for this triangle
                    texTri.setTexture(triTexture);

                    // add to the texture manager
                    textureManager.addTexture(triTexture);

                    // translate to triangle in 3D space
                    for (int p = 0; p < 3; p++) {
                        TexTriPoint point = texTri.getPoint(p);
                        float[] coord = point.getCoords();
                        point.set(directionId, entries.getKey() + offset);
                        point.set(id1, minA + coord[0]);
                        point.set(id2, minB + coord[1]);
                    }

                    // invert the triangle when necessary (correct back-face culling)
                    if (orientationPositive) {
                        texTri.invert();
                    }

                    // change positions so that the exported file is accurate
                    texTri.swap(1, 2);
                    texTri.invert(0);
                    texTri.invert(1);
                    texTri.invert(2);

                    // move one up
                    texTri.move(0.5f,0.5f,0.5f);

                    // scale to create integers
                    texTri.scale(2);

                    // convert to integer values
                    texTri.round();

                    // add to known triangles
                    triangleManager.addTriangle(texTri);
                }
            }
        }
    }

}
