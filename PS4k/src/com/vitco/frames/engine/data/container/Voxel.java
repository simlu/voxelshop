package com.vitco.frames.engine.data.container;

import java.awt.*;

/**
 * A Voxel instance
 */
public final class Voxel {
    private final float[] pos = new float[3]; // position
    private final int id; // id
    private Color color; // color of voxel
    private int alpha; // alpha of this voxel
    private int layerId; // the id of the layer this voxel lives in

    // constructor
    public Voxel(int id, int[] pos) {
        this.id = id;
        for (int i = 0; i < pos.length; i++) {
            this.pos[i] = pos[i];
        }
    }

    // retrieve id
    public final int getId() {
        return id;
    }

    // retrieve position (int[] as float[])
    public final float[] getPos() {
        return pos.clone();
    }

    // set the color of this voxel
    public final void setColor(Color color) {
        this.color = color;
    }

    // get the color of this voxel
    public final Color getColor() {
        return color;
    }

    // set the alpha of this voxel
    public final void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    // get the color of this voxel
    public final int getAlpha() {
        return alpha;
    }

    // set the layerId of this voxel
    public final void setLayerId(int layerId) {
        this.layerId = layerId;
    }

    // get the layerId of this voxel
    public final int getLayerId() {
        return layerId;
    }


}
