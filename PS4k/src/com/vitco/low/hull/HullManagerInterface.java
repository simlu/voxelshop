package com.vitco.low.hull;

import com.threed.jpct.SimpleVector;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Set;

/**
 * Interface - Efficient way to compute the hull for a group of
 * objects in 3D space (with short values as coordinates)
 *
 * At most one object is expected in any position at any given time.
 */
public interface HullManagerInterface<T> {

    void clear();

    boolean contains(short[] pos);

    boolean contains(int posId);

    boolean containsBorder(short[] pos, int orientation);

    boolean containsBorder(int posId, int orientation);

    int[] getPosIds();

    // obtain object by position
    T get(short[] pos);

    // add an object to the hull finder
    public void update(short[] pos, T object);

    // remove an object from this hull finder
    void update(int posId, T object);

    // remove an object from this hull finder
    public boolean clearPosition(short[] pos);

    // remove an object from this hull finder
    boolean clearPosition(int posId);

    // get the hull additions (in direction 0-5)
    Set<T> getHullAdditions(int direction);

    // get the hull removals (in direction 0-5)
    Set<T> getHullRemovals(int direction);

    // get the current hull
    short[][] getHull(int direction);

    // get the visible voxel ids
    TIntHashSet getVisibleVoxelsIds();

    // get the current hull as ids
    int[] getHullAsIds(int direction);

    // get the outline of all voxels into one direction
    SimpleVector[][] getOutline(int direction);
}
