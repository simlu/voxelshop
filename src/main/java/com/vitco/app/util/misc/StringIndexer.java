package com.vitco.app.util.misc;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is used to keep track of String.
 *
 * Strings can be assigned to indices. One can then check if a given string
 * was already seen and to which index it was assigned.
 *
 * The class further supports migration of all strings that have a certain
 * index to another index (very efficient!).
 */

public final class StringIndexer {

    // maps all seen points to internal ids
    private final HashMap<String, Integer> seen = new HashMap<String, Integer>();

    // internal id counter
    private int _idCounter = 1;

    // maps local ids to external ids
    private final HashMap<Integer, Integer> local2ext = new HashMap<Integer, Integer>();

    // maps external ids to local ids (arrays)
    private final HashMap<Integer, ArrayList<Integer>> ext2local = new HashMap<Integer, ArrayList<Integer>>();

    // get one internal id (allocate if none assigned yet)
    private Integer getLocalId(Integer id) {
        ArrayList<Integer> _idList = ext2local.get(id);
        if (_idList == null) {
            _idList = new ArrayList<Integer>();
            ext2local.put(id, _idList);
            Integer _id = _idCounter++;
            _idList.add(_id);
            local2ext.put(_id, id);
        }
        return _idList.get(0);
    }

    // add a point to this manager
    public final void index(String str, Integer id) {
        Integer _id = getLocalId(id);
        seen.put(str, _id);
    }

    // check where/if a point was seen before
    // (returns null if not seen before)
    public final Integer getIndex(String str) {
        Integer _id = seen.get(str);
        if (_id != null) {
            return local2ext.get(_id);
        }
        return null;
    }

    // migrate all points from one index to another
    public final void changeIndex(Integer newIndex, Integer oldIndex) {
        // initialize the local id
        getLocalId(newIndex);

        // remap old
        ArrayList<Integer> list = ext2local.get(oldIndex);
        // this is necessary since if all points are seen,
        // there might not be points tracked for the old id
        if (list != null) {
            // remap
            for (Integer local : list) {
                local2ext.put(local, newIndex);
            }
            // remove and add
            ext2local.get(newIndex).addAll(
                    ext2local.remove(oldIndex)
            );
        }
    }

}
