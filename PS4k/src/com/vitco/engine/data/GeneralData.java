package com.vitco.engine.data;

import com.vitco.engine.data.container.VOXELMODE;
import com.vitco.engine.data.notification.DataChangeAdapter;

import java.awt.*;
import java.util.ArrayList;

/**
 * Manages everything that has to do with general data
 */
public class GeneralData extends ListenerData implements GeneralDataInterface {

    // ######################
    protected ArrayList<Color> USED_COLORS = new ArrayList<Color>(); //todo use this!
    protected Color CURRENT_COLOR = new Color(193, 124, 50);

    protected VOXELMODE TOOL_MODE = VOXELMODE.VIEW;

    // true if we are dealing with animation (not voxel)
    protected boolean STATE_ANIMATE = false;

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
    public Color getCURRENT_COLOR() {
        return CURRENT_COLOR;
    }

    @Override
    public boolean setCurrentColor(Color color) {
        boolean result = false;
        if (CURRENT_COLOR != color) {
            CURRENT_COLOR = color;
            notifier.onColorDataChanged();
            result = true;
        }
        return result;
    }

    @Override
    public boolean setVoxelMode(VOXELMODE mode) {
        boolean result = false;
        if (this.TOOL_MODE != mode) {
            this.TOOL_MODE = mode;
            notifier.onVoxelModeChanged();
            result = true;
        }
        return result;
    }

    @Override
    public VOXELMODE getVoxelMode() {
        return TOOL_MODE;
    }

    @Override
    public boolean setAnimate(boolean animate) {
        boolean result = false;
        if (this.STATE_ANIMATE != animate) {
            this.STATE_ANIMATE = animate;
            notifier.onAnimateChanged();
            result = true;
        }
        return result;
    }

    @Override
    public boolean isAnimate() {
        return STATE_ANIMATE;
    }


}
