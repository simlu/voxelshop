package com.vitco.export.container;

import java.util.ArrayList;

/**
 * Manages a list of textures and implements compression techniques for the textures.
 */
public class TriTextureManager {
    // holds list of textures
    private final ArrayList<TriTexture> textures = new ArrayList<TriTexture>();

    public void addTexture(TriTexture triTexture) {
        textures.add(triTexture);
    }
}
