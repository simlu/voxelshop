package com.vitco.app.util.misc;

/**
 * Provides some basic functionality to alter bytes.
 */
public class ByteHelper {
    // binary values
    private static final byte[] BYTE = new byte[] {-128, 64, 32, 16, 8, 4, 2, 1};

    public static boolean isBitSet(byte input, int bit) {
        return (input & BYTE[bit]) == BYTE[bit];
    }

    public static byte setBit(byte input, int bit) {
        return (byte) (input | BYTE[bit]);
    }

    public static byte clearBit(byte input, int bit) {
        return (byte) (input & ~BYTE[bit]);
    }

}
