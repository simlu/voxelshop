package com.vitco.core.data.container;

import com.vitco.low.CubeIndexer;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A layer that contains voxels.
 *
 * Does not check for duplicates on insertion!
 */
public final class VoxelLayer implements Serializable {
    private static final long serialVersionUID = 1L;

    // list of all voxels
    private final ArrayList<Voxel> voxelList = new ArrayList<Voxel>();
    // string "index"
    private transient HashMap<Integer, Voxel> index = new HashMap<Integer, Voxel>();
    // side index
    private transient HashMap<Integer, HashSet<Voxel>> sideX = new HashMap<Integer, HashSet<Voxel>>();
    private transient HashMap<Integer, HashSet<Voxel>> sideY = new HashMap<Integer, HashSet<Voxel>>();
    private transient HashMap<Integer, HashSet<Voxel>> sideZ = new HashMap<Integer, HashSet<Voxel>>();

    public final int id;
    private String layerName; // layerName of layer
    private boolean visible = true;

    // constructor
    public VoxelLayer(int id, String layerName) {
        this.id = id;
        this.layerName = layerName;
    }

    // helper to add voxel to index
    private void indexVoxel(Voxel voxel) {
        index.put(voxel.posId, voxel);

        // side x
        HashSet<Voxel> planeX = sideX.get(voxel.x);
        if (planeX == null) {
            planeX = new HashSet<Voxel>();
            sideX.put(voxel.x, planeX);
        }
        planeX.add(voxel);
        // side y
        HashSet<Voxel> planeY = sideY.get(voxel.y);
        if (planeY == null) {
            planeY = new HashSet<Voxel>();
            sideY.put(voxel.y, planeY);
        }
        planeY.add(voxel);
        // side z
        HashSet<Voxel> planeZ = sideZ.get(voxel.z);
        if (planeZ == null) {
            planeZ = new HashSet<Voxel>();
            sideZ.put(voxel.z, planeZ);
        }
        planeZ.add(voxel);
    }

    private boolean unindexVoxel(Voxel voxel) {
        boolean result = true;

        if (null == index.remove(voxel.posId)) {
            result = false;
        }

        // side x
        HashSet<Voxel> planeX = sideX.get(voxel.x);
        if (planeX != null) {
            planeX.remove(voxel);
            if (planeX.isEmpty()) {
                sideX.remove(voxel.x);
            }
        } else {
            result = false; // should never happen
        }
        // side y
        HashSet<Voxel> planeY = sideY.get(voxel.y);
        if (planeY != null) {
            planeY.remove(voxel);
            if (planeY.isEmpty()) {
                sideY.remove(voxel.y);
            }
        } else {
            result = false; // should never happen
        }
        // side z
        HashSet<Voxel> planeZ = sideZ.get(voxel.z);
        if (planeZ != null) {
            planeZ.remove(voxel);
            if (planeZ.isEmpty()) {
                sideZ.remove(voxel.z);
            }
        } else {
            result = false; // should never happen
        }

        return result;
    }

    public final Voxel[] getXPlane(int plane) {
        HashSet<Voxel> planeX = sideX.get(plane);
        if (planeX != null) {
            Voxel[] result = new Voxel[planeX.size()];
            planeX.toArray(result);
            return result;
        } else {
            return new Voxel[0];
        }
    }

    public final Voxel[] getYPlane(int plane) {
        HashSet<Voxel> planeY = sideY.get(plane);
        if (planeY != null) {
            Voxel[] result = new Voxel[planeY.size()];
            planeY.toArray(result);
            return result;
        } else {
            return new Voxel[0];
        }
    }

    public final Voxel[] getZPlane(int plane) {
        HashSet<Voxel> planeZ = sideZ.get(plane);
        if (planeZ != null) {
            Voxel[] result = new Voxel[planeZ.size()];
            planeZ.toArray(result);
            return result;
        } else {
            return new Voxel[0];
        }
    }

    // called after deserialization
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // build the indices when loading from file
        if (sideX == null) {
            sideX = new HashMap<Integer, HashSet<Voxel>>();
        }
        if (sideY == null) {
            sideY = new HashMap<Integer, HashSet<Voxel>>();
        }
        if (sideZ == null) {
            sideZ = new HashMap<Integer, HashSet<Voxel>>();
        }
        if (index == null) {
            index = new HashMap<Integer, Voxel>();
        }
        for (Voxel voxel : voxelList) {
            indexVoxel(voxel);
        }
    }

    public Voxel search(int[] pos) {
        return index.get(CubeIndexer.getId(pos[0], pos[1], pos[2]));
    }

    // search position by using another voxel as reference
    public Voxel search(Voxel voxel) {
        return index.get(voxel.posId);
    }

    public int getSize() {
        return voxelList.size();
    }

    // check if a position already contains a voxel
    public boolean voxelPositionFree(int[] pos) {
        return !index.containsKey(CubeIndexer.getId(pos[0], pos[1], pos[2]));
    }

    // check position by using another voxel
    public boolean voxelPositionFree(Voxel voxel) {
        return !index.containsKey(voxel.posId);
    }

    // add a voxel iff that position is not already occupied
    public void addVoxel(Voxel voxel) {
        assert voxelPositionFree(voxel);
        indexVoxel(voxel);
        voxelList.add(voxel);
    }

    // set the color of a voxel
    public final void setVoxelColor(Voxel voxel, Color color) {
        voxel.setColor(color);
    }

    // set the alpha of a voxel
    public final void setVoxelAlpha(Voxel voxel, int alpha) {
        voxel.setAlpha(alpha);
    }

    // remove a voxel
    public final boolean removeVoxel(Voxel voxel) {
        return voxelList.remove(voxel) && unindexVoxel(voxel);
    }

    // get all voxels of this layer
    public final Voxel[] getVoxels() {
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
