package com.vitco.core.data.container;

import com.vitco.low.CubeIndexer;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * A Voxel instance, only getter are available (!)
 */
public final class Voxel implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int[] posI;
    private transient String posS = null;
    // make final when legacy support is removed
    public transient final int x;
    public transient final int y;
    public transient final int z;
    public transient final int posId; // position id
    public final int id; // id
    private Color color; // color of voxel
    private int alpha = -1; // alpha of this voxel
    private final int layerId; // the id of the layer this voxel lives in
    private int[] textureIds = null; // get the texture ids of this voxel (for all sides)

    private int[] sideRotation = null;
    private boolean[] sideFlip = null;

    public static Integer[] convertVoxelsToIdArray(Voxel[] voxels) {
        Integer[] voxelIds = new Integer[voxels.length];
        for (int i = 0; i < voxels.length; i++) {
            voxelIds[i] = voxels[i].id;
        }
        return voxelIds;
    }

    // constructor (with texture)
    public Voxel(int id, int[] pos, Color color, boolean selected, int[] textureIds, int layerId) {
        this.id = id;
        this.color = color;
        this.layerId = layerId;
        this.textureIds = textureIds == null ? null : textureIds.clone();
        this.selected = selected;
        posI = pos.clone();
        posS = posI[0] + "_" + posI[1] + "_" + posI[2];
        // load the public values for fast access
        x = pos[0];
        y = pos[1];
        z = pos[2];
        // define position id
        posId = CubeIndexer.getId(posI[0], posI[1], posI[2]);
    }

    // called after deserialization
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // build the string representation when loading from file
        if (posS == null) {
            posS = posI[0] + "_" + posI[1] + "_" + posI[2];
        }
        // read the transient final values after de-serialization
        try {
            Field f = this.getClass().getDeclaredField("x");
            f.setAccessible(true);
            f.set(this, posI[0]);
            f = this.getClass().getDeclaredField("y");
            f.setAccessible(true);
            f.set(this, posI[1]);
            f = this.getClass().getDeclaredField("z");
            f.setAccessible(true);
            f.set(this, posI[2]);

            f = this.getClass().getDeclaredField("posId");
            f.setAccessible(true);
            f.set(this, CubeIndexer.getId(posI[0], posI[1], posI[2]));
        } catch (NoSuchFieldException e) {
            // should never happen
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // should never happen
            e.printStackTrace();
        }
    }

    // retrieve position
    public final int[] getPosAsInt() {
        return posI.clone();
    }
    public final String getPosAsString() {
        return posS;
    }

    // set the color of this voxel
    public final void setColor(Color color) {
        this.color = color;
    }

    // get the color of this voxel
    public final Color getColor() {
        return color;
    }

    // rotate this voxel
    public final void rotate(Integer side) {
        if (sideRotation == null) {
            sideRotation = new int[6];
        }
        sideRotation[side] = (sideRotation[side] + 1)%4;
    }

    // rotate this voxel (reverse)
    public final void rotateReverse(Integer side) {
        if (sideRotation == null) {
            sideRotation = new int[6];
        }
        sideRotation[side] = (sideRotation[side] + 3)%4;
    }

    // get the rotation of this voxel
    public final int[] getRotation() {
        return sideRotation == null ? null : sideRotation.clone();
    }

    // set the flip of this voxel
    public final void flip(Integer side) {
        if (sideFlip == null) {
            sideFlip = new boolean[6];
        }
        sideFlip[side] = !sideFlip[side];
    }

    // get the flip of this voxel
    public final boolean[] getFlip() {
        return sideFlip == null ? null : sideFlip.clone();
    }

    // set the texture of this voxel
    public final boolean setTexture(int[] textureIds) {
        if (textureIds == null || textureIds.length == 6) {
            this.textureIds = textureIds == null ? null : textureIds.clone();
            // cancel rotation/flipping
            if (textureIds == null) {
                sideRotation = null;
                sideFlip = null;
            }
            return true;
        }
        return false;
    }

    // get the texture of this voxel
    public final int[] getTexture() {
        return textureIds == null ? null : textureIds.clone();
    }

    // set the alpha of this voxel
    public final void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    // get the color of this voxel
    public final int getAlpha() {
        return alpha;
    }

    // get the layerId of this voxel
    public final int getLayerId() {
        return layerId;
    }

    // ===================================
    // for this object instance only
    private transient boolean selected = false;

    public final boolean isSelected() {
        return selected;
    }

    public final void setSelected(boolean b) {
        selected = b;
    }

}
