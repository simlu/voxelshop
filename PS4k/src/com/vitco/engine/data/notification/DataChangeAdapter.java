package com.vitco.engine.data.notification;

/**
 * Like DataChangeListener but overwrites all methods.
 */
public abstract class DataChangeAdapter implements DataChangeListener {

    @Override
    public void onAnimationDataChanged() {}

    @Override
    public void onAnimationSelectionChanged() {}

    @Override
    public void onVoxelDataChanged() {}

    @Override
    public void onVoxelSelectionChanged() {}

    @Override
    public void onColorDataChanged() {}

    @Override
    public void onVoxelModeChanged() {}

    @Override
    public void onAnimateChanged() {}

    @Override
    public void onPreviewPlaneChanged() {}
}
