package com.vitco.engine.data;

/**
 * Functionality for voxel highlighting.
 */
public interface VoxelHighlightInterface {
    // highlight a voxel
    void highlightVoxel(int[] pos);
    // retrieve highlighted voxel
    int[] getHighlightedVoxel();
    // remove all highliths
    void removeVoxelHighlights();
}
