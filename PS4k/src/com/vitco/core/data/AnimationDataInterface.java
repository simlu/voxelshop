package com.vitco.core.data;

import com.threed.jpct.SimpleVector;
import com.vitco.core.data.container.ExtendedVector;

/**
 * Interface for Animation Data and Frame interaction.
 */
public interface AnimationDataInterface {
    // check if a point is valid
    boolean isValid(int pointId);
    // adds a point and returns the id that was assigned
    int addPoint(SimpleVector point); // all frames
    // removes a points, returns true iff the point was successfully removed
    boolean removePoint(int pointId); // all frames
    // move point in the current frame to new position
    boolean movePoint(int pointId, SimpleVector pos); // current frame
    // returns true iff two points are connected by a line
    boolean areConnected(int id1, int id2);
    // connects two points by a line, returns true iff successful
    boolean connect(int id1, int id2);
    // deletes all points and lines
    boolean clearA(); // all frames
    // disconnects the line between two points, returns true iff successful
    boolean disconnect(int id1, int id2);
    // returns a point for a given id
    ExtendedVector getPoint(int pointId); // current frame
    // returns all points
    ExtendedVector[] getPoints(); // current frame
    // returns all lines
    ExtendedVector[][] getLines();

    // undo last action (animation)
    void undoA();
    // redo last action (animation)
    void redoA();
    // return true if the last action can be undone (animation)
    boolean canUndoA();
    // return true if the last action can be redone (animation)
    boolean canRedoA();

    // sets the current frame, returns true iff frame exists
    boolean selectFrame(int frameId);
    // returns the selected frame
    int getSelectedFrame();
    // creates a new frame and returns the assigned frame id
    int createFrame(String frameName);
    // deletes a frame, returns true if frame exists
    boolean deleteFrame(int frameId);
    // rename a frame
    boolean renameFrame(int frameId, String newName);
    // get all frame ids
    Integer[] getFrames();
    // get the name of a frame by id
    String getFrameName(int frameId);
    // resets the current frame to base
    boolean resetFrame(int frameId);
}