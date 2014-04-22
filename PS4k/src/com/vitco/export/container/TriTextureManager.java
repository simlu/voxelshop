package com.vitco.export.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Manages a list of textures and implements compression techniques for the textures.
 */
public class TriTextureManager {
    // holds list of textures
    private final ArrayList<TriTexture> textures = new ArrayList<TriTexture>();

    // list of unique TriTextures (by id)
    private final HashMap<Integer, TriTexture> uniqueTextures = new HashMap<Integer, TriTexture>();

    // maps textures to their corresponding id
    private final HashMap<TriTexture, Integer> textureIds = new HashMap<TriTexture, Integer>();

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
            // order by parent/not parent
            Collections.sort(textures, new Comparator<TriTexture>() {
                @Override
                public int compare(TriTexture o1, TriTexture o2) {
                    return (o2.getTopTexture() == o2 ? 1 : 0) - (o1.getTopTexture() == o1 ? 1 : 0);
                }
            });
            // regenerate texture id list
            textureIds.clear();
            int i = 0;
            for (TriTexture tex : textures) {
                textureIds.put(tex, i++);
            }
            // ===========================
            // We need to obtain the id here since it might come from a parent texture.
            // Hence this must be strictly separated from the id generation above (!)
            uniqueTextures.clear();
            for (TriTexture tex : textures) {
                uniqueTextures.put(tex.getId(), tex);
            }
        }
    }

    // retrieve the id for a texture
    public final int getId(TriTexture triTexture) {
        validate();
        return textureIds.get(triTexture);
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

    // combine the textures in this manager
    public final void combine() {
        // -- find textures that are "inside" other textures
        // create dummy list that we can delete from
        ArrayList<TriTexture> textures = new ArrayList<TriTexture>(this.textures);
        int len = textures.size();
        for (int i = 0; i < len; i++) {
            for (int j = i + 1; j < len; j++) {
                TriTexture tex1 = textures.get(i);
                TriTexture tex2 = textures.get(j);
                if (tex1.makeChild(tex2)) {
                    textures.remove(j);
                    j--;
                    len--;
                } else if (tex2.makeChild(tex1)) {
                    textures.remove(i);
                    i--;
                    len--;
                    break;
                }
            }
        }

        // -- combine remaining "parent" textures into one image
        // ...

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
