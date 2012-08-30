package com.vitco.frames.engine.data;

import com.vitco.frames.engine.data.container.ExtendedVector;

/**
 * Defines the methods that help highlighting of animation data (frame).
 */
public abstract class AnimationHighlight extends AnimationData implements AnimationHighlightInterface {

    // point that is currently highlighted
    private int highlightedPoint = -1;
    // point that is currently selected
    private int selectedPoint = -1;
    // line that is currently previewed
    private final Integer[] previewLine = new Integer[]{-1, -1};

    // remove all highlights
    @Override
    public void removeHighlights() {
        highlightPoint(-1);
        selectPoint(-1);
        setPreviewLine(-1, -1);
    }

    // highlight / remove highlight of point
    @Override
    public boolean highlightPoint(int id) {
        if (isValid(id) || id == -1) {
            if (highlightedPoint != id) {
                highlightedPoint = id;
                notifier.onAnimationSelectionChanged();
            }
            return true;
        }
        return false;
    }

    // select / deselect point
    @Override
    public boolean selectPoint(int id) {
        if (isValid(id) || id == -1) {
            if (selectedPoint != id) {
                selectedPoint = id;
                notifier.onAnimationSelectionChanged();
            }
            return true;
        }
        return false;
    }

    // retrieve highlighted point
    @Override
    public int getHighlightedPoint() {
        return highlightedPoint;
    }

    // retrieve selected point
    @Override
    public int getSelectedPoint() {
        return selectedPoint;
    }

    // set the preview line
    @Override
    public void setPreviewLine(int id1, int id2) {
        if (previewLine[0] != id1 || previewLine[1] != id2) {
            if ((isValid(id1) || id1 == -1) && (isValid(id2) || id2 == -1)) {
                previewLine[0] = id1;
                previewLine[1] = id2;
                previewLineBufferValid = false;
                notifier.onAnimationSelectionChanged();
            }
        }
    }

    // buffer for the preview line
    ExtendedVector[] previewLineBuffer = null;
    // true iff the buffer is still valid
    boolean previewLineBufferValid = false;
    @Override
    public ExtendedVector[] getPreviewLine() {
        if (!previewLineBufferValid) {
            if (isValid(previewLine[0]) && isValid(previewLine[1])) {
                previewLineBuffer = new ExtendedVector[] {
                        getPoint(previewLine[0]),
                        getPoint(previewLine[1])
                };
            } else {
                previewLineBuffer = null;
            }
            previewLineBufferValid = true;
        }
        return previewLineBuffer != null ? previewLineBuffer.clone() : null;
    }
}
