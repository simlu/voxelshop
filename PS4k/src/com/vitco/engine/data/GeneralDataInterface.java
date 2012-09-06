package com.vitco.engine.data;

import com.vitco.engine.data.container.VOXELMODE;

import java.awt.*;

/**
 * Manages everything that has to do with general data
 */
public interface GeneralDataInterface {
    Color getCurrentColor();

    boolean setCurrentColor(Color color);

    boolean setVoxelMode(VOXELMODE mode);

    VOXELMODE getVoxelMode();

    boolean setAnimate(boolean animate);

    boolean isAnimate();

    boolean hasChanged();
}
