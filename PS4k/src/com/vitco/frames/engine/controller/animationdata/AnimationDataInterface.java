package com.vitco.frames.engine.controller.animationdata;

/**
 * Main Container for a file that we work on.
 *
 * Holds basic animation data:
 * points, lines, color blocks (attached to lines), new frames (points moving from original frame)
 * Also provides means to add/remove/change the data
 * and stores history.
 */
public interface AnimationDataInterface extends AnimationDataCoreInterface {

    boolean highlightPoint(int id);

    boolean selectPoint(int id);

    int getHighlightedPoint();

    int getSelectedPoint();

    void setPreviewLine(int id1, int id2);

    int[][][] getPreviewLine();
}
