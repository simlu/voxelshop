package com.vitco.engine.data;

import com.vitco.engine.data.container.VOXELMODE;

import java.awt.*;

/**
 * Manages everything that has to do with general data
 */
public interface GeneralDataInterface {

    // retrieve the currently selected color
    Color getCurrentColor();
    // set the currently selected color
    boolean setCurrentColor(Color color);
    // set the voxel mode (eg. VIEW, DRAW,...)
    boolean setVoxelMode(VOXELMODE mode);
    // get the current voxel mode
    VOXELMODE getVoxelMode();
    // set the mode (Animation OR Voxel)
    boolean setAnimate(boolean animate);
    // true iff the current mode is "Animation"
    boolean isAnimate();
    // true iff the data has changed since last reset (triggered by save to file)
    boolean hasChanged();
    // set the preview plane (attached to highlighted voxel)
    void setPreviewPlane(int i);
    // retrieve the preview plane
    int getPreviewPlane();
    // reset the changed of data (nothing has changed after this is called)
    void resetHasChanged();
}
