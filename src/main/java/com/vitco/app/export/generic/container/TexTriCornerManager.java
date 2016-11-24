package com.vitco.app.export.generic.container;

import com.vitco.app.util.misc.BiMap;

import java.util.ArrayList;

/**
 * Manages corner information of a TexTriCornerInfo
 */
public class TexTriCornerManager<T extends TexTriCornerInfo> {

    // list of known corner information objects
    private final ArrayList<T> known = new ArrayList<T>();
    // list of unique corner information objects mapped to there id
    // this is only accurate after validate() has been called
    private final BiMap<T, Integer> knownUnique = new BiMap<T, Integer>();
    // true if the internal data structure is outdated
    private boolean outdated = true;

    // add a triangle corner information
    protected final void add(T cornerInfo) {
        known.add(cornerInfo);
        outdated = true;
    }

    // invalidate internal data structure (triangle ids)
    protected final void invalidate() {
        outdated = true;
    }

    // validate the buffer
    private void validate() {
        if (outdated) {
            knownUnique.clear();
            int i = 0;
            for (T p : known) {
                if (!knownUnique.containsKey(p)) {
                    knownUnique.put(p, i++);
                }
            }
            outdated = false;
        }
    }

    // ----------
    // below methods need validation

    // get the id of a triangle corner info
    protected final int getId(T cornerInfo) {
        validate();
        return knownUnique.get(cornerInfo);
    }

    // get string with unique cornerInfo
    public final String getString(boolean asInt) {
        validate();
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        for (int i = 0, len = knownUnique.size(); i < len; i++) {
            if (!first) {
                stringBuilder.append(" ");
            } else {
                first = false;
            }
            stringBuilder.append(knownUnique.getKey(i).toString(asInt));
        }
        return stringBuilder.toString();
    }

    // get the amount of unique corner info objects in this manager
    public int getUniqueCount() {
        validate();
        return knownUnique.size();
    }
}
