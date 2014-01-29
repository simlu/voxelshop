package com.vitco.core.modes.tools;

import com.vitco.core.container.DrawContainer;
import com.vitco.core.data.container.Voxel;
import com.vitco.util.misc.ColorTools;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Defines the basic draw tool.
 */
public class DrawTool extends AbstractVoxelTool {

    // constructor
    public DrawTool(DrawContainer container, int side) {
        super(container, side);
    }

    // --------------------

    // the current texture (fetched onPress)
    private int[] texture = null;

    // --------------------

    @Override
    protected void key() {}

    // --------------------------
    // simple drawing
    // --------------------------



    @Override
    public void move(MouseEvent e) {
        if (!isMouseDown()) {
            data.highlightVoxel(getVoxel(e.getPoint(), true));
        }
    }

    @Override
    public void press(MouseEvent e) {

        int[] highlighted = data.getHighlightedVoxel();
        // get the center
        setActiveCenter(highlighted);

        // reselect voxel if this is delete
        if (isMouse3Down()) {
            highlighted = getVoxel(e.getPoint(), false);
            if (highlighted != null) {
                // update center
                setActiveCenter(highlighted);
            }
        }

        // valid selected voxel position (slot)
        if (highlighted != null) {
            Voxel voxel = data.searchVoxel(highlighted, false);
            // get the current texture
            int selectedTexture = data.getSelectedTexture();
            texture = selectedTexture == -1 ? null : new int[] {
                    selectedTexture, selectedTexture, selectedTexture,
                    selectedTexture, selectedTexture, selectedTexture
            };

            // perform action
            if (voxel == null) {
                // place voxel
                if (!isMouse3Down()) {
                    if (isLayerVisible()) {
                        data.addVoxel(ColorTools.hsbToColor(getCurrentColor()), texture, highlighted);
                    }
                }
            } else {
                // delete voxel
                if (isMouse3Down()) {
                    data.removeVoxel(voxel.id);
                }
            }

            // cancel highlighting
            data.highlightVoxel(null);
        }
    }

    @Override
    protected void release(MouseEvent e) {}

    @Override
    public void drag(MouseEvent e) {
        int[] voxelPos;
        if (side == -1) {
            if (isMouse3Down()) {
                // hit test with prev plane
                voxelPos = getVoxelUsePlanePrev(e.getPoint());
            } else {
                // hit test with next plane
                voxelPos = getVoxelUsePlaneNext(e.getPoint());
            }
        } else {
            voxelPos = getVoxel(e.getPoint(), !isMouse3Down());
        }
        if (voxelPos != null) {
            Voxel voxel = data.searchVoxel(voxelPos, false);
            if (voxel == null) {
                if (!isMouse3Down()) {
                    if (isLayerVisible()) {
                        data.addVoxel(ColorTools.hsbToColor(getCurrentColor()), texture, voxelPos);
                    }
                }
            } else {
                if (isMouse3Down()) {
                    data.removeVoxel(voxel.id);
                }
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
    private final int[] addDashColor = new int[]{Color.BLACK.getRGB(), Color.WHITE.getRGB()};
    private final int[] eraseDashColor = new int[]{Color.BLACK.getRGB(), Color.RED.getRGB()};

    // --------------------------

    @Override
    protected void shiftMove(MouseEvent e) {
        data.highlightVoxel(getVoxel(e.getPoint(), true));
    }

    @Override
    protected void shiftPress(MouseEvent e) {
        // store "erase" state information
        mouse3Down = isMouse3Down();
        // select depending on erase status
        int[] highlighted = getVoxel(e.getPoint(), !mouse3Down);
        // memo selection outline
        initialVoxelPos = highlighted;
        lastVoxelPos = highlighted;
        // set initial preview rect
        data.setOutlineBox("preview", new int[][]{initialVoxelPos, lastVoxelPos,
                mouse3Down ? eraseDashColor : addDashColor}.clone());
        // no voxel selection
        data.highlightVoxel(null);
    }

    @Override
    protected void shiftRelease(MouseEvent e) {
        // fill/erase this area
        if (lastVoxelPos != null) {
            int selectedLayer = data.getSelectedLayer();
            Color color = ColorTools.hsbToColor(getCurrentColor());
            ArrayList<Voxel> listAdd = new ArrayList<Voxel>();
            ArrayList<Integer> listRem = new ArrayList<Integer>();
            for (int x = Math.min(initialVoxelPos[0], lastVoxelPos[0]),
                         maxx = Math.max(initialVoxelPos[0], lastVoxelPos[0]); x <= maxx; x++ ) {
                for (int y = Math.min(initialVoxelPos[1], lastVoxelPos[1]),
                             maxy = Math.max(initialVoxelPos[1], lastVoxelPos[1]); y <= maxy; y++ ) {
                    for (int z = Math.min(initialVoxelPos[2], lastVoxelPos[2]),
                                 maxz = Math.max(initialVoxelPos[2], lastVoxelPos[2]); z <= maxz; z++ ) {
                        int[] pos = new int[]{x,y,z};
                        Voxel voxel = data.searchVoxel(pos, false);
                        if (mouse3Down && voxel != null) {
                            listRem.add(voxel.id);
                        } else if (!mouse3Down && voxel == null) {
                            listAdd.add(new Voxel(0, pos, color, false, texture, selectedLayer));
                        }
                    }
                }
            }
            if (mouse3Down) {
                Integer[] voxels = new Integer[listRem.size()];
                listRem.toArray(voxels);
                data.massRemoveVoxel(voxels);
            } else {
                Voxel[] voxels = new Voxel[listAdd.size()];
                listAdd.toArray(voxels);
                if (isLayerVisible()) {
                    data.massAddVoxel(voxels);
                }
            }
        }
        // hide preview
        data.setOutlineBox("preview", null);
    }

    @Override
    protected void shiftDrag(MouseEvent e) {
        lastVoxelPos = getVoxel(e.getPoint(), !mouse3Down);
        // display preview
        if (lastVoxelPos != null && initialVoxelPos != null) {
            data.setOutlineBox("preview", new int[][]{initialVoxelPos, lastVoxelPos,
                    mouse3Down ? eraseDashColor : addDashColor});
        } else {
            data.setOutlineBox("preview", null);
        }
    }

    @Override
    protected void shiftClick(MouseEvent e) {}

    @Override
    protected void singleShiftClick(MouseEvent e) {}

}
