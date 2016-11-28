package com.vitco.app.low.hull;

import java.util.HashMap;

/**
 * Efficiently tracks the minimum and maximum of a set of shorts.
 */
public class MinMaxTracker {

    // known values and their count in the list
    private final HashMap<Short, Short> known = new HashMap<Short, Short>();

    // maximum and minimum values
    private short min = Short.MAX_VALUE, max = Short.MIN_VALUE;

    // gets the current minimum value
    public final short getMin() {
        refresh();
        return min;
    }

    // gets the current maximum value
    public final short getMax() {
        refresh();
        return max;
    }

    // reset this object
    public final void clear() {
        known.clear();
        min = Short.MAX_VALUE;
        max = Short.MIN_VALUE;
        outdated = false;
    }

    // -------------------

    // true if the internal min/max values are outdated
    private boolean outdated = false;

    // internal - refreshes the min/max value if necessary
    private void refresh() {
        if (outdated) {
            min = Short.MAX_VALUE;
            max = Short.MIN_VALUE;
            for (Short val : known.keySet()) {
                min = (short) Math.min(min, val);
                max = (short) Math.max(max, val);
            }
            outdated = false;
        }
    }

    // Adds a short to the list
    public final void add(short val) {
        Short count = known.get(val);
        if (count == null) {
            known.put(val, (short)1);
            // update min/max values
            min = (short) Math.min(min, val);
            max = (short) Math.max(max, val);
        } else {
            known.put(val, ++count);
        }
    }

    // Remove a short from the list
    // Note: Assumes that the list contains the integer when the remove is called (no check!).
    public final void remove(short val) {
        Short count = known.get(val);
        if (count == 1) {
            known.remove(val);
            // check if this could affect the maximum value
            if (!outdated && (val == min || val == max)) {
                outdated = true;
            }
        } else {
            known.put(val, --count);
        }
    }

}
