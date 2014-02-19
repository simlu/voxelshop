package com.vitco.util.file;

import java.io.File;
import java.io.FileNotFoundException;
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

    // ---------------------


}
