package com.vitco.engine.data;

import com.vitco.engine.data.container.DataContainer;

import java.awt.*;

/**
 * Manages everything that has to do with general data
 */
public class GeneralData extends ListenerData implements GeneralDataInterface {
    @Override
    public Color getCurrentColor() {
        return dataContainer.currentColor;
    }

    @Override
    public boolean setCurrentColor(Color color) {
        boolean result = false;
        if (dataContainer.currentColor != color) {
            dataContainer.currentColor = color;
            notifier.onColorDataChanged();
            result = true;
        }
        return result;
    }

    @Override
    public boolean setVoxelMode(DataContainer.VOXELMODE mode) {
        boolean result = false;
        if (dataContainer.mode != mode) {
            dataContainer.mode = mode;
            result = true;
        }
        return result;
    }

    @Override
    public DataContainer.VOXELMODE getVoxelMode() {
        return dataContainer.mode;
    }

}
