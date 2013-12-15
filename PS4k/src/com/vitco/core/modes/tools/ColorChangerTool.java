package com.vitco.core.modes.tools;

import com.vitco.core.container.DrawContainer;
import com.vitco.core.data.container.Voxel;
import com.vitco.util.misc.ColorTools;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Defines the basic color changer tool.
 */
public class ColorChangerTool extends AbstractVoxelTool {

    // constructor
    public ColorChangerTool(DrawContainer container, int side) {
        super(container, side);
    }

    // --------------------

    // the current texture (fetched onPress)
    private int selectedTexture = -1;

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
        Voxel highlightedVoxel = data.searchVoxel(highlighted, false);

        // get the current texture
        selectedTexture = data.getSelectedTexture();

        // perform action
        if (highlightedVoxel != null) {
            if (selectedTexture != -1) {
                int activeSide = getActiveSide();
                int[] texture = highlightedVoxel.getTexture();
                if (texture != null && texture[activeSide] == selectedTexture) {
                    if (!isMouse3Down()) {
                        // rotate the texture
                        data.rotateVoxelTexture(highlightedVoxel.id, activeSide);
                    } else {
                        // mirror the texture
                        data.flipVoxelTexture(highlightedVoxel.id, activeSide);
                    }
                } else {
                    if (!isMouse3Down() || getCurrentLayer() == highlightedVoxel.getLayerId()) {
                        // replace the texture (side)
                        data.setTexture(highlightedVoxel.id, getActiveSide(), selectedTexture);
                    }
                }
            } else {
                if (!isMouse3Down() || getCurrentLayer() == highlightedVoxel.getLayerId()) {
                    // set the voxel color
                    data.setColor(highlightedVoxel.id, ColorTools.hsbToColor(getCurrentColor()));
                }
            }
        }

        // cancel highlighting
        data.highlightVoxel(null);
    }

    @Override
    protected void release(MouseEvent e) {
        data.highlightVoxel(getVoxelSimple(e.getPoint()));
    }

    @Override
    public void drag(MouseEvent e) {
        int[] highlighted = getVoxelSimple(e.getPoint());
        if (highlighted != null) {
            Voxel highlightedVoxel = data.searchVoxel(highlighted, isMouse3Down());
            if (highlightedVoxel != null) {
                if (selectedTexture != -1) {
                    data.setTexture(highlightedVoxel.id, getActiveSide(), selectedTexture);
                } else {
                    data.setColor(highlightedVoxel.id, ColorTools.hsbToColor(getCurrentColor()));
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

    // previewRect color
    private final int[] selectDashColor = new int[]{Color.BLACK.getRGB(), Color.WHITE.getRGB()};

    // true if mouse3 was pressed when drag started
    private boolean mouse3down = false;

    // --------------------------

    @Override
    protected void shiftMove(MouseEvent e) {
        data.highlightVoxel(getVoxel(e.getPoint(), false));
    }

    @Override
    protected void shiftPress(MouseEvent e) {
        int[] highlighted = data.getHighlightedVoxel();
        // store state
        mouse3down = isMouse3Down();
        // memo selection outline
        initialVoxelPos = highlighted;
        lastVoxelPos = highlighted;
        // set initial preview rect
        data.setOutlineBox("preview", new int[][]{initialVoxelPos, lastVoxelPos, selectDashColor}.clone());
        // no voxel selection
        data.highlightVoxel(null);
    }

    @Override
    protected void shiftRelease(MouseEvent e) {
        // use the voxels to select the new color
        if (lastVoxelPos != null) {
            ArrayList<Integer> list = new ArrayList<Integer>();
            for (int x = Math.min(initialVoxelPos[0], lastVoxelPos[0]),
                         maxx = Math.max(initialVoxelPos[0], lastVoxelPos[0]); x <= maxx; x++ ) {
                for (int y = Math.min(initialVoxelPos[1], lastVoxelPos[1]),
                             maxy = Math.max(initialVoxelPos[1], lastVoxelPos[1]); y <= maxy; y++ ) {
                    for (int z = Math.min(initialVoxelPos[2], lastVoxelPos[2]),
                                 maxz = Math.max(initialVoxelPos[2], lastVoxelPos[2]); z <= maxz; z++ ) {
                        // depending on mouse3 state we only search current layer
                        Voxel voxel = data.searchVoxel(new int[]{x,y,z}, mouse3down);
                        if (voxel != null) {
                            list.add(voxel.id);
                        }
                    }
                }
            }
            // store in array
            Integer[] voxels = new Integer[list.size()];
            list.toArray(voxels);
            // update color / texture
            selectedTexture = data.getSelectedTexture();
            if (selectedTexture != -1) {
                data.massSetTexture(voxels, selectedTexture);
            } else {
                data.massSetColor(voxels, ColorTools.hsbToColor(getCurrentColor()));
            }
        }
        // hide preview
        data.setOutlineBox("preview", null);
    }

    @Override
    protected void shiftDrag(MouseEvent e) {
        lastVoxelPos = getVoxel(e.getPoint(), false);
        // display preview
        if (lastVoxelPos != null && initialVoxelPos != null) {
            data.setOutlineBox("preview", new int[][]{initialVoxelPos, lastVoxelPos, selectDashColor});
        } else {
            data.setOutlineBox("preview", null);
        }
    }

    @Override
    protected void shiftClick(MouseEvent e) {}

    @Override
    protected void singleShiftClick(MouseEvent e) {}

}

