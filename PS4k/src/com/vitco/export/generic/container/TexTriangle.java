package com.vitco.export.generic.container;

import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

/**
 * A textured triangle.
 */
public class TexTriangle {

    // the three points of this triangle
    private final TexTriPoint[] points = new TexTriPoint[3];

    // the three uvs of this triangle
    private final TexTriUV[] uvs = new TexTriUV[3];

    // get the coordinates of a point
    public final TexTriPoint getPoint(int point) {
        return points[point];
    }

    // get the uvs of a point
    public final TexTriUV[] getUVs() {
        return uvs.clone();
    }

    // -----------

    // the orientation of this triangle
    private final int orientation;

    // getter for orientation
    public final int getOrientation() {
        return orientation;
    }

    // the texture that this triangle uses
    private TriTexture texture = null;

    // get the texture of this triangle
    public final TriTexture getTexture() {
        return texture;
    }

    // set the texture of this triangle;
    public final void setTexture(TriTexture texture) {
        this.texture = texture;
    }

    // ------------------------

    // constructor
    public TexTriangle(DelaunayTriangle tri, TexTriangleManager manager, int orientation) {
        for (int i = 0; i < 3; i++) {
            points[i] = new TexTriPoint(
                    Math.round(tri.points[i].getXf()),
                    Math.round(tri.points[i].getYf()),
                    Math.round(tri.points[i].getZf()),
                    manager
            );
            uvs[i] = new TexTriUV(0,0,manager);
        }
        this.orientation = orientation;
    }

    // invert this triangle
    public final void invert() {
        TexTriPoint tmp1 = points[0];
        points[0] = points[1];
        points[1] = tmp1;

        TexTriUV tmp2 = uvs[0];
        uvs[0] = uvs[1];
        uvs[1] = tmp2;
    }

    // -------------------
    // Below Changes the coordinates of the different points
    // (but not the order of the points!)
    // -------------------

    // swap two coordinates
    public final void swap(int coord1, int coord2) {
        for (TexTriPoint point : points) {
            point.swap(coord1, coord2);
        }
    }

    // invert a coordinate
    public final void invert(int coord) {
        for (TexTriPoint point : points) {
            point.invert(coord);
        }
    }

    // move this triangle
    public final void move(float x, float y, float z) {
        for (TexTriPoint point : points) {
            point.move(0, x);
            point.move(1, y);
            point.move(2, z);
        }
    }

    // scale this triangle
    public final void scale(float factor) {
        for (TexTriPoint point : points) {
            float[] coords = point.getCoords();
            point.set(0, coords[0] * factor);
            point.set(1, coords[1] * factor);
            point.set(2, coords[2] * factor);
        }
    }

    // round all points of this triangle to integers
    public void round() {
        for (TexTriPoint point : points) {
            point.round();
        }
    }
}
