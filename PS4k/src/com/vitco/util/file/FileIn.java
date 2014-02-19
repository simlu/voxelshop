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

    private final File file;
    public final File getInternalFile() {
        return file;
    }

    // constructor
    public FileIn(File file) throws FileNotFoundException {
        in = new SaveDataInputStream(new FileInputStream(file));
        this.file = file;
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

    public String readASCIIString(int length) throws IOException {
        byte[] bytes = new byte[length];
        if (length != in.read(bytes)) {
            return null;
        }
        return new String(bytes, "ASCII");
    }

    // read space terminated string
    public String readSpaceString() throws java.io.IOException {
        String rtn = "";
        int ch;
        do {
            ch = in.read();
            if (ch != 32 && ch != -1)
                rtn += (char)ch;
        } while (ch != 32 && ch != -1);
        return ch != -1 ? rtn : null;
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

    public int readIntRevUnsigned() throws IOException {
        return readIntRev() & 0xffffff;
    }

    public int readIntUnsigned() throws IOException {
        return readInt() & 0xffffff;
    }

    public long readLongRev() throws IOException {
        return Long.reverseBytes(readLong());
    }

    public boolean skipBytes(int mainContentSize) throws IOException {
        return mainContentSize == in.skip(mainContentSize);
    }
}
