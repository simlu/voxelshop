package com.vitco.engine.data;

import com.vitco.res.VitcoSettings;

import java.awt.*;
import java.util.HashMap;

/**
 * Functionality for voxel highlighting and selection of voxels.
 */
public class VoxelHighlighting extends VoxelData implements VoxelHighlightingInterface {

    private int[] pos = null;

    @Override
    public final void highlightVoxel(int[] pos) {
        synchronized (VitcoSettings.SYNC) {
            if (this.pos == null || pos == null || (this.pos[0] != pos[0] || this.pos[1] != pos[1] || this.pos[2] != pos[2])) {
                if (this.pos != null || pos != null) {
                    this.pos = pos != null ? pos.clone() : null;
                    notifier.onVoxelHighlightingChanged();
                }
            }
        }
    }

    @Override
    public final int[] getHighlightedVoxel() {
        synchronized (VitcoSettings.SYNC) {
            return pos != null ? pos.clone() : null;
        }
    }

    @Override
    public final void removeVoxelHighlights() {
        synchronized (VitcoSettings.SYNC) {
            if (pos != null) {
                pos = null;
                notifier.onVoxelHighlightingChanged();
            }
        }
    }

    // ================================
    // set selection boxes
    private final HashMap<String, int[][]> boxOutlines = new HashMap<String, int[][]>();
    private int[][][] boxOutlinesArray = new int[0][][];

    @Override
    public final void setOutlineBox(String key, int[][] rect) {
        synchronized (VitcoSettings.SYNC) {
            if (rect == null) {
                boxOutlines.remove(key);
            } else {
                boxOutlines.put(key, rect);
            }
            // convert to array
            if (boxOutlinesArray.length != boxOutlines.size()) {
                boxOutlinesArray = new int[boxOutlines.size()][][];
            }
            boxOutlines.values().toArray(boxOutlinesArray);
            notifier.onOutlineBoxesChanged();
        }
    }

    @Override
    public final int[][][] getOutlineBoxes() {
        synchronized (VitcoSettings.SYNC) {
            return boxOutlinesArray.clone();
        }
    }

    // ================================
    // set selection rectangles
    private Rectangle selectionRect = null;

    @Override
    public Rectangle getSelectionRect() {
        synchronized (VitcoSettings.SYNC) {
            return selectionRect;
        }
    }

    @Override
    public void setSelectionRect(Rectangle selectionRect) {
        synchronized (VitcoSettings.SYNC) {
            this.selectionRect = selectionRect;
            notifier.onSelectionRectChanged();
        }
    }

    // ================================
    // shifting of selected voxels (selection tool)

    private final int[] voxelSelectionShift = new int[] {0,0,0};

    @Override
    public final void setVoxelSelectionShift(int x, int y, int z) {
        synchronized (VitcoSettings.SYNC) {
            if (voxelSelectionShift[0] != x || voxelSelectionShift[1] != y || voxelSelectionShift[2] != z) {
                voxelSelectionShift[0] = x;
                voxelSelectionShift[1] = y;
                voxelSelectionShift[2] = z;
                notifier.onVoxelSelectionShiftChanged();
            }
        }
    }

    @Override
    public final int[] getVoxelSelectionShift() {
        synchronized (VitcoSettings.SYNC) {
            return voxelSelectionShift.clone();
        }
    }
}
