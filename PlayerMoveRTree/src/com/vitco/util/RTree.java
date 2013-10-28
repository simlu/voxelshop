package com.vitco.util;

import com.infomatiq.jsi.Rectangle;
import gnu.trove.TIntProcedure;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses fast and save RTree implementation.
 */
public class RTree<T> extends com.infomatiq.jsi.rtree.RTree {

    // constructor
    public RTree() {
        this.init(null);
    }

    // bitmap to find relation (id <-> T)
    private final BiMap<Integer, T> index = new BiMap<Integer, T>();

    // delete an entry from this RTree
    public boolean delete(Rectangle rect, T object) {
        Integer id = index.getKey(object);
        if (id != null) {
            delete(rect, id);
            index.removeByKey(id);
            return true;
        } else {
            return false;
        }
    }

    private int currentIndex = Integer.MIN_VALUE;

    // insert an entry into this RTree
    public void insert(Rectangle rect, T object) {
        int id;
        do {
            id = currentIndex++;
            if (currentIndex == Integer.MAX_VALUE) {
                currentIndex = Integer.MIN_VALUE;
            }
        } while(index.containsKey(id));
        index.put(id, object);
        add(rect, id);
    }

    // helper to do a ranged search
    private final class SaveToListProcedure extends ArrayList<T> implements TIntProcedure {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean execute(int id) {
            add(index.get(id));
            return true;
        }
    }

    // search an area and obtain all objects contained in it
    public List<T> search(Rectangle rect) {
        SaveToListProcedure saveToListProcedure = new SaveToListProcedure();
        contains(rect, saveToListProcedure);
        return saveToListProcedure;
    }
}
