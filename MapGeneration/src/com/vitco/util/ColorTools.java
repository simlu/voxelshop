package com.vitco.util;

/**
 * Color Tools
 */
public class ColorTools {
    public static double colorDistanceNatural(double[] rgb1, double[] rgb2) {
        int rmean = (int) Math.round((rgb1[0] + rgb2[0]) / 2);
        int r = (int) (rgb1[0] - rgb2[0]);
        int g = (int) (rgb1[1] - rgb2[1]);
        int b = (int) (rgb1[2] - rgb2[2]);
        return Math.sqrt((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8));
    }
}
