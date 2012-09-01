package com.vitco.engine.data.container;

import java.awt.*;
import java.io.Serializable;

/**
 * A Voxel instance, only getter are available (!)
 */
public final class Voxel implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int[] posI;
    private final float[] posF = new float[3]; // position
    public final int id; // id
    private Color color; // color of voxel
    private int alpha = -1; // alpha of this voxel
    private final int layerId; // the id of the layer this voxel lives in

    // constructor
    public Voxel(int id, int[] pos, Color color, int layerId) {
        this.id = id;
        this.color = color;
        this.layerId = layerId;
        posI = pos.clone();
        for (int i = 0; i < pos.length; i++) {
            this.posF[i] = pos[i];
        }
    }

    // retrieve position
    public final int[] getPosAsInt() {
        return posI.clone();
    }
    public final float[] getPosAsFloat() {
        return posF.clone();
    }

    // set the color of this voxel
    protected final void setColor(Color color) {
        this.color = color;
    }

    // get the color of this voxel
    public final Color getColor() {
        return color;
    }

    // set the alpha of this voxel
    protected final void setAlpha(int alpha) {
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


}
