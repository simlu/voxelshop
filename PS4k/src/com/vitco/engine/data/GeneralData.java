package com.vitco.engine.data;

import com.vitco.engine.data.container.DataContainer;
import com.vitco.engine.data.notification.DataChangeAdapter;

import java.awt.*;

/**
 * Manages everything that has to do with general data
 */
public class GeneralData extends ListenerData implements GeneralDataInterface {
    public GeneralData() {
        this.addDataChangeListener(new DataChangeAdapter() {
            @Override
            public void onVoxelDataChanged() {
                hasChanged = true;
            }

            @Override
            public void onAnimationDataChanged() {
                hasChanged = true;
            }
        });
    }

    // true if the data has changed since last save
    protected boolean hasChanged = false;

    @Override
    public boolean hasChanged() {
        return hasChanged;
    }

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
            notifier.onVoxelModeChanged();
            result = true;
        }
        return result;
    }

    @Override
    public DataContainer.VOXELMODE getVoxelMode() {
        return dataContainer.mode;
    }

    @Override
    public boolean setAnimate(boolean animate) {
        boolean result = false;
        if (dataContainer.animate != animate) {
            dataContainer.animate = animate;
            notifier.onAnimateChanged();
            result = true;
        }
        return result;
    }

    @Override
    public boolean isAnimate() {
        return dataContainer.animate;
    }


}
