package com.vitco.frames.engine.data2.container;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 8/27/12
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class Frame {
    private String name;

    public Frame(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private HashMap<Integer, ExtendedVector> points = new HashMap<Integer, ExtendedVector>();

    public void setPoint(int id, ExtendedVector point) {
        points.put(id, point);
    }

    public ExtendedVector getPoint(int id) {
        return points.get(id);
    }

    public void removePoint(int id) {
        points.remove(id);
    }

    public Integer[] getPoints() {
        Integer[] result = new Integer[points.size()];
        points.keySet().toArray(result);
        return result;
    }

}
