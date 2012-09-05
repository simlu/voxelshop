package com.vitco.engine.data;

import com.vitco.engine.data.container.DataContainer;

import java.awt.*;

/**
 * Manages everything that has to do with general data
 */
public interface GeneralDataInterface {
    Color getCurrentColor();

    boolean setCurrentColor(Color color);

    boolean setVoxelMode(DataContainer.VOXELMODE mode);

    DataContainer.VOXELMODE getVoxelMode();
}
