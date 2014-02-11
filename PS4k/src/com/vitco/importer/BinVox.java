package com.vitco.importer;

import com.vitco.util.file.FileIn;

import java.io.File;
import java.io.IOException;

public class BinVox {
    private byte[] voxels;
    private int sx, sy, sz = 0; // size x,y,z
    private double tx, ty, tz = 0; // translation as written in file
    private long cx, cy, cz = 0; // weighted center
    private int lx, ly, lz = Integer.MAX_VALUE; // lowest voxel value
    private int hx, hy, hz = Integer.MIN_VALUE; // highest voxel value
    private double scale; // scale as written in file
    private int version; // version as written in file
    private long voxelCount; // how many voxels are in this file
    private boolean hasLoaded; // true if the file was loaded correctly

    // constructor
    public BinVox(File file) throws IOException {
        FileIn inputStream = new FileIn(file);
        hasLoaded = read(inputStream);
        inputStream.finish();
    }

    // ----------

    // getter
    public double getScale() {
        return scale;
    }

    // getter
    public int getVersion() {
        return version;
    }

    // getter
    public double[] getTranslation() {
        return new double[] {tx, ty, tz};
    }

    // getter
    public int[] getCenter() {
        return new int[] {(int) (cx/voxelCount), (int) (cy/voxelCount), (int) (cz/voxelCount)};
    }

    // getter
    public int[] getMin() {
        return new int[] {lx, ly, lz};
    }

    // getter
    public int[] getMax() {
        return new int[] {hx, hy, hz};
    }

    // getter
    public long getVoxelCount() {
        return voxelCount;
    }

    // getter
    public byte[] getVoxels() {
        return voxels.clone();
    }

    // getter
    public int[] getSize() {
        return new int[] {sx, sy, sz};
    }

    public boolean hasLoaded() {
        return hasLoaded;
    }

    // -----------------------

    // read the file, returns true if everything went ok
    private boolean read(FileIn inputStream) throws IOException {
        // header
        String line = inputStream.readLine();
        if (!line.startsWith("#binvox")) { // not a bin vox format
            return false;
        }

        // version
        String version_string = line.substring(8);
        version = Integer.parseInt(version_string);

        line = inputStream.readLine();

        while (null != line) {

            if (line.startsWith("data")) {
                if (sx == 0 || sy == 0 || sz == 0) {
                    // required parameter missing/wrong
                    return false;
                }
                // define the size
                int size = sx * sy * sz;
                // read voxel data
                voxels = new byte[size];
                byte value;
                int count = -1;
                int index = 0;
                int end_index = 0;
                int nr_voxels = 0;

                while (end_index < size && count != 0) {

                    value = inputStream.readByte();
                    // convert into unsigned
                    count = inputStream.readByte() & 0xff;

                    end_index = index + count;
                    if (end_index > size) {
                        return false;
                    }
                    if (value == 1) {
                        for (int i = index; i < end_index; i++) {
                            int x = i % sx;
                            int y = (i / sx) % sz;
                            int z = (i / (sx *sy));
                            lx = Math.min(lx, x);
                            ly = Math.min(ly, y);
                            lz = Math.min(lz, z);
                            hx = Math.max(hx, x);
                            hy = Math.max(hy, y);
                            hz = Math.max(hz, z);
                            cx += x;
                            cy += y;
                            cz += z;
                            voxels[i] = value;
                        }
                    }

                    if (value > 0) {
                        nr_voxels += count;
                    }
                    index = end_index;
                }

                voxelCount = nr_voxels;

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
                    sz = inputStream.readInt();
                    sy = inputStream.readInt();
                    sx = inputStream.readInt();
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
                    tx = inputStream.readDouble();
                    ty = inputStream.readDouble();
                    tz = inputStream.readDouble();
                }
            } else if (line.startsWith("scale")) {
                String[] scaleLine = line.split(" ");
                if (scaleLine.length == 2) {
                    // the values are in the same line
                    scale = Double.parseDouble(scaleLine[1]);
                } else {
                    // the value is in the next line
                    scale = inputStream.readDouble();
                }
            }

            line = inputStream.readLine();

        }

        // something went wrong
        return false;
    }
}