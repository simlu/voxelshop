package com.vitco.core.world;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.vitco.core.data.container.Voxel;
import com.vitco.settings.VitcoSettings;

import java.awt.*;

/**
 * Abstract - This is a world wrapper that provides easy voxel interaction.
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

    // --------------

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
            drawWireframe(buffer, selected);
        }
    }

    // find the collision point for a selected voxel (shifted selection)
    public final short[] getShiftedCollisionVoxel(SimpleVector dir) {
        // check if we hit a <selected> voxel
        short[] result = null;
        Camera camera = getCamera();
        camera.moveCamera(offset, length);
        SimpleVector origin = camera.getPosition().calcAdd(
                new SimpleVector(VitcoSettings.HALF_VOXEL_SIZE, VitcoSettings.VOXEL_GROUND_DISTANCE, VitcoSettings.HALF_VOXEL_SIZE)
        );
        origin.scalarMul(1 / VitcoSettings.VOXEL_SIZE);
        short[] hit = this.hitTest(origin, dir);
        if (hit != null) { // something hit
            // find collision point
            result = hit;
        }
        camera.moveCamera(offset, -length);
        return result;
    }

    // -----------

    // add or update a voxel
    public abstract void updateVoxel(Voxel voxel);

    // erase the entire content of this world
    public abstract void clear();

    // clear field by position
    public abstract boolean clearPosition(int[] pos);

    // clear field by voxel
    public abstract boolean clearPosition(Voxel voxel);

    // enable/disable the border on all objects in the world (main view)
    public abstract void setBorder(boolean border);

    // refresh world (partially) - returns true if fully refreshed
    public abstract boolean refreshWorld();

    // get voxel by hit position
    public abstract int[] getVoxelPos(Integer objectId, float posx, float posy, float posz);

    // get side for world object
    public abstract Integer getSide(Integer objectId);

    // do a hit test against the voxels in this world
    public abstract short[] hitTest(SimpleVector position, SimpleVector dir);
}
