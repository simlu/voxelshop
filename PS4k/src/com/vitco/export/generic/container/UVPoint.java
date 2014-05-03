package com.vitco.export.generic.container;

/**
 * Represents a UV point (for texture coordinates).
 */
public class UVPoint {
    public final int id;
    public final String floatPos; // two points, separated by space
    public UVPoint(int id, float[] floatPos,
                   boolean plusx, boolean plusy,
                   float interpx, float interpy) {
        this.id = id;
        this.floatPos = (floatPos[0] + (plusx ? interpx : -interpx)) + " " +
                (floatPos[1] + (plusy ? interpy : -interpy));
    }
}
