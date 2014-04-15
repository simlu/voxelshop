package com.vitco.export.container;

import java.util.Arrays;

/**
 * UV of a textured triangle.
 */
public class TexTriUV extends TexTriCornerInfo {
    // uv values
    private final float[] uv = new float[2];

    // constructor
    public TexTriUV(float u, float v, TexTriangleManager manager) {
        super(manager);
        uv[0] = u;
        uv[1] = v;
        manager.addUV(this);
    }

    // -----------------

    // set this uv
    public final void set(float u, float v) {
        uv[0] = u;
        uv[1] = v;
        manager.invalidateUVs();
    }

    // get the coordinates of this point
    public final float[] getUV() {
        return uv.clone();
    }

    // --------------

    @Override
    public final int getId() {
        return manager.getUVId(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TexTriUV that = (TexTriUV) o;
        return Arrays.equals(uv, that.uv);

    }

    @Override
    public final int hashCode() {
        return Arrays.hashCode(uv);
    }

    @Override
    public final String toString(boolean useInt) {
        if (useInt) {
            return (int)uv[0] + " " + (int)uv[1];
        } else {
            return uv[0] + " " + uv[1];
        }
    }
}
