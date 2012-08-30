package com.vitco.frames.engine.data.listener;

/**
 * A data change listener
 */
public interface DataChangeListener {
    void onAnimationDataChanged();
    void onAnimationSelectionChanged();
    void onVoxelDataChanged();
    void onLayerDataChanged();
}
