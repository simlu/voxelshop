package com.vitco.engine.world;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.vitco.engine.data.container.Voxel;
import com.vitco.engine.world.container.FaceListener;
import com.vitco.engine.world.container.VoxelW;
import com.vitco.res.VitcoSettings;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Interface - This is a world wrapper that provides easy voxel interaction.
 */
public abstract class AbstractCWorld extends World {
    protected final boolean culling;
    protected final Integer side; // default -1 (all sites)
    protected final boolean simpleMode;
    // constructor
    public AbstractCWorld(boolean culling, Integer side, boolean simpleMode) {
        this.culling = culling;
        this.side = side;
        this.simpleMode = simpleMode;
    }

    // ===========================
    // data objects
    // ===========================

    // holds the voxel wrapper positions
    protected final HashMap<String, VoxelW> voxelPos = new HashMap<String, VoxelW>();

    // voxel that need to be redrawn
    protected final HashSet<VoxelW> toUpdate = new HashSet<VoxelW>();

    // ==============================
    // drawing of selected (wireframe)
    // ==============================

    // move offset
    private SimpleVector offset = new SimpleVector(0, 0, 0);
    private float length = offset.length();

    // set the shift of this (just used for "drawAsShiftedWireframe")
    public final void setShift(int[] shift) {
        offset = new SimpleVector(
                shift[0],
                shift[1],
                shift[2]);
        length = offset.length() * VitcoSettings.VOXEL_SIZE;
        offset = offset.normalize();
    }

    // draw just the wireframe (possible shifted)
    public final void drawAsShiftedWireframe(FrameBuffer buffer, Color selected, Color shifted) {
        if (length != 0) {
            getCamera().moveCamera(offset, length);
            renderScene(buffer);
            drawWireframe(buffer, shifted);
            getCamera().moveCamera(offset, -length);
        } else {
            renderScene(buffer);
            drawWireframe(buffer, VitcoSettings.SELECTED_VOXEL_WIREFRAME_COLOR);
        }
    }

    // find the collision point for a selected voxel (shifted selection)
    public final SimpleVector shiftedCollisionPoint(SimpleVector dir) {
        // check if we hit a <selected> voxel
        SimpleVector result = null;
        Camera camera = getCamera();
        camera.moveCamera(offset, length);
        Object[] res = calcMinDistanceAndObject3D(camera.getPosition(), dir, 100000);
        if (res[1] != null) { // something hit
            // find collision point
            result = camera.getPosition();
            dir.scalarMul((Float)res[0]);
            result.add(dir);
        }
        camera.moveCamera(offset, -length);
        return result;
    }

    // ==============================
    // voxel interaction
    // ==============================

    protected FaceListener faceListener = null;

    // add or update a voxel voxel
    public final void updateVoxel(Voxel voxel) {
        String pos = voxel.getPosAsString();
        if (voxelPos.containsKey(pos)) {
            voxelPos.get(pos).refresh(voxel); // update voxel
        } else {
            if (faceListener != null) {
                // add new voxel and add side listener
                new VoxelW(voxel, voxelPos, toUpdate).setSideListener(faceListener);
            } else {
                new VoxelW(voxel, voxelPos, toUpdate);
            }
        }
    }

    // erase the entire content of this world
    public final void clear() {
        for (Object voxel : voxelPos.values().toArray()) {
            ((VoxelW)voxel).remove();
        }
    }

    // clear field by position
    public final boolean clearPosition(int[] pos) {
        boolean result = false;
        VoxelW wrapper = voxelPos.get(pos[0] + "_" + pos[1] + "_" + pos[2]);
        if (wrapper != null) {
            wrapper.remove();
            result = true;
        }
        return result;
    }

    // clear field by voxel
    public final boolean clearPosition(Voxel voxel) {
        boolean result = false;
        VoxelW wrapper = voxelPos.get(voxel.getPosAsString());
        if (wrapper != null) {
            wrapper.remove();
            result = true;
        }
        return result;
    }

    // enable/disable the border on all objects in the world (main view)
    public abstract void setBorder(boolean border);

    // refresh world (partially) - returns true if fully refreshed
    public abstract boolean refreshWorld();

    // get voxel by hit position
    public abstract int[] getVoxelPos(Integer objectId, float posx, float posy, float posz);

    // get side for world object
    public abstract Integer getSide(Integer objectId);

    // retrieve all visible voxels in this world
    public final HashMap<Voxel, String> getVisibleVoxel() {
        HashMap<Voxel, String> result = new HashMap<Voxel, String>();
        for (VoxelW voxel : voxelPos.values()) {
            String sides = voxel.getSides();
            if (!sides.equals("111111")) {
                result.put(voxel.getVoxel(), voxel.getSides());
            }
        }
        return result;
    }
}
