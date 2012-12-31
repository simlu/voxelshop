package com.vitco.frames.engine.data;

import com.vitco.frames.engine.data.container.ExtendedVector;

/**
 * Defines the signature of methods that help highlighting of animation data (frame).
 */
public interface AnimationHighlightInterface {
    // remove all highlights
    void removeHighlights();
    // highlight a point
    boolean highlightPoint(int id);
    // select a point
    boolean selectPoint(int id);
    // retrieve highlighted point
    int getHighlightedPoint();
    // retrieve selected point
    int getSelectedPoint();
    // set preview line
    void setPreviewLine(int id1, int id2);
    // retrieve preview line
    ExtendedVector[] getPreviewLine();
}
