package com.vitco.low.engine;

import java.awt.*;

/**
 * This describes a voxel type (i.e. a block with very specific properties).
 */
public class VoxelType {
    protected int uId = 0;
    protected int usedCount = 0;


    // -----------------


    public final Color color;

    public VoxelType(Color color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VoxelType voxelType = (VoxelType) o;

        return !(color != null ? !color.equals(voxelType.color) : voxelType.color != null);

    }

    @Override
    public int hashCode() {
        return color != null ? color.hashCode() : 0;
    }
}
