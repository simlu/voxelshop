package com.vitco.util.misc;

/**
 * Some Hex tools.
 */
public class HexTools {
    public static String byteToHex(byte[] inBytes) {
        StringBuilder sb = new StringBuilder();
        for (byte inByte : inBytes) {
            sb.append(Integer.toString((inByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
