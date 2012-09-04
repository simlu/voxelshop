package com.vitco.engine.data;

/**
 * Functionality for voxel highlighting.
 */
public class VoxelHighlight extends VoxelData implements VoxelHighlightInterface {

    private int[] pos = null;

    @Override
    public final void highlightVoxel(int[] pos) {
        if (this.pos == null || pos == null || (this.pos[0] != pos[0] || this.pos[1] != pos[1] || this.pos[2] != pos[2])) {
            if (this.pos != null || pos != null) {
                this.pos = pos != null ? pos.clone() : null;
                notifier.onVoxelSelectionChanged();
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
            notifier.onVoxelSelectionChanged();
        }
    }

}
