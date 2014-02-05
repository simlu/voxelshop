package com.vitco.core.modes.tools;

import com.vitco.core.container.DrawContainer;
import com.vitco.core.data.container.Voxel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Defines the basic erase tool.
 */
public class EraseTool extends AbstractVoxelTool {

    // constructor
    public EraseTool(DrawContainer container, int side) {
        super(container, side);
    }

    // --------------------

    @Override
    protected void key() {}

    // --------------------------
    // simple drawing
    // --------------------------

    @Override
    public void move(MouseEvent e) {
        data.highlightVoxel(getVoxelSimple(e.getPoint()));
    }

    @Override
    public void press(MouseEvent e) {
        int[] highlighted = data.getHighlightedVoxel();

        setActiveCenter(highlighted);

        // mouse3 ~ erase only same layer
        Voxel voxel = data.searchVoxel(highlighted, isMouse3Down());

        // perform action
        if (voxel != null) {
            data.removeVoxel(voxel.id);
        }

        // cancel highlighting
        data.highlightVoxel(null);
    }

    @Override
    protected void release(MouseEvent e) {}

    @Override
    public void drag(MouseEvent e) {
        int[] voxelPos;
        // simple hit test
        voxelPos = getVoxelUsePlanePrev(e.getPoint());
        // handle remove
        if (voxelPos != null) {
            Voxel voxel = data.searchVoxel(voxelPos, isMouse3Down());
            if (voxel != null) {
                data.removeVoxel(voxel.id);
            }
        }
    }

    @Override
    protected void click(MouseEvent e) {}

    @Override
    protected void singleClick(MouseEvent e) {}

    // --------------------------
    // shift drawing
    // --------------------------

    // last active voxel position
    private int[] initialVoxelPos = null;

    private int[] lastVoxelPos = null;

    // true if this is add event
    private boolean mouse3Down = false;

    // previewRect color
    private final int[] eraseDashColor = new int[]{Color.BLACK.getRGB(), Color.RED.getRGB()};

    // --------------------------

    @Override
    protected void shiftMove(MouseEvent e) {
        data.highlightVoxel(getVoxel(e.getPoint(), false));
    }

    @Override
    protected void shiftPress(MouseEvent e) {
        int[] highlighted = data.getHighlightedVoxel();
        // store "erase" state information
        mouse3Down = isMouse3Down();
        // memo selection outline
        initialVoxelPos = highlighted;
        lastVoxelPos = highlighted;
        // set initial preview rect
        data.setOutlineBox("preview", new int[][]{initialVoxelPos, lastVoxelPos, eraseDashColor}.clone());
        // no voxel selection
        data.highlightVoxel(null);
    }

    @Override
    protected void shiftRelease(MouseEvent e) {
        // erase this area
        if (lastVoxelPos != null && initialVoxelPos != null) {
            ArrayList<Integer> listRem = new ArrayList<Integer>();
            for (int x = Math.min(initialVoxelPos[0], lastVoxelPos[0]),
                         maxx = Math.max(initialVoxelPos[0], lastVoxelPos[0]); x <= maxx; x++ ) {
                for (int y = Math.min(initialVoxelPos[1], lastVoxelPos[1]),
                             maxy = Math.max(initialVoxelPos[1], lastVoxelPos[1]); y <= maxy; y++ ) {
                    for (int z = Math.min(initialVoxelPos[2], lastVoxelPos[2]),
                                 maxz = Math.max(initialVoxelPos[2], lastVoxelPos[2]); z <= maxz; z++ ) {
                        // depending on mouse3 state we only search current layer
                        Voxel voxel = data.searchVoxel(new int[]{x,y,z}, mouse3Down);
                        if (voxel != null) {
                            listRem.add(voxel.id);
                        }
                    }
                }
            }
            Integer[] voxels = new Integer[listRem.size()];
            listRem.toArray(voxels);
            data.massRemoveVoxel(voxels);
        }
        // hide preview
        data.setOutlineBox("preview", null);
    }

    @Override
    protected void shiftDrag(MouseEvent e) {
        lastVoxelPos = getVoxel(e.getPoint(), false);
        // display preview
        if (lastVoxelPos != null && initialVoxelPos != null) {
            data.setOutlineBox("preview", new int[][]{initialVoxelPos, lastVoxelPos, eraseDashColor});
        } else {
            data.setOutlineBox("preview", null);
        }
    }

    @Override
    protected void shiftClick(MouseEvent e) {}

    @Override
    protected void singleShiftClick(MouseEvent e) {}

}

