package com.vitco.app.util.misc;

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

    // generate the appropriate texture size
    // (makes sure that the dimension is a power of two)
    public static int getTextureSize(int s) {
        return (int)Math.pow(2, Math.ceil(Math.log(s + 2)/Math.log(2)));
    }
}
