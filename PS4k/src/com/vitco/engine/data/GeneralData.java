package com.vitco.engine.data;

import com.vitco.engine.data.container.DataContainer;
import com.vitco.engine.data.container.VOXELMODE;
import com.vitco.engine.data.notification.DataChangeAdapter;

import java.awt.*;
import java.util.ArrayList;

/**
 * Manages everything that has to do with general data
 */
public class GeneralData extends ListenerData implements GeneralDataInterface {

    // main data container
    protected DataContainer dataContainer = new DataContainer();

    // ######################
    protected ArrayList<Color> USED_COLORS = new ArrayList<Color>(); //todo use this!
    protected Color CURRENT_COLOR = new Color(193, 124, 50);

    // the mode of voxel drawing
    protected VOXELMODE TOOL_MODE = VOXELMODE.VIEW;

    // true if we are dealing with animation (not voxel)
    protected boolean STATE_ANIMATE = false;

    // true if the data has changed since last save
    protected boolean hasChanged = false;

    // the preview plane (-1 if none)
    protected int PREVIEW_PLANE = -1;

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

    @Override
    public void setPreviewPlane(int i) {
        if (PREVIEW_PLANE != i) {
            PREVIEW_PLANE = Math.max(-1, Math.min(2, i));
            notifier.onPreviewPlaneChanged();
        }
    }

    @Override
    public int getPreviewPlane() {
        return PREVIEW_PLANE;
    }

    @Override
    public boolean hasChanged() {
        return hasChanged;
    }

    @Override
    public void resetHasChanged() {
        hasChanged = false;
    }

    @Override
    public Color getCurrentColor() {
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
