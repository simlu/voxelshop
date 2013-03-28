package com.vitco.export.container;

/**
 * Represents a UV point (for texture coordinates).
 */
public class UVPoint {
    public final int id;
    public final String floatPos; // two points, separated by space
    public UVPoint(int id, String floatPos) {
        this.id = id;
        this.floatPos = floatPos;
    }
}
