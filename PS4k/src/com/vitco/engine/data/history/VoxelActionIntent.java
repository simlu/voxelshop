package com.vitco.engine.data.history;

/**
 * Basic action intent for Voxels
 */
public abstract class VoxelActionIntent extends BasicActionIntent {
    protected VoxelActionIntent(boolean attach) {
        super(attach);
    }

    // returns the affected voxel positions
    public abstract int[][] effected();
}
