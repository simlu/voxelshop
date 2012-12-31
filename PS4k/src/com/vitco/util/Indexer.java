package com.vitco.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the interface for spacial searching.
 *
 * This can still be optimized a lot by using an R Tree.
 */
public class Indexer {

    // a data object
    private static final class DataObject {
        public final float[] values;
        public final float[] dim;

        public DataObject(float[] values, float[] dim) {
            this.values = values;
            this.dim = dim;
        }
    }

    // holds data
    private final Map<Integer, DataObject> data = new HashMap<Integer, DataObject>();

    // true iff value,dim contains the object id and id exists
    private boolean contains(float[] values, float[] dim, int id) {
        boolean result = true;
        if (data.containsKey(id)) {
            DataObject obj = data.get(id);
            for (int i = 0; i < obj.values.length; i++) {
                if (obj.values[i] < values[i]) {
                    result = false;
                }
                if (obj.values[i] + obj.dim[i] > values[i] + dim[i]) {
                    result = false;
                }
            }
        } else {
            result = false;
        }
        return result;
    }

    // clear data
    public void clear() {
        data.clear();
    }

    // add value
    public void insert(float[] values, float[] dim, int id) {
        data.put(id, new DataObject(values, dim));
    }

    // delete value
    public boolean delete(float[] values, float[] dim, int id) {
        if (contains(values, dim, id)) {
            data.remove(id);
            return true;
        }
        return false;
    }

    // range search
    public List<Integer> search(float[] values, float[] dim) {
        List<Integer> result = new ArrayList<Integer>();
        for (Integer id: data.keySet()) {
            if (contains(values, dim, id)) {
                result.add(id);
            }
        }
        return result;
    }

}
