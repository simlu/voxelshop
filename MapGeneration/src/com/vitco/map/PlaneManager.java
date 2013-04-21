package com.vitco.map;

import com.vitco.export.ColladaFile;
import com.vitco.export.container.UVPoint;
import com.vitco.export.container.Vertex;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Manages the different planes and also handles the sides
 */
public class PlaneManager extends ColladaFile {
    private int IMAGE_WIDTH;
    private int IMAGE_HEIGHT;

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
        public int hashCode() {
            return (int) (Math.round(x1) * Math.round(y1) + Math.round(x2) * Math.round(y2));
        }
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

    // holds all the edges so we know where to draw the sides
    private final HashMap<LineInfo, LineInfo> edges = new HashMap<LineInfo, LineInfo>();

    public PlaneManager(int IMAGE_WIDTH, int IMAGE_HEIGHT) {
        super();
        this.IMAGE_WIDTH = IMAGE_WIDTH;
        this.IMAGE_HEIGHT = IMAGE_HEIGHT;
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

                LineInfo line = new LineInfo(coord.x, coord.y, height, coords[i+1].x, coords[i+1].y, height);
                LineInfo existingLine = edges.get(line);
                if (existingLine != null) {
                    // draw this side
                    ArrayList<Vertex> list = new ArrayList<Vertex>();
                    list.add(new Vertex(Math.round(line.x1), Math.round(line.y1), (float)line.z1,
                            new UVPoint(new float[] {
                                    (float) (line.x1/IMAGE_WIDTH),
                                    (float) (1-line.y1/IMAGE_HEIGHT)})));
                    list.add(new Vertex(Math.round(line.x2), Math.round(line.y2), (float)line.z2,
                            new UVPoint(new float[] {
                                    (float) (line.x2/IMAGE_WIDTH),
                                    (float) (1-line.y2/IMAGE_HEIGHT)})));
                    list.add(new Vertex(Math.round(existingLine.x1), Math.round(existingLine.y1), (float)existingLine.z1,
                            new UVPoint(new float[] {
                                    (float) (existingLine.x1/IMAGE_WIDTH),
                                    (float) (1-existingLine.y1/IMAGE_HEIGHT)})));
                    list.add(new Vertex(Math.round(existingLine.x2), Math.round(existingLine.y2), (float)existingLine.z2,
                            new UVPoint(new float[] {
                                    (float) (existingLine.x2/IMAGE_WIDTH),
                                    (float) (1-existingLine.y2/IMAGE_HEIGHT)})));
                    this.addPolygon(list);
                } else {
                    edges.put(line, line);
                }

            }
        }
        // triangulate the polygon
        Poly2Tri.triangulate(polyR);
        // add the polygon
        addTrianglePolygon(polyR);
    }

    // internal - add polygon
    private void addTrianglePolygon(org.poly2tri.geometry.polygon.Polygon polyR) {
        assert polyR != null;
        // add all the triangles for this plane
        for (DelaunayTriangle tri : polyR.getTriangles()) {
            ArrayList<Vertex> list = new ArrayList<Vertex>();
            list.add(new Vertex(
                    Math.round(tri.points[0].getX()),
                    Math.round(tri.points[0].getY()),
                    (float)tri.points[0].getZ(),
                    new UVPoint(new float[] {
                        tri.points[0].getXf()/IMAGE_WIDTH,
                            1-tri.points[0].getYf()/IMAGE_HEIGHT})));
            list.add(new Vertex(
                    Math.round(tri.points[1].getX()),
                    Math.round(tri.points[1].getY()),
                    (float)tri.points[1].getZ(),
                    new UVPoint(new float[] {
                        tri.points[1].getXf()/IMAGE_WIDTH,
                            1-tri.points[1].getYf()/IMAGE_HEIGHT})));
            list.add(new Vertex(
                    Math.round(tri.points[2].getX()),
                    Math.round(tri.points[2].getY()),
                    (float)tri.points[2].getZ(),
                    new UVPoint(new float[] {
                        tri.points[2].getXf()/IMAGE_WIDTH,
                            1-tri.points[2].getYf()/IMAGE_HEIGHT})));
            this.addPolygon(list);
        }
    }
}
