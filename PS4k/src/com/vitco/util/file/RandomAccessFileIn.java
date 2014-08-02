package com.vitco.util.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * RandomAccessFile with additional functionaly
 */
public class RandomAccessFileIn extends RandomAccessFile {
    // constructor
    public RandomAccessFileIn(String name, String mode) throws FileNotFoundException {
        super(name, mode);
    }

    // constructor
    public RandomAccessFileIn(File file, String mode) throws FileNotFoundException {
        super(file, mode);
    }

    public int readIntRevUnsigned() throws IOException {
        return readIntRev() & 0xffffff;
    }

    public int readIntRev() throws IOException {
        return Integer.reverseBytes(this.readInt());
    }

    public int readUInt8() throws IOException {
        return ((int)readByte()) & 0xFF;
    }

    public int readUInt32() throws IOException {
        return readIntRevUnsigned();
    }

    public static final int CURRENT = 1;
    public static final int BEGINNING = 2;

    public void seek(int length, int type) throws IOException {
        switch (type) {
            case CURRENT:
                skipBytes(length);
                break;
            default:
                seek(length);
                break;
        }
    }

    public String readASCII(int length) throws IOException {
        byte[] bytes = new byte[length];
        if (length != this.read(bytes)) {
            return null;
        }
        return new String(bytes, "ASCII");
    }

    public byte[] readBytes(int length) throws IOException {
        byte[] result = new byte[length];
        read(result);
        return result;
    }

    public int readInt32() throws IOException {
        return readIntRev();
    }

    // ---------------------


}
