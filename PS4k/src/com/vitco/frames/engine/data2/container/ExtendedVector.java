package com.vitco.frames.engine.data2.container;

import com.threed.jpct.SimpleVector;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 8/24/12
 * Time: 11:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExtendedVector extends SimpleVector {
    public int id;
    public ExtendedVector(float x, float y, float z, int id) {
        super(x, y, z);
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof ExtendedVector) {
            ExtendedVector extendedVector = (ExtendedVector)obj;
            if (super.equals(extendedVector) && this.id == extendedVector.id) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        return id + super.hashCode();
    }

    public ExtendedVector clone() {
        return new ExtendedVector(x, y, z, id);
    }
}
