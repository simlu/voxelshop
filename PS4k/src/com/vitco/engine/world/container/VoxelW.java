package com.vitco.engine.world.container;

import com.threed.jpct.SimpleVector;
import com.vitco.engine.data.container.Voxel;
import com.vitco.res.VitcoSettings;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/**
 *  Wrapper object that holds voxel information used by CWorld
 */
public final class VoxelW implements Serializable {
    private static final long serialVersionUID = 1L;

    // ====================
    // locally updated data
    // ====================

    // holds the current wrapped voxel object
    private Voxel voxel;
    // holds the neighbor information (different sides)
    private final VoxelW[] neighbours = new VoxelW[6];

    // ====================
    // globally updated data
    // ====================

    // holds the voxel wrapper positions (injected)
    private final HashMap<String, VoxelW> voxelPos;

    // voxel that need to be redrawn (injected)
    private final HashSet<VoxelW> toUpdate;

    // ====================

    // constructor
    public VoxelW(Voxel voxel, HashMap<String, VoxelW> voxelPos, HashSet<VoxelW> toUpdate) {
        this.voxel = voxel;
        // find neighbours and update side information (of them and this)
        // calculate the required sides
        for (int i = 0; i < 6; i++) {
            int add = i%2 == 0 ? 1 : -1;
            neighbours[i] = voxelPos.get(
                    (i/2 == 0 ? voxel.x + add : voxel.x) + "_" +
                            (i/2 == 1 ? voxel.y + add : voxel.y) + "_" +
                            (i/2 == 2 ? voxel.z + add : voxel.z)
            );
            if (neighbours[i] != null) {
                neighbours[i].addNeighbour(this, i%2 == 0?i+1:i-1);
            }
        }
        // inject the global mappings
        this.voxelPos = voxelPos;
        this.toUpdate = toUpdate;
        // add this voxel to the position and update mapping
        voxelPos.put(getPos(), this);
        toUpdate.add(this);
    }

    // refresh the voxel that this wrapper encapsulates
    public final void refresh(Voxel voxel) {
        // note: this might be the same voxel,
        // e.g. this.voxel == voxel can happen (in that case we still need to update!)
        this.voxel = voxel;
        if (listener != null) {
            // notify listener
            for (int i = 0; i < 6; i++) {
                if (neighbours[i] == null) {
                    listener.onRefresh(voxel, i);
                }
            }
        }
        toUpdate.add(this);
    }

    // helper - update with neighbour information
    private void addNeighbour(VoxelW neighbour, int side) {
        assert neighbours[side] == null;
        neighbours[side] = neighbour;
        if (listener != null) {
            // notify listener
            listener.onRemove(voxel, side);
        }
        toUpdate.add(this);
    }

    // helper - remove a neighbour
    private void removeNeighbour(int side) {
        assert neighbours[side] != null;
        neighbours[side] = null;
        if (listener != null) {
            // notify listener
            listener.onAdd(voxel, side);
        }
        toUpdate.add(this);
    }

    // ========================
    // neighbour listener

    private transient FaceListener listener = null;
    public final void setSideListener(FaceListener listener) {
        this.listener = listener;
        for (int i = 0; i < 6; i++) {
            if (neighbours[i] == null) {
                // notify listener
                listener.onAdd(voxel, i);
            }
        }
    }

    // ========================
    // remove/replace voxel

    // returns true iff this wrapper was not removed
    private boolean removed = false;
    public boolean notRemoved() {
        return !removed;
    }

    // remove this voxel
    public final void remove() {
        // update side information of neighbours
        for (int i = 0; i < 6; i++) {
            if (neighbours[i] != null) {
                neighbours[i].removeNeighbour(i%2 == 0?i+1:i-1);
            } else {
                if (listener != null) {
                    // notify listener
                    listener.onRemove(voxel, i);
                }
            }
        }
        // remove this voxel from the position mapping and add to update mapping
        voxelPos.remove(getPos());
        toUpdate.add(this);
        // update removed information
        removed = true;
    }

    // ========================
    // basic information
    // ==========================

    // get the side information of this voxel
    public final String getSides() {
        return (neighbours[0] == null ? "0" : "1") + (neighbours[1] == null ? "0" : "1") +
                (neighbours[2] == null ? "0" : "1") + (neighbours[3] == null ? "0" : "1") +
                (neighbours[4] == null ? "0" : "1") + (neighbours[5] == null ? "0" : "1");
    }

    // helper - get this position information as a string
    private String getPos() {
        return voxel.getPosAsString();
    }

    // =======================
    // world id information
    // ==========================

    private Integer worldId = null;
    public final Integer getWorldId() {
        return worldId;
    }
    public final void setWorldId(Integer worldId) {
        this.worldId = worldId;
    }

    // ==========================
    // get underlying voxel information
    // ==========================

    // get the color of the voxel
    public final Color getColor() {
        return voxel.getColor();
    }
    // get the rotation of the textures of the voxel
    public final int[] getRotation() {
        return voxel.getRotation();
    }
    // get the flip information of the texture of the voxel
    public final boolean[] getFlip() {
        return voxel.getFlip();
    }
    // get the texture information of the voxel
    public final int[] getTexture() {
        return voxel.getTexture();
    }
    // get the vector position of the voxel (position in the world)
    public final SimpleVector getVectorPos() {
        return new SimpleVector(
                voxel.x * VitcoSettings.VOXEL_SIZE,
                voxel.y * VitcoSettings.VOXEL_SIZE,
                voxel.z * VitcoSettings.VOXEL_SIZE);
    }
    // get the id of the underlying voxel (this can change!)
    public final int getVoxelId() {
        return voxel.id;
    }
    // get the underlying voxel (this can change!)
    public final Voxel getVoxel() {
        return voxel;
    }
}