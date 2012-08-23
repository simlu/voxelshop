package com.vitco.frames.engine.data.animationdata;

import com.vitco.frames.engine.data.listener.DataChangeListener;

import java.io.Serializable;

/**
 * Main Container for a file that we work on.
 *
 * Holds basic animation data:
 * points, lines, color blocks (attached to lines), new frames (points moving from original frame)
 * Also provides means to add/remove/change the data
 * and stores history.
 */
public interface AnimationDataCoreInterface extends Serializable {
    // adds a point, returns id of that point
    int addPoint(float x, float y, float z);
    // removes point with id, returns true iff successful
    boolean removePoint(int id);
    // moves a point to the new coordinates
    boolean movePoint(int id, float x, float y, float z);
    // connects p1 and p2 with a line, true iff successful
    boolean connect(int id1, int id2);
    // removes all lines and all points
    void clear();
    // disconnects p1 and p2, returns true iff successful
    boolean disconnect(int id1, int id2);
    // returns all the points
    float[][][] getPoints();
    // returns all the lines
    float[][][][] getLines();
    // adds a listener
    void addDataChangeListener(DataChangeListener dcl);
    // removes a listener
    void removeDataChangeListener(DataChangeListener dcl);
    // returns the formatted point for a key (key, x, y, z)
    float[][] getPoint(int id);
    // true iff the two points are connected by a line
    boolean areConnected(int id1, int id2);
}
