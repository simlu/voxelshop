package com.vitco.util;

/**
 * Some Array help function.
 */
public class ArrayUtil {
    // true iff array contains the key
    public static boolean contains(final int[] array, final int key) {
        if (array == null) return false;
        for (final int i : array) {
            if (i == key) {
                return true;
            }
        }
        return false;
    }
}
