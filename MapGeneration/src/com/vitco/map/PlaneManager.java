package com.vitco.map;

import com.vitco.export.ColladaFile;
import com.vitco.export.container.UVPoint;
import com.vitco.export.container.Vertex;
import com.vitco.util.RTree;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.util.*;

/**
 * Manages the different planes and also handles the sides
 */
public class PlaneManager extends ColladaFile {
    private int IMAGE_WIDTH;
    private int IMAGE_HEIGHT;

    // holds all the points
    private final HashMap<String, PointInfo> pointXYs = new HashMap<String, PointInfo>();

    // holds all the neighboring points
    private HashMap<LineInfo, ArrayList<LineInfo>> lines = new HashMap<LineInfo, ArrayList<LineInfo>>();

    public final class LineInfo {
        public final double x1,y1,z1,x2,y2,z2;

        public LineInfo(double x1, double y1, double z1, double x2, double y2, double z2) {
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
        }

        @Override
        public String toString() {
            return "(" + x1 + ", " + y1 + ", " + z1 + ") -> (" + x2 + ", " + y2 + ", " + z2 + ")";
        }

        @Override
        public int hashCode() {
            return (int) (Math.round(x1) * Math.round(y1) + Math.round(x2) * Math.round(y2));
        }

        // two lines are equal if they are drawn between the same
        // points
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            LineInfo other = (LineInfo) obj;
            return  (Math.round(x1) == Math.round(other.x1) &&
                    Math.round(y1) == Math.round(other.y1) &&
                    Math.round(x2) == Math.round(other.x2) &&
                    Math.round(y2) == Math.round(other.y2)) ||
                    (Math.round(x1) == Math.round(other.x2) &&
                            Math.round(y1) == Math.round(other.y2) &&
                            Math.round(x2) == Math.round(other.x1) &&
                            Math.round(y2) == Math.round(other.y1));
        }
    }

    public final class PointInfo {
        public final float x, y;

        public PointInfo(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode() {
            return Math.round(x) * Math.round(y);
        }

        // two points are equal if they have the
        // same (rounded) points
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            PointInfo other = (PointInfo) obj;
            return Math.round(other.x) == Math.round(x) && Math.round(other.y) == Math.round(y);
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }

        public double distance(PointInfo pointInfo) {
            return Math.sqrt(Math.pow(pointInfo.x - x, 2) + Math.pow(pointInfo.y - y, 2));
        }
    }

    public PlaneManager(int IMAGE_WIDTH, int IMAGE_HEIGHT) {
        super();
        this.IMAGE_WIDTH = IMAGE_WIDTH;
        this.IMAGE_HEIGHT = IMAGE_HEIGHT;
    }

    private static double distance(double val) {
        return Math.round(val) - val;
    }

    // generate all sides
    public void generateSides() {
        // extract all points
        RTree<PointInfo> searchTree = new RTree<PointInfo>(50, 2, 2);
        HashSet<PointInfo> points = new HashSet<PointInfo>();
        for (LineInfo lineInfo : lines.keySet()) {
            PointInfo pointInfo1 = new PointInfo( (float)lineInfo.x1, (float)lineInfo.y1);
            PointInfo pointInfo2 = new PointInfo( (float)lineInfo.x2, (float)lineInfo.y2);
            if (!points.contains(pointInfo1)) {
                points.add(pointInfo1);
                searchTree.insert(new float[] {Math.round(pointInfo1.x), Math.round(pointInfo1.y)}, pointInfo1);
            }
            if (!points.contains(pointInfo2)) {
                points.add(pointInfo2);
                searchTree.insert(new float[] {Math.round(pointInfo2.x), Math.round(pointInfo2.y)}, pointInfo2);
            }
        }

        // split all the lines (that need splitting)
        for (Object lineInfoObj : lines.keySet().toArray()) {
            LineInfo lineInfo = (LineInfo)lineInfoObj;
            float minx = Math.min(Math.round(lineInfo.x1), Math.round(lineInfo.x2));
            float maxx = Math.max(Math.round(lineInfo.x1), Math.round(lineInfo.x2));
            float miny = Math.min(Math.round(lineInfo.y1), Math.round(lineInfo.y2));
            float maxy = Math.max(Math.round(lineInfo.y1), Math.round(lineInfo.y2));
            List<PointInfo> result = searchTree.search(
                    new float[] {minx, miny},
                    new float[] {maxx - minx, maxy - miny});
            float zValue = (float) lineInfo.z1;

            // add all the points: start, stop of line and any points
            // that are in between those two points
            ArrayList<PointInfo> linePoints = new ArrayList<PointInfo>();
            final PointInfo firstPoint = new PointInfo((float)lineInfo.x1, (float)lineInfo.y1);
            linePoints.add(firstPoint);
            for (PointInfo pointInfo : result) {
                if (!(Math.round(lineInfo.x1) == Math.round(pointInfo.x) &&
                        Math.round(lineInfo.y1) == Math.round(pointInfo.y)) &&
                        !(Math.round(lineInfo.x2) == Math.round(pointInfo.x) &&
                        Math.round(lineInfo.y2) == Math.round(pointInfo.y))) {
                    PointInfo smoothPointInfo = new PointInfo(
                        Math.round(pointInfo.x) == Math.round(firstPoint.x) ? firstPoint.x : pointInfo.x,
                        Math.round(pointInfo.y) == Math.round(firstPoint.y) ? firstPoint.y : pointInfo.y
                    );
                    linePoints.add(smoothPointInfo);
                }
            }
            linePoints.add(new PointInfo((float)lineInfo.x2, (float)lineInfo.y2));

            if (linePoints.size() > 2) { // this line needs to be split
                // sort according to distance from start point
                Collections.sort(linePoints, new Comparator<PointInfo>() {
                    @Override
                    public int compare(PointInfo o1, PointInfo o2) {
                        return (int) Math.signum(o1.distance(firstPoint) - o2.distance(firstPoint));
                    }
                });

                // delete the original
                lines.remove(lineInfo);

                // create all the "sub-lines" of this line
                for (int i = 0; i < linePoints.size() - 1; i++) {
                    PointInfo pointInfo1 = linePoints.get(i);
                    PointInfo pointInfo2 = linePoints.get(i+1);
                    LineInfo lineInfoSplit = new LineInfo(
                            pointInfo1.x, pointInfo1.y, zValue,
                            pointInfo2.x, pointInfo2.y, zValue);

                    ArrayList<LineInfo> lineInfos = lines.get(lineInfoSplit);
                    if (lineInfos == null) {
                        lineInfos = new ArrayList<LineInfo>();
                    }
                    lineInfos.add(lineInfoSplit);
                    lines.put(lineInfoSplit, lineInfos);
                }
            }
        }

        // draw all the sides (squares)
        for (ArrayList<LineInfo> heightLines : lines.values()) {
            for (int i = 0; i < heightLines.size(); i++) {
                LineInfo lineInfo1 = heightLines.get(i);
                for (int j = i + 1; j < heightLines.size(); j++) {
                    LineInfo lineInfo2 = heightLines.get(j);
                    // the z value needs to be different
                    if (lineInfo1.z1 != lineInfo2.z2) {

                        // add this rectangle
                        ArrayList<Vertex> list = new ArrayList<Vertex>();
                        float uValue = (float) ((lineInfo1.x1 - distance(lineInfo1.x1) * 2) / IMAGE_WIDTH);
                        float vValue = (float) (1 - (lineInfo1.y1 - distance(lineInfo1.y1) * 2) / IMAGE_HEIGHT);
                        list.add(new Vertex(
                                Math.round(lineInfo1.x1) - IMAGE_WIDTH / 2f,
                                Math.round(lineInfo1.y1) - IMAGE_HEIGHT / 2f,
                                (float) lineInfo1.z1,
                                new UVPoint(new float[]{uValue, vValue})));
                        uValue = (float) ((lineInfo1.x2 - distance(lineInfo1.x2) * 2) / IMAGE_WIDTH);
                        vValue = (float) (1 - (lineInfo1.y2 - distance(lineInfo1.y2) * 2) / IMAGE_HEIGHT);
                        list.add(new Vertex(
                                Math.round(lineInfo1.x2) - IMAGE_WIDTH / 2f,
                                Math.round(lineInfo1.y2) - IMAGE_HEIGHT / 2f,
                                (float) lineInfo1.z2,
                                new UVPoint(new float[]{uValue, vValue})));
                        uValue = (float) ((lineInfo2.x1 - distance(lineInfo2.x1) * 2) / IMAGE_WIDTH);
                        vValue = (float) (1 - (lineInfo2.y1 - distance(lineInfo2.y1) * 2) / IMAGE_HEIGHT);
                        list.add(new Vertex(
                                Math.round(lineInfo2.x1) - IMAGE_WIDTH / 2f,
                                Math.round(lineInfo2.y1) - IMAGE_HEIGHT / 2f,
                                (float) lineInfo2.z1,
                                new UVPoint(new float[]{uValue, vValue})));
                        uValue = (float) ((lineInfo2.x2 - distance(lineInfo2.x2) * 2) / IMAGE_WIDTH);
                        vValue = (float) (1 - (lineInfo2.y2 - distance(lineInfo2.y2) * 2) / IMAGE_HEIGHT);
                        list.add(new Vertex(
                                Math.round(lineInfo2.x2) - IMAGE_WIDTH / 2f,
                                Math.round(lineInfo2.y2) - IMAGE_HEIGHT / 2f,
                                (float) lineInfo2.z2,
                                new UVPoint(new float[]{uValue, vValue})));
                        this.addPolygon(list);

                    }
                }

            }
        }

    }

    public final void addPlane(Polygon poly, float height) {
        // make the covered area smaller (increases point count?)
        Geometry inside = poly.buffer(-0.3f, 0);
        Coordinate[] coords = inside.getCoordinates();

        // holds the points that were already seen, so we know
        // where a polygon (full or hole) starts
        HashSet<String> seenPoints = new HashSet<String>();
        ArrayList<PolygonPoint> points = new ArrayList<PolygonPoint>();
        org.poly2tri.geometry.polygon.Polygon polyR = null;
        for (int i = 0; i < coords.length; i++) {
            Coordinate coord = coords[i];
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
                points.add(new PolygonPoint(coord.x, coord.y, height));

                LineInfo lineInfo = new LineInfo(coord.x, coord.y, height,
                        coords[i+1].x, coords[i+1].y, height);
                ArrayList<LineInfo> lineInfos = lines.get(lineInfo);
                if (lineInfos == null) {
                    lineInfos = new ArrayList<LineInfo>();
                }
                lineInfos.add(lineInfo);
                lines.put(lineInfo, lineInfos);
            }
        }
        // triangulate the polygon
        Poly2Tri.triangulate(polyR);
        // add the polygon
        addTrianglePolygon(polyR);
    }

    // internal - add polygon consisting of triangles
    private void addTrianglePolygon(org.poly2tri.geometry.polygon.Polygon polyR) {
        assert polyR != null;
        // add all the triangles for this plane
        for (DelaunayTriangle tri : polyR.getTriangles()) {
            ArrayList<Vertex> list = new ArrayList<Vertex>();
            list.add(new Vertex(
                    Math.round(tri.points[0].getX()) - IMAGE_WIDTH/2f,
                    Math.round(tri.points[0].getY()) - IMAGE_HEIGHT/2f,
                    (float)tri.points[0].getZ(),
                    new UVPoint(new float[] {
                            tri.points[0].getXf()/IMAGE_WIDTH,
                            1-tri.points[0].getYf()/IMAGE_HEIGHT})));
            list.add(new Vertex(
                    Math.round(tri.points[1].getX()) - IMAGE_WIDTH/2f,
                    Math.round(tri.points[1].getY()) - IMAGE_HEIGHT/2f,
                    (float)tri.points[1].getZ(),
                    new UVPoint(new float[] {
                            tri.points[1].getXf()/IMAGE_WIDTH,
                            1-tri.points[1].getYf()/IMAGE_HEIGHT})));
            list.add(new Vertex(
                    Math.round(tri.points[2].getX()) - IMAGE_WIDTH/2f,
                    Math.round(tri.points[2].getY()) - IMAGE_HEIGHT/2f,
                    (float)tri.points[2].getZ(),
                    new UVPoint(new float[] {
                            tri.points[2].getXf()/IMAGE_WIDTH,
                            1-tri.points[2].getYf()/IMAGE_HEIGHT})));
            this.addPolygon(list);
        }
    }
}
