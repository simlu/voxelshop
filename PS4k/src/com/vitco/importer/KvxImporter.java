package com.vitco.importer;

import com.vitco.util.file.FileIn;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;

/**
 * Kvx importer
 */
public class KvxImporter extends AbstractImporter {
    // constructor
    public KvxImporter(File file, String name) throws IOException {
        super(file, name);
    }

    @Override
    protected boolean read(FileIn fileIn, RandomAccessFile raf) throws IOException {
        fileIn.readIntRev(); //int numbytes = fileIn.readIntRev();

        // read the dimensions
        int sx = fileIn.readIntRev();
        int sy = fileIn.readIntRev();
        int sz = fileIn.readIntRev();
        if (sx == 0 || sy == 0 || sz == 0) {
            return false;
        }

        // read center (discards precision)
        int cx = Math.round(fileIn.readIntRev()/256f);
        int cy = Math.round(fileIn.readIntRev()/256f);
        int cz = Math.round(fileIn.readIntRev()/256f);

        // read the amounts in the different dimensions
        int sumxoffset = 0;
        int sumxyoffset = 0;

        // contains the offsets
        int[] xoffset = new int[sx + 1];
        int[][] xyoffset = new int[sx][sy + 1];

        // read the xoffset
        int prevx = fileIn.readIntRev();
        xoffset[0] = prevx;
        for (int x = 1; x <= sx; x++) {
            int xoffSum = fileIn.readIntRev();
            int xoff = xoffSum - prevx;
            sumxoffset += xoff;
            prevx = xoffSum;
            xoffset[x] = xoffSum;
        }
        // read the xyoffset
        for (int x = 0; x < sx; x++) {
            int prevxy = 0;
            for (int y = 0; y <= sy; y++) {
                int xyoffSum = fileIn.readShortRevUnsigned();
                int xyoff = xyoffSum - prevxy;
                sumxyoffset += xyoff;
                prevxy = xyoffSum;
                xyoffset[x][y] = xyoffSum;
            }
        }

        // sanity checks
        if (sumxoffset != sumxyoffset) {
            return false;
        }
        if (xoffset[0] != (sx+1)*4 + sx*(sy+1)*2) {
            return false;
        }

        // Read the color palette (always at the end of the file)
        raf.seek(raf.length() - 768);
        int[] colPalette = new int[256];
        for (int i = 0; i < 256; i++) {
            int r = raf.read();
            int g = raf.read();
            int b = raf.read();
            colPalette[i] = new Color(r, g, b).getRGB();
        }

        int lastZ = 0;
        int lastCol = 0;
        // read the voxel data
        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {

                int start = xyoffset[x][y];
                int end = xyoffset[x][y+1];

                while (start < end) {
                    // read header bytes
                    int zpos = fileIn.readByteUnsigned();
                    int zlen = fileIn.readByteUnsigned();
                    int visfaces = fileIn.readByteUnsigned();
                    for (int i = 0; i < zlen; i++) {
                        // read color->voxel mapping and add
                        lastCol = colPalette[fileIn.readByteUnsigned()];
                        addVoxel(x - cx, y - cy, zpos + i - cz, lastCol);
                    }
                    start += zlen + 3;

                    // fill in voxels "in between"
                    BigInteger bigInteger = BigInteger.valueOf(visfaces);
                    if (!bigInteger.testBit(4)) {
                        for (int i = lastZ + 1; i < zpos; i++) {
                            addVoxel(x - cx, y - cy, i - cz, lastCol);
                        }
                    }
                    if (!bigInteger.testBit(5)) {
                        lastZ = zpos + zlen - 1;
                    }

                }

            }
        }

        return true;
    }
}
