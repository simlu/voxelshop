package com.vitco.util.hull;

import java.util.Set;

/**
 * Interface - Efficient way to compute the hull for a group of
 * objects in 3D space (with short values as coordinates)
 *
 * At most one object is expected in any position at any given time.
 */
public interface HullFinderInterface<T> {

    void clear();

    boolean contains(short[] pos);

    // add an object to the hull finder
    public void update(short[] pos, T object);

    // remove an object from this hull finder
    public boolean clearPosition(short[] pos);

    // get the hull additions (in direction 0-5)
    Set<T> getHullAdditions(int direction);

    // get the hull removals (in direction 0-5)
    Set<T> getHullRemovals(int direction);
}
