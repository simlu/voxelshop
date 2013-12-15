package com.vitco.low.hull;

import com.vitco.util.misc.IntegerTools;

/**
 * Indexes a 1600 ^ 3 cube.
 *
 * Defines mappings X x Y x Z -> Integer and reverse.
 */
public class CubeIndexer {

    // dimension/radius
    public static final short width = 1600; // up to 1600 is fine, because 1625^3 < Integer.MAX_VALUE - Integer.MIN_VALUE
    public static final short radius = 800;
    public static final int widthwidth = width * width;

    // needs to be dividable by width ^ 2 and smaller than Integer.MIN_VALUE
    private static final int min = - 2145280000;
    private static final int minOffset = radius + min/widthwidth;

    // compute the 1d representation for the position (x right, y up, z into background)
    public static int getId(short[] pos) {
        return min + (pos[0] + radius) + (pos[2] + radius) * width + (pos[1] + radius) * widthwidth;
    }

    // compute the 1d representation for the position (x right, y up, z into background)
    public static int getId(int x, int y, int z) {
        return min + (x + radius) + (y + radius) * width + (z + radius) * widthwidth;
    }

    // compute the 1d representation for the position (x right, y up, z into background)
    public static short[] getPos(int id) {
        short x = (short) (IntegerTools.ifloormod2(id, width) - radius);
        short z = (short) (IntegerTools.ifloordiv2(IntegerTools.ifloormod2(id,widthwidth),width) - radius);
        short y = (short) (IntegerTools.ifloordiv2(id,widthwidth) - minOffset);
        return new short[]{x,y,z};
    }

}
