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

    public String readLine() throws IOException {
        return in.readLineSave();
    }

    public byte readByte() throws IOException {
        return in.readByte();
    }

    public int readInt() throws IOException {
        return in.readInt();
    }

    public int readReverseInt() throws IOException {
        return Integer.reverseBytes(in.readInt());
    }

    public double readDouble() throws IOException {
        return in.readDouble();
    }

    // finalize
    public void finish() throws IOException {
        in.close();
    }
}
