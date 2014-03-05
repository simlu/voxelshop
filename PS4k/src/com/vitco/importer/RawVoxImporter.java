package com.vitco.importer;

import com.vitco.util.file.FileIn;
import com.vitco.util.file.RandomAccessFileIn;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * RawVox importer
 */
public class RawVoxImporter extends AbstractImporter {

    // the voxel color is not defined in the format, so
    // we need to define it here
    private static final int voxelRGB = new Color(158, 194, 88).getRGB();

    // the size of the block that contains the voxel as written in file
    private int sx = 0, sy = 0, sz = 0;
    public final double[] getSize() {
        return new double[] {sx, sy, sz};
    }

    // constructor
    public RawVoxImporter(File file, String layerName) throws IOException {
        super(file, layerName);
    }

    // read file - returns true if file has loaded correctly
    @Override
    protected boolean read(FileIn fileIn, RandomAccessFileIn raf) throws IOException {

        // header
        String header = fileIn.readASCIIString(4);
        if (!header.startsWith("XOVR")) { // check format identifier
            return false;
        }

        // read size of the voxel area
        sx = fileIn.readIntRev();
        sy = fileIn.readIntRev();
        sz = fileIn.readIntRev();

        // sanity check
        if (sx == 0 || sy == 0 || sz == 0) {
            // required parameter missing/wrong
            return false;
        }

        // read bits per voxel
        int bitsPerVoxel = fileIn.readIntRev();

        // sanity check
        if (bitsPerVoxel != 8 && bitsPerVoxel != 16 && bitsPerVoxel != 32) {
            return false;
        }

        // tmp value
        int bVal = 0;

        // read raw voxel data
        for (int z = 0; z < sz; z++) {
            for (int y = 0; y < sy; y++) {
                for (int x = 0; x < sx; x++) {
                    if (bitsPerVoxel == 8) {
                        bVal = fileIn.readByteUnsigned();
                    } else if (bitsPerVoxel == 16) {
                        bVal = fileIn.readByteUnsigned() + fileIn.readByteUnsigned();
                    } else if (bitsPerVoxel == 32) {
                        bVal = fileIn.readByteUnsigned() + fileIn.readByteUnsigned() + fileIn.readByteUnsigned() + fileIn.readByteUnsigned();
                    }
                    if (bVal > 0) {
                        addVoxel(-z,-y,x,voxelRGB);
                    }
                }
            }
        }

        return true;
    }
}
