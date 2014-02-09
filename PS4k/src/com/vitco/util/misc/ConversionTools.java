package com.vitco.util.misc;

/**
 * Some Hex tools.
 */
public class ConversionTools {
    // convert int[] to byte[]
    public static byte[] int2byte(int[] src) {
        int j = 0;
        byte[] outData = new byte[src.length*4];
        for (int aSrc : src) {
            outData[j++] = (byte) (aSrc >>> 24);
            outData[j++] = (byte) (aSrc >>> 16);
            outData[j++] = (byte) (aSrc >>> 8);
            outData[j++] = (byte) (aSrc);
        }
        return outData;
    }
}
