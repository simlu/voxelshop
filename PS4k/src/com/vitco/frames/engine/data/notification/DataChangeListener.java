package com.vitco.frames.engine.data.notification;

/**
 * A data change listener interface for the whole data container.
 */
public interface DataChangeListener {
    void onAnimationDataChanged();
    void onAnimationSelectionChanged();
    void onVoxelDataChanged();
    void onLayerDataChanged();
}
