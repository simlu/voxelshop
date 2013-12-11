package com.vitco.engine.world.container;

import com.vitco.engine.data.container.Voxel;

import java.awt.*;

/**
 *  helper class that represents a voxel side (face)
 */
public final class Face {
    // ===============
    // static information
    // ===============

    // the face (orientation)
    private final int orientation;

    // two dimensional position
    private final int[] pos2D;
    public final int[] getPos2D() {
        return pos2D.clone();
    }

    // get position as string
    public final String getPosAsString2D() {
        return pos2D[0] + "_" + pos2D[1];
    }

    // static convert 3D to 2D
    public static int[] convert(int[] pos3D, int axis) {
        int[] result;
        switch (axis) {
            case 0: result = new int[] {pos3D[1], pos3D[2]}; break;
            case 1: result = new int[] {pos3D[0], pos3D[2]}; break;
            default: result = new int[] {pos3D[0], pos3D[1]}; break;
        }
        return result;
    }

    // static convert 3D to 2D String key
    public static String convertString(int[] pos3D, int axis) {
        int[] tmp = convert(pos3D, axis);
        return tmp[0] + "_" + tmp[1];
    }

    // ----------------------

    // constructor
    public Face(Voxel voxel, int orientation) {
        // store which side we're dealing with
        this.orientation = orientation;

        // set the 2D position
        pos2D = convert(voxel.getPosAsInt(), orientation/2);

        // store/refresh face information from voxel
        refresh(voxel);
    }

    // update face information with the specifics from the voxel
    public final void refresh(Voxel voxel) {
        // set the color information
        color = voxel.getColor();
        // set the texture information
        int[] texture = voxel.getTexture();
        this.texture = texture == null ? null : texture[orientation];
        // set the rotation information
        int[] rotation = voxel.getRotation();
        this.rotation = rotation == null ? null : rotation[orientation];
        // set the flip information
        boolean[] flip = voxel.getFlip();
        this.flip = flip != null && flip[orientation];
    }

    // ===============
    // dynamic voxel information
    // ===============

    // color of this side
    private Color color;
    public final Color getColor() {
        return color;
    }

    // texture of this side
    private Integer texture;
    public final Integer getTexture() {
        return texture;
    }

    // rotation information of this side
    private Integer rotation;
    public final int getRotation() {
        return rotation == null ? 0 : rotation;
    }

    // flip information of this side
    private boolean flip;
    public final boolean isFlip() {
        return flip;
    }

}
