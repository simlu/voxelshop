package com.vitco.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * To read a file (binary or not)
 */
public class FileIn  {

    private final SaveDataInputStream in;

    // constructor
    public FileIn(File file) throws FileNotFoundException {
        in = new SaveDataInputStream(new FileInputStream(file));
    }

    // constructor
    public FileIn(String filename) throws FileNotFoundException {
        this(new File(filename));
    }

    public String readLine() throws IOException {
        return in.readLineSave();
    }

    public byte readByte() throws IOException {
        return in.readByte();
    }

    public int readInt() throws IOException {
        return in.readInt();
    }

    public int readIntRev() throws IOException {
        return Integer.reverseBytes(in.readInt());
    }

    public float readFloat() throws IOException {
        return in.readFloat();
    }

    public float readFloatRev() throws IOException {
        return Float.intBitsToFloat(
                Integer.reverseBytes(Float.floatToIntBits (in.readFloat()))
        );
    }

    public double readDouble() throws IOException {
        return in.readDouble();
    }

    public String readString(int length) throws IOException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(in.readChar());
        }
        return result.toString();
    }

    public long readLong() throws IOException {
        return in.readLong();
    }

    public char readChar() throws IOException {
        return in.readChar();
    }

    public int readByteUnsigned() throws IOException {
        return in.read();
    }

    public short readShort() throws IOException {
        return in.readShort();
    }

    public short readShortRev() throws IOException {
        return Short.reverseBytes(in.readShort());
    }

    public int readShortUnsigned() throws IOException {
        return readShort() & 0xffff;
    }

    public int readShortRevUnsigned() throws IOException {
        return readShortRev() & 0xffff;
    }

    // finalize
    public void finish() throws IOException {
        in.close();
    }
}
