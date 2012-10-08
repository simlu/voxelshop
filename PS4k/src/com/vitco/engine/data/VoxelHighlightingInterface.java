package com.vitco.engine.data;

/**
 * Functionality for voxel highlighting and selection of voxels.
 */
public interface VoxelHighlightingInterface {
    // highlight a voxel
    void highlightVoxel(int[] pos);
    // retrieve highlighted voxel
    int[] getHighlightedVoxel();
    // remove all highliths
    void removeVoxelHighlights();

    Integer[] getVoxelSelectionShift();

    void setVoxelSelectionShift(int x, int y, int z);
}
