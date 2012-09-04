package com.vitco.engine.data.container;

import com.vitco.util.RTree;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A layer that contains voxels.
 *
 * Does not check for duplicates on insertion!
 */
public final class VoxelLayer implements Serializable {
    private static final long serialVersionUID = 1L;
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

    public Voxel[] search(float[] pos, float[] range) {
        List<Voxel> search = voxelIndex.search(pos,range);
        Voxel[] result = new Voxel[search.size()];
        search.toArray(result);
        return result;
    }

    public Voxel[] search(int[] pos, int radius) {
        List<Voxel> search = voxelIndex.search(
                new float[]{pos[0] - radius, pos[1] - radius, pos[2] - radius},
                new float[]{radius*2, radius*2, radius*2}
        );
        Voxel[] result = new Voxel[search.size()];
        search.toArray(result);
        return result;
    }

    public int getSize() {
        return voxelList.size();
    }

    // check if a position already contains a voxel
    public boolean containsVoxel(int[] pos) {
        return voxelIndex.search(new float[]{pos[0], pos[1], pos[2]}, ZEROS).size() > 0;
    }

    // add a voxel iff that position is not already occupied
    public void addVoxel(Voxel voxel) {
        assert !containsVoxel(voxel.getPosAsInt());
        voxelIndex.insert(voxel.getPosAsFloat(), ZEROS, voxel);
        voxelList.add(voxel);
    }

    // set the color of a voxel
    public void setVoxelColor(Voxel voxel, Color color) {
        voxel.setColor(color);
    }

    // set the alpha of a voxel
    public void setVoxelAlpha(Voxel voxel, int alpha) {
        voxel.setAlpha(alpha);
    }

    // remove a voxel
    public boolean removeVoxel(Voxel voxel) {
        return voxelList.remove(voxel) && voxelIndex.delete(voxel.getPosAsFloat(), ZEROS, voxel);
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
