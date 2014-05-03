package com.vitco.export.generic.container;

import java.awt.*;

/**
 * object that holds plane material information
 * this is not only color / texture but also how the texture is
 * used (if this material has texture)
 */

public class PlaneMaterial {
    public final Color color;
    public final Integer textureId;
    public final int rotation;
    public final boolean flip;
    public final boolean hasTexture;
    public final int orientation;

    public PlaneMaterial(Color color, Integer textureId, int rotation, boolean flip, int orientation) {
        this.color = color;
        this.textureId = textureId;
        this.rotation = rotation;
        this.flip = flip;
        this.hasTexture = textureId != null;
        this.orientation = orientation;
    }

    // serves as a unique identifier for this material
    @Override
    public String toString() {
        return color.getRGB() + "_" +
                (textureId == null ? "null" : textureId) + "_" +
                rotation + "_" +
                (flip ? "1" : "0");
    }
}