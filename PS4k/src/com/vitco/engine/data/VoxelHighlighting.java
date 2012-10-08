package com.vitco.engine.data;

/**
 * Functionality for voxel highlighting and selection of voxels.
 */
public class VoxelHighlighting extends VoxelData implements VoxelHighlightingInterface {

    private int[] pos = null;

    @Override
    public final void highlightVoxel(int[] pos) {
        if (this.pos == null || pos == null || (this.pos[0] != pos[0] || this.pos[1] != pos[1] || this.pos[2] != pos[2])) {
            if (this.pos != null || pos != null) {
                this.pos = pos != null ? pos.clone() : null;
                notifier.onVoxelHighlightingChanged();
            }
        }
    }

    @Override
    public final int[] getHighlightedVoxel() {
        return pos != null ? pos.clone() : null;
    }

    @Override
    public final void removeVoxelHighlights() {
        if (pos != null) {
            pos = null;
            notifier.onVoxelHighlightingChanged();
        }
    }

    // ================================
    // shifting of selected voxels (selection tool)

    private final Integer[] voxelSelectionShift = new Integer[] {0,0,0};

    @Override
    public final void setVoxelSelectionShift(int x, int y, int z) {
        if (voxelSelectionShift[0] != x || voxelSelectionShift[1] != y || voxelSelectionShift[2] != z) {
            voxelSelectionShift[0] = x;
            voxelSelectionShift[1] = y;
            voxelSelectionShift[2] = z;
            notifier.onVoxelSelectionShiftChanged();
        }
    }

    @Override
    public final Integer[] getVoxelSelectionShift() {
        return voxelSelectionShift.clone();
    }
}
