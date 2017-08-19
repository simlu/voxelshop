package com.vitco.app.core.data;

import java.awt.*;

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

    int[] getVoxelSelectionShift();

    void setVoxelSelectionShift(int x, int y, int z);

    void setOutlineBox(String key, int[][] rect);

    int[][][] getOutlineBoxes();

    void setSelectionRect(Rectangle selectionRect);

    Rectangle getSelectionRect();
}
