package com.vitco.util.misc;

/**
 * General functionality related to numbers
 */
public class NumberTools {

    // parse an integer with default value
    public static int parseInt(String string, int defaultValue) {
        try {
            return Integer.parseInt(string);
        } catch(NumberFormatException nfe) {
            return defaultValue;
        }
    }

    // parse an integer with default value
    public static float parseFloat(String string, float defaultValue) {
        try {
            return Float.parseFloat(string);
        } catch(NumberFormatException nfe) {
            return defaultValue;
        }
    }
}
