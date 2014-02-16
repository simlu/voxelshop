package com.vitco.importer;

import com.vitco.util.file.FileIn;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Kv6 importer
 */
public class Kv6Importer extends AbstractImporter {

    public Kv6Importer(File file) throws IOException {
        super(file);
    }

    // ---------

    @Override
    protected boolean read(FileIn fileIn, RandomAccessFile raf) throws IOException {
        // check that this is a valid file (header correct)
        if (fileIn.readIntRev() != 0x6c78764b) {
            return false;
        }

        // read the dimensions
        int sx = fileIn.readIntRev();
        int sy = fileIn.readIntRev();
        int sz = fileIn.readIntRev();
        if (sx == 0 || sy == 0 || sz == 0) {
            return false;
        }

        // read center
        int cx = Math.round(fileIn.readFloatRev());
        int cy = Math.round(fileIn.readFloatRev());
        int cz = Math.round(fileIn.readFloatRev());

        // read the amount of voxel that have a visible side
        long count = fileIn.readIntRev();

        // voxel info list (we don't know yet where they are in the world)
        ArrayList<int[]> voxel = new ArrayList<int[]>();

        // read the colors and some other data
        for (int c = 0; c < count; c++) {
            // treat as unsigned byte
            int b = fileIn.readByteUnsigned();
            int g = fileIn.readByteUnsigned();
            int r = fileIn.readByteUnsigned();
            fileIn.readByteUnsigned(); //int l = fileIn.readByteUnsigned();
            Color color = new Color(r, g, b);
            // -----------
            int zpos = fileIn.readShortRevUnsigned();
            int visfaces = fileIn.readByteUnsigned();
            fileIn.readByteUnsigned(); //int lighting = fileIn.readByteUnsigned();

            voxel.add(new int[]{zpos, color.getRGB(), visfaces});
        }

        int sumxoffset = 0;
        int sumxyoffset = 0;

        // read the xoffset
        for (int x = 0; x < sx; x++) {
            int xoff = fileIn.readIntRev();
            sumxoffset += xoff;
        }
        // read the xyoffset
        int lastZ = 0;
        int c = 0;
        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                int xyoff = fileIn.readShortRevUnsigned();
                sumxyoffset += xyoff;
                for (int newC = c + xyoff; c < newC; c++) {
                    int[] vox = voxel.remove(0); // alternative "voxel.get(c)"
                    addVoxel(x - cx, y - cy, vox[0] - cz, vox[1]);

                    // fill in voxels "in between"
                    BigInteger bigInteger = BigInteger.valueOf(vox[2]);
                    if (!bigInteger.testBit(4)) {
                        for (int i = lastZ + 1; i < vox[0]; i++) {
                            addVoxel(x - cx, y - cy, i - cz, vox[1]);
                        }
                    }
                    if (!bigInteger.testBit(5)) {
                        lastZ = vox[0];
                    }
                }
            }
        }

        // sanity check
        return sumxoffset == sumxyoffset;
    }
}
