package com.vitco.export.container;

/**
 * Definition of a vertex for Collada Export.
 */
public final class Vertex {
    // id of this vertex
    private int id;

    // position of this vertex
    public final float x;
    public final float y;
    public final float z;
    // string representation of the position
    private final String toString;

    // constructor
    public Vertex(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        toString = x + "_" + y + "_" + z;
    }

    // set the id of this vertex
    public void setId(int id) {
        this.id = id;
    }
    // get the id of this vertex
    public int getId() {
        return id;
    }


    // string representation of this vector (only position)
    @Override
    public String toString() {
        return toString;
    }
}
