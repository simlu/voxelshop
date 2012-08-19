package com.vitco.frames.engine.controller.animationdata;

import com.vitco.frames.engine.controller.listener.DataChangeListener;

/**
 * Main Container for a file that we work on.
 *
 * Holds basic animation data:
 * points, lines, color blocks (attached to lines), new frames (points moving from original frame)
 * Also provides means to add/remove/change the data
 * and stores history.
 */
public class AnimationData extends AnimationDataCore implements AnimationDataInterface {

    private void notifyAnimationSelectionChangeListener() {
        for (DataChangeListener dcl : listeners) {
            dcl.onAnimationSelectionChanged();
        }
    }

    private int highlightedPoint = -1;
    private int selectedPoint = -1;
    private final int[] previewLine = new int[2];

    @Override
    public int getHighlightedPoint() {
        return highlightedPoint;
    }

    @Override
    public int getSelectedPoint() {
        return selectedPoint;
    }

    @Override
    public int[][][] getPreviewLine() {
        if (isValid(previewLine[0]) && isValid(previewLine[1])) {
            return new int[][][] {
                    getPoint(previewLine[0]),
                    getPoint(previewLine[1])
            };
        } else {
            return null;
        }
    }

    @Override
    public void setPreviewLine(int id1, int id2) {
        if (previewLine[0] != id1 || previewLine[1] != id2) {
            if ((isValid(id1) || id1 == -1) && (isValid(id2) || id2 == -1)) {
                previewLine[0] = id1;
                previewLine[1] = id2;
                notifyAnimationSelectionChangeListener();
            }
        }
    }

    @Override
    public boolean highlightPoint(int id) {
        if (isValid(id) || id == -1) {
            if (highlightedPoint != id) {
                highlightedPoint = id;
                notifyAnimationSelectionChangeListener();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean selectPoint(int id) {
        if (isValid(id) || id == -1) {
            if (selectedPoint != id) {
                selectedPoint = id;
                notifyAnimationSelectionChangeListener();
            }
            return true;
        }
        return false;
    }

}
