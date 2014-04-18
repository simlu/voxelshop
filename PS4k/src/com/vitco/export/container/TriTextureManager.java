package com.vitco.export.container;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manages a list of textures and implements compression techniques for the textures.
 */
public class TriTextureManager {
    // holds list of textures
    private final ArrayList<TriTexture> textures = new ArrayList<TriTexture>();

    // list of unique TriTextures (by id)
    private final HashMap<Integer, TriTexture> uniqueTextures = new HashMap<Integer, TriTexture>();

    // true if the texture list is outdated
    private boolean outdated = false;

    // invalidate the texture list
    private void invalidate() {
        outdated = true;
    }

    // validate the texture list
    private void validate() {
        if (outdated) {
            outdated = false;
            uniqueTextures.clear();
            for (TriTexture tex : textures) {
                uniqueTextures.put(tex.getId(), tex);
            }
        }
    }

    // #########################

    // add a texture to this manager
    public final void addTexture(TriTexture triTexture) {
        textures.add(triTexture);
        invalidate();
    }

    // get a texture with a certain id
    public final TriTexture getTexture(int id) {
        validate();
        return uniqueTextures.get(id);
    }

    // ########################

    // compress the textures in this manager
    public final void compress() {
        // todo compress textures
        // ....

        // invalidate
        invalidate();
    }

    // update uv maps
    public final void validateUVMappings() {
        for (TriTexture texture : textures) {
            texture.validateUVMapping();
        }
    }
}
