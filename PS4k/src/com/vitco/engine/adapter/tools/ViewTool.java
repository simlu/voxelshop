package com.vitco.engine.adapter.tools;

import com.vitco.engine.view.DrawContainer;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 * Defines the basic view tool.
 */
public class ViewTool extends AbstractVoxelTool {
    public ViewTool(DrawContainer container, int side) {
        super(container, side);
        actionManager.registerActionIsUsed("center_main_view_camera");
    }

    @Override
    protected void move(MouseEvent e) {}

    @Override
    protected void press(MouseEvent e) {}

    @Override
    protected void release(MouseEvent e) {}

    @Override
    protected void drag(MouseEvent e) {}

    @Override
    protected void click(MouseEvent e) {}

    @Override
    protected void shiftMove(MouseEvent e) {
        data.highlightVoxel(getVoxel(e.getPoint(), false));
    }

    @Override
    protected void shiftPress(MouseEvent e) {
        int[] highlighted = getVoxel(e.getPoint(), false);
        if (highlighted != null) {
            preferences.storeInteger("currentplane_sideview1", highlighted[2]);
            preferences.storeInteger("currentplane_sideview2", highlighted[1]);
            preferences.storeInteger("currentplane_sideview3", highlighted[0]);
            if (isMouse3Down()) {
                actionManager.getAction("center_main_view_camera")
                        .actionPerformed(new ActionEvent(e.getSource(), e.getID(), e.paramString()));
            }
        }
    }

    @Override
    protected void shiftRelease(MouseEvent e) {}

    @Override
    protected void shiftDrag(MouseEvent e) {}

    @Override
    protected void shiftClick(MouseEvent e) {}

    @Override
    protected void key() {
        if (!isShiftDown()) {
            data.highlightVoxel(null);
        }
    }

}
