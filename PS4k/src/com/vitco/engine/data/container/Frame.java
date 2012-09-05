package com.vitco.engine.data.container;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Defines an animation frame. Points that are different from the main frame are set.
 */
public class Frame implements Serializable {
    private static final long serialVersionUID = 1L;

    // the current name
    private String name;

    // constructor
    public Frame(String name) {
        this.name = name;
    }

    // get name
    public String getName() {
        return name;
    }

    // set name
    public void setName(String name) {
        this.name = name;
    }

    // the points that are moved
    private final HashMap<Integer, ExtendedVector> points = new HashMap<Integer, ExtendedVector>();

    // sets a point
    public void setPoint(int id, ExtendedVector point) {
        points.put(id, point);
    }

    // return a point, null if not set
    public ExtendedVector getPoint(int id) {
        return points.get(id);
    }

    // remove a point
    public void removePoint(int id) {
        points.remove(id);
    }

    // get all points in this frame
    public Integer[] getPoints() {
        Integer[] result = new Integer[points.size()];
        points.keySet().toArray(result);
        return result;
    }

}
