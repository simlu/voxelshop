package com.vitco.frames.engine.data.container;

import com.vitco.util.RTree;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A layer that contains voxels.
 */
public final class VoxelLayer implements Serializable {
    private final static float[] ZEROS = new float[] {0,0,0};
    // holds the voxel data for this layer
    private final RTree<Voxel> voxelIndex = new RTree<Voxel>(50, 2, 3);
    // list of all voxels
    private final ArrayList<Voxel> voxelList = new ArrayList<Voxel>();
    public final int id;
    private String layerName; // layerName of layer
    private boolean visible = true;

    // constructor
    public VoxelLayer(int id, String layerName) {
        this.id = id;
        this.layerName = layerName;
    }

    // check if a position already contains a voxel
    private boolean containsVoxel(float[] pos) {
        return voxelIndex.search(pos, ZEROS).size() > 0;
    }

    // add a voxel iff that position is not already occupied
    public boolean addVoxel(Voxel voxel) {
        boolean result = false;
        if (!containsVoxel(voxel.getPos())) {
            voxel.setLayerId(this.id);
            voxelIndex.insert(voxel.getPos(), ZEROS, voxel);
            voxelList.add(voxel);
            result = true;
        }
        return result;
    }

    // remove a voxel
    public boolean removeVoxel(Voxel voxel) {
        return voxelList.remove(voxel) && voxelIndex.delete(voxel.getPos(), ZEROS, voxel);
    }

    // get all voxels of this layer
    public Voxel[] getVoxels() {
        Voxel[] result = new Voxel[voxelList.size()];
        voxelList.toArray(result);
        return result;
    }

    // set the name of this layer
    public final void setName(String layerName) {
        this.layerName = layerName;
    }

    // get the name of this layer
    public final String getName() {
        return layerName;
    }

    // set the visibility of this layer
    public final void setVisible(boolean b) {
        this.visible = b;
    }

    // get the name of this layer
    public final boolean isVisible() {
        return visible;
    }

}
