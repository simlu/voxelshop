package com.vitco.export.generic.container;

import java.util.Arrays;

/**
 * Point of a textured triangle.
 */
public class TexTriPoint extends TexTriCornerInfo {

    // coordinates of this tex triangle point
    private final float[] coords = new float[3];

    // constructor
    public TexTriPoint(float x, float y, float z, TexTriangleManager manager) {
        super(manager);
        coords[0] = x;
        coords[1] = y;
        coords[2] = z;
        manager.addPoint(this);
    }

    // -----------------

    // move this point
    public final void move(int axis, float val) {
        coords[axis] += val;
        manager.invalidatePoints();
    }

    // set this point
    public final void set(int axis, float val) {
        coords[axis] = val;
        manager.invalidatePoints();
    }

    // swap two coordinates of this point
    public final void swap(int coord1, int coord2) {
        float tmp = coords[coord1];
        coords[coord1] = coords[coord2];
        coords[coord2] = tmp;
        manager.invalidatePoints();
    }

    // invert a coordinate of this point
    public final void invert(int coord) {
        coords[coord] = -coords[coord];
        manager.invalidatePoints();
    }

    // round all coordinates (to integer)
    public final void round() {
        coords[0] = Math.round(coords[0]);
        coords[1] = Math.round(coords[1]);
        coords[2] = Math.round(coords[2]);
        manager.invalidatePoints();
    }

    // get the coordinates of this point
    public final float[] getCoords() {
        return coords.clone();
    }

    // -----------------

    @Override
    public final int getId() {
        return manager.getPointId(this);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TexTriPoint that = (TexTriPoint) o;
        return Arrays.equals(coords, that.coords);
    }

    @Override
    public final int hashCode() {
        return Arrays.hashCode(coords);
    }

    @Override
    public final String toString(boolean asInt) {
        if (asInt) {
            return Math.round(coords[0]) + " " + Math.round(coords[1]) + " " + Math.round(coords[2]);
        } else {
            return coords[0] + " " + coords[1] + " " + coords[2];
        }
    }
}
