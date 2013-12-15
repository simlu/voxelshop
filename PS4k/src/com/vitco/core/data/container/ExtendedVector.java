package com.vitco.core.data.container;

import com.threed.jpct.SimpleVector;

import java.io.Serializable;

/**
 * Extended Vector that has in addition to a SimpleVector an id.
 *
 * Also defines equality method: true iff same position and id.
 */
public final class ExtendedVector extends SimpleVector implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int id;
    public ExtendedVector(float x, float y, float z, int id) {
        super(x, y, z);
        this.id = id;
    }

    public ExtendedVector(SimpleVector pos, Integer id) {
        super(pos);
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof ExtendedVector) {
            ExtendedVector extendedVector = (ExtendedVector)obj;
            if (this.x == extendedVector.x && this.y == extendedVector.y &&
                    this.z == extendedVector.z && this.id == extendedVector.id) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        return id + super.hashCode();
    }
}
