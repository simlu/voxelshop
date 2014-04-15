package com.vitco.export.container;

/**
 * Abstract class that holds corner information for a TexTriangle
 */
public abstract class TexTriCornerInfo {
    // string representation
    public abstract String toString(boolean useInt);
    @Override
    public String toString() {
        return toString(false);
    }

    // making sure these will be overwritten
    // Note: two info objects should be equal if they represent the same data
    @Override
    public abstract boolean equals(Object o);
    @Override
    public abstract int hashCode();

    // the manager of this tex triangle point
    protected final TexTriangleManager manager;

    // constructor
    public TexTriCornerInfo(TexTriangleManager manager) {
        this.manager = manager;
    }

    // get the id of this info object
    public abstract int getId();
}
