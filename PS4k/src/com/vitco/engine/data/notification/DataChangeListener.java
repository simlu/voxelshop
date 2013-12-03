package com.vitco.engine.data.notification;

/**
 * A data change listener interface for the whole data container.
 */
public interface DataChangeListener {
    void onAnimationDataChanged();
    void onAnimationSelectionChanged();
    void onVoxelDataChanged();
    void onVoxelHighlightingChanged();
    void onVoxelSelectionShiftChanged();
    void onTextureDataChanged();
    void onOutlineBoxesChanged();
    void onSelectionRectChanged();
    void onLayerStateChanged();
}
