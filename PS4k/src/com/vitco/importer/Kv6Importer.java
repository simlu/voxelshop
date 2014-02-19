package com.vitco.importer;

import com.vitco.util.file.FileIn;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Kv6 importer
 */
public class Kv6Importer extends AbstractImporter {

    // constructor
    public Kv6Importer(File file, String name) throws IOException {
        super(file, name);
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
        Integer lastZ = null;
        int c = 0;
        int invisibleVoxel = 0;
        ArrayList<int[]> addedVoxelList = new ArrayList<int[]>();
        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                int xyoff = fileIn.readShortRevUnsigned();
                sumxyoffset += xyoff;
                // create list first, order it and then check which voxel are missing (testing)
                for (int newC = c + xyoff; c < newC; c++) {
                    int[] vox = voxel.remove(0); // alternative "voxel.get(c)"
                    addedVoxelList.add(vox);
                    addVoxel(x - cx, y - cy, vox[0] - cz, vox[1]);
                    // some files don't count invisible voxel, so we need to track them
                    // for the sanity check
                    if (vox[2] == 0) {
                        invisibleVoxel++;
                    }
                }
                Collections.sort(addedVoxelList, new Comparator<int[]>() {
                    @Override
                    public int compare(int[] o1, int[] o2) {
                        return (int)Math.signum(o1[0] - o2[0]);
                    }
                });
                for (int[] vox : addedVoxelList) {
                    // fill in voxels "in between"
                    BigInteger bigInteger = BigInteger.valueOf(vox[2]);
                    if (lastZ != null && !bigInteger.testBit(4)) {
                        for (int i = lastZ + 1; i < vox[0]; i++) {
                            addVoxel(x - cx, y - cy, i - cz, vox[1]);
                        }
                    }
                    if (!bigInteger.testBit(5)) {
                        lastZ = vox[0];
                    }
                }
                lastZ = null;
                addedVoxelList.clear();
            }
        }

        return sumxoffset == sumxyoffset || sumxoffset == (sumxyoffset - invisibleVoxel);
    }
}
