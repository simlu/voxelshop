package com.vitco.engine.adapter.tools;

import com.vitco.engine.data.container.Voxel;
import com.vitco.engine.view.DrawContainer;
import com.vitco.util.ColorTools;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashSet;

/**
 * Defines the basic color picker tool.
 */
public class PickerTool extends AbstractVoxelTool {

    // constructor
    public PickerTool(DrawContainer container, int side) {
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
        Voxel highlightedVoxel = data.searchVoxel(highlighted, isMouse3Down());
        // perform action
        if (highlightedVoxel != null) {
            int[] textureIds = highlightedVoxel.getTexture();
            if (textureIds == null) {
                preferences.storeObject("currently_used_color",
                        ColorTools.colorToHSB(highlightedVoxel.getColor()));
            }
            data.selectTextureSoft(textureIds == null ? -1 : textureIds[container.getLastActiveSide()]);
        }

        // cancel highlighting
        data.highlightVoxel(null);
    }

    @Override
    protected void release(MouseEvent e) {
        data.highlightVoxel(getVoxelSimple(e.getPoint()));
    }

    @Override
    public void drag(MouseEvent e) {}

    @Override
    protected void click(MouseEvent e) {}

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
            int r = 0, g = 0, b = 0, count = 0;
            HashSet<Integer> colors = new HashSet<Integer>();
            for (int x = Math.min(initialVoxelPos[0], lastVoxelPos[0]),
                         maxx = Math.max(initialVoxelPos[0], lastVoxelPos[0]); x <= maxx; x++ ) {
                for (int y = Math.min(initialVoxelPos[1], lastVoxelPos[1]),
                             maxy = Math.max(initialVoxelPos[1], lastVoxelPos[1]); y <= maxy; y++ ) {
                    for (int z = Math.min(initialVoxelPos[2], lastVoxelPos[2]),
                                 maxz = Math.max(initialVoxelPos[2], lastVoxelPos[2]); z <= maxz; z++ ) {
                        // depending on mouse3 state we only search current layer
                        Voxel voxel = data.searchVoxel(new int[]{x,y,z}, false);
                        if (voxel != null && voxel.getTexture() == null) {
                            // right click (mouse3) only considers unique colors
                            Color color = voxel.getColor();
                            if (!mouse3down || colors.add(color.getRGB())) {
                                r += color.getRed();
                                g += color.getGreen();
                                b += color.getBlue();
                                count++;
                            }
                        }
                    }
                }
            }
            // set the color
            if (count > 0) {
                preferences.storeObject("currently_used_color",
                        ColorTools.colorToHSB(new Color(
                                Math.min(255, Math.max(0, r / count)),
                                Math.min(255,Math.max(0,g/count)),
                                Math.min(255,Math.max(0,b/count))
                        )));
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

}

