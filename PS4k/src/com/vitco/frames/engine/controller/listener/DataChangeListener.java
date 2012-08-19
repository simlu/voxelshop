package com.vitco.frames.engine.controller.listener;

/**
 * A data change listener
 */
public interface DataChangeListener {
    void onAnimationDataChanged();
    void onAnimationSelectionChanged();
    void onFrameDataChanged();
    void onVoxelDataChanged();
}
