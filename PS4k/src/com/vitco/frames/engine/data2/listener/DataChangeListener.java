package com.vitco.frames.engine.data2.listener;

/**
 * A data change listener
 */
public interface DataChangeListener {
    void onAnimationDataChanged();
    void onAnimationSelectionChanged();
    void onVoxelDataChanged();
    void onLayerDataChanged();
}
