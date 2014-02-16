package com.vitco.importer;

import com.vitco.util.file.FileIn;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BinVoxImporter extends AbstractImporter {

    // the voxel color is not defined in the format, so
    // we need to define it here
    private static final int voxelRGB = new Color(158, 194, 88).getRGB();

    // the size of the block that contains the voxel as written in file
    private int sx, sy, sz = 0;
    public final double[] getSize() {
        return new double[] {sx, sy, sz};
    }

    // the translation that is written in file
    private double tx, ty, tz = 0;
    public final double[] getTranslation() {
        return new double[] {tx, ty, tz};
    }

    // scale as written in file
    private double scale;
    public final double getScale() {
        return scale;
    }

    // version as written in file
    private int version;
    public final int getVersion() {
        return version;
    }

    // constructor
    public BinVoxImporter(File file) throws IOException {
        super(file);
    }

    // read file - returns true if file has loaded correctly
    @Override
    protected boolean read(FileIn fileIn, RandomAccessFile raf) throws IOException {
        // header
        String line = fileIn.readLine();
        if (!line.startsWith("#binvox")) { // not a bin vox format
            return false;
        }

        // version
        String version_string = line.substring(8);
        version = Integer.parseInt(version_string);

        line = fileIn.readLine();

        while (null != line) {

            if (line.startsWith("data")) {
                if (sx == 0 || sy == 0 || sz == 0) {
                    // required parameter missing/wrong
                    return false;
                }
                // define the size
                int size = sx * sy * sz;
                // read voxel data
                int value;
                int count = -1;
                int index = 0;
                int end_index = 0;

                while (end_index < size && count != 0) {

                    value = fileIn.readByteUnsigned();
                    // read as unsigned
                    count = fileIn.readByteUnsigned();

                    end_index = index + count;
                    if (end_index > size) {
                        return false;
                    }
                    if (value == 1) {
                        for (int i = index; i < end_index; i++) {
                            int x = i % sx;
                            int y = (i / sx) % sz;
                            int z = (i / (sx * sy));
                            addVoxel(x, y, z, voxelRGB);
                        }
                    }
                    index = end_index;
                }

                return true;

            } else if (line.startsWith("dim")) {
                String[] dimensions = line.split(" ");
                if (dimensions.length == 4) {
                    // the values are in the same line
                    sz = Integer.parseInt(dimensions[1]);
                    sy = Integer.parseInt(dimensions[2]);
                    sx = Integer.parseInt(dimensions[3]);
                } else {
                    // the values are in the next line(s)
                    sz = fileIn.readInt();
                    sy = fileIn.readInt();
                    sx = fileIn.readInt();
                }
            } else if (line.startsWith("translate")) {
                String[] translations = line.split(" ");
                if (translations.length == 4) {
                    // the values are in the same line
                    tx = Double.parseDouble(translations[1]);
                    ty = Double.parseDouble(translations[2]);
                    tz = Double.parseDouble(translations[3]);
                } else {
                    // the values are in the next line(s)
                    tx = fileIn.readDouble();
                    ty = fileIn.readDouble();
                    tz = fileIn.readDouble();
                }
            } else if (line.startsWith("scale")) {
                String[] scaleLine = line.split(" ");
                if (scaleLine.length == 2) {
                    // the values are in the same line
                    scale = Double.parseDouble(scaleLine[1]);
                } else {
                    // the value is in the next line
                    scale = fileIn.readDouble();
                }
            }

            line = fileIn.readLine();

        }

        // something went wrong
        return false;
    }
}