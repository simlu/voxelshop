package com.vitco.export.container;

/**
 * Represents a UV point (for texture coordinates).
 */
public class UVPoint {
    private int id;
    public final String floatPos; // two points, separated by space
    public UVPoint(float[] floatPos) {
        this.floatPos = floatPos[0] + " " + floatPos[1];
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
