package com.vitco.export.generic.container;

import com.vitco.layout.content.console.ConsoleInterface;
import com.vitco.util.components.progressbar.ProgressDialog;
import com.vitco.util.components.progressbar.ProgressReporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Manages a list of textures and implements compression techniques for the textures.
 */
public class TriTextureManager extends ProgressReporter {
    // holds list of textures
    private final ArrayList<TriTexture> textures = new ArrayList<TriTexture>();

    // list of unique TriTextures (by id)
    private final HashMap<Integer, TriTexture> uniqueTextures = new HashMap<Integer, TriTexture>();

    // maps textures to their corresponding id
    private final HashMap<TriTexture, Integer> textureIds = new HashMap<TriTexture, Integer>();

    // true if the texture list is outdated
    private boolean outdated = false;

    // constructor
    public TriTextureManager(ProgressDialog dialog, ConsoleInterface console) {
        super(dialog, console);
    }

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
                    return (o1.hasParent() ? 1 : 0) - (o2.hasParent() ? 1 : 0);
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

    // pad textures to the next power of two size
    public final void resizeToPowOfTwo() {
        for (TriTexture tex : new ArrayList<TriTexture>(this.textures)) {
            if (!tex.hasParent()) {
                addTexture(new TriTexture(tex, this));
            }
        }
    }

    // combine the textures in this manager
    public final void combine() {
        // -- find textures that are "inside" other textures
        // create dummy list that we can delete from
        ArrayList<TriTexture> textures = new ArrayList<TriTexture>(this.textures);
        // sort by size (smallest first) - this makes processing a bit faster
        Collections.sort(textures, new Comparator<TriTexture>() {
            @Override
            public int compare(TriTexture o1, TriTexture o2) {
                return Math.max(o1.width, o1.height) - Math.max(o2.width, o2.height);
            }
        });
        int len = textures.size();
        int originalLength = len;
        setActivity("Merging Textures...", false);
        for (int i = 0; i < len; i++) {
            for (int j = i + 1; j < len; j++) {
                if (len%100 == 0) {setProgress(((originalLength - len - i)/(float)originalLength)*100);}
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

        //System.out.println("Obtained " + textures.size() + " unique textures after merging.");

        // sort by size (largest first) - better combining having the "smaller ones in the back"
        Collections.sort(textures, new Comparator<TriTexture>() {
            @Override
            public int compare(TriTexture o1, TriTexture o2) {
                return Math.max(o2.width, o2.height) - Math.max(o1.width, o1.height);
            }
        });
        // -- combine remaining "parent" textures into one image
        int lengthBeforeCombining = len;
        setActivity("Combining Textures...", false);
        while (len > 1) {
            setProgress(((lengthBeforeCombining - len + 1)/(float)lengthBeforeCombining) * 100);
            // find the texture with the biggest jaccard similarity
            TriTexture texture = textures.get(0);
            TriTexture mergeTo = textures.get(1);
            int mergeToId = 1;
            float similarity = texture.jaccard(mergeTo);
            for (int i = 2; i < len; i++) {
                TriTexture compareTo = textures.get(i);
                float newSim = texture.jaccard(compareTo);
                // note: this gets the largest most similar neighbour
                if (newSim > similarity) {
                    similarity = newSim;
                    mergeTo = compareTo;
                    mergeToId = i;
                }
            }
            // check if we can make this a child
            // otherwise we combine the textures
            if (texture.makeChild(mergeTo)) {
                textures.remove(mergeToId);
            } else if (mergeTo.makeChild(texture)) {
                textures.remove(0);
            } else {
                // generate the new TriTexture
                TriTexture parentTexture = new TriTexture(texture, mergeTo, this);
                // remove textures
                textures.remove(mergeToId);
                textures.remove(0);
                // add new parent to front
                textures.add(0, parentTexture);
                // register texture
                this.addTexture(parentTexture);
            }
            len--;
        }

        // invalidate texture list (for id generation)
        invalidate();

        //System.out.println("Pixel Count: " + textures.get(0).getPixelCount());
    }

    // update uv maps
    public final void validateUVMappings() {
        for (TriTexture texture : textures) {
            texture.validateUVMapping();
        }
    }
}
