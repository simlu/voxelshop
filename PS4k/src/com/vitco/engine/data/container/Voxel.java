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
    private int textureId = -1; // get the texture id of this voxel

    private int rotation = 0; // rotation 0-3
    public int getRotation() {
        return rotation;
    }
    public void rotateBy(int value) {
        assert value > -4;
        assert value < 4;
        this.rotation = (rotation + value + 4) % 4;
    }

    // constructor (with texture)
    public Voxel(int id, int[] pos, Color color, int textureId, int layerId) {
        this.id = id;
        this.color = color;
        this.layerId = layerId;
        this.textureId = textureId;
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
    public final String getPosAsString() {
        return posI[0] + "_" + posI[1] + "_" + posI[2];
    }

    // set the color of this voxel
    protected final void setColor(Color color) {
        this.color = color;
    }

    // get the color of this voxel
    public final Color getColor() {
        return color;
    }

    // set the texture of this voxel
    protected final void setTexture(Integer textureId) {
        this.textureId = textureId;
    }

    // get the texture of this voxel
    public final int getTexture() {
        return textureId;
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
