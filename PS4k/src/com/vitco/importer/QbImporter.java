package com.vitco.importer;

import com.vitco.util.file.FileIn;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * *.qb importer
 */
public class QbImporter extends AbstractImporter {

    // constructor
    public QbImporter(File file, String name) throws IOException {
        super(file, name);
    }

    private static final int CODEFLAG = 2;
    private static final int NEXTSLICEFLAG = 6;

    @Override
    protected boolean read(FileIn fileIn, RandomAccessFile raf) throws IOException {
        fileIn.readIntRevUnsigned(); //int version = fileIn.readIntRevUnsigned();
        //System.out.println("version: " + version);
        int colorFormat = fileIn.readIntRevUnsigned();
        //System.out.println("colorFormat: " + colorFormat);
        int zAxisOrientation = fileIn.readIntRevUnsigned();
        //System.out.println("zAxisOrientation: " + zAxisOrientation);
        int compressed = fileIn.readIntRevUnsigned();
        //System.out.println("compressed: " + compressed);
        fileIn.readIntRevUnsigned(); // int visibilityMaskEncoded = fileIn.readIntRevUnsigned();
        //System.out.println("visibilityMaskEncoded: " + visibilityMaskEncoded);
        int numMatrices = fileIn.readIntRevUnsigned();
        //System.out.println("numMatrices: " + numMatrices);

        for (int i = 0; i < numMatrices; i++) {
            // read matrix name
            int nameLength = fileIn.readByteUnsigned();
            String name = fileIn.readASCIIString(nameLength);
            addLayer(name);
            //System.out.println("name: " + name);

            // read matrix size
            int sx = fileIn.readIntRevUnsigned();
            int sy = fileIn.readIntRevUnsigned();
            int sz = fileIn.readIntRevUnsigned();

            //System.out.println(sx + " " + sy + " " + sz);

            // read matrix size
            int cx = fileIn.readIntRev();
            int cy = fileIn.readIntRev();
            int cz = fileIn.readIntRev();

            //System.out.println(cx + " " + cy + " " + cz);

            ByteBuffer byteBuffer = ByteBuffer.allocate(4);

            if (compressed == 0) { // uncompressed
                for(int z = 0; z < sz; z++) {
                    for(int y = 0; y < sy; y++) {
                        for(int x = 0; x < sx; x++) {
                            int c1 = fileIn.readByteUnsigned();
                            int c2 = fileIn.readByteUnsigned();
                            int c3 = fileIn.readByteUnsigned();
                            int a = fileIn.readByteUnsigned(); // read visibility encoding
                            if (a != 0) { // if voxel is not invisible (this should work correctly in all cases)
                                int rgb = colorFormat == 0 ? new Color(c1,c2,c3).getRGB() : new Color(c3, c2, c1).getRGB();
                                if (zAxisOrientation == 1) {
                                    addVoxel(x + cx, y + cy, z + cz, rgb);
                                } else {
                                    addVoxel(z + cz, y + cy, x + cx, rgb);
                                }
                            }
                        }
                    }
                }
            } else { // compressed

                int z = 0;

                while (z < sz) {
                    z++;
                    int index = 0;

                    while (true) {
                        int data = fileIn.readIntRev();
                        if (data == NEXTSLICEFLAG) {
                            break;
                        } else if (data == CODEFLAG) {
                            int count = fileIn.readIntRevUnsigned();
                            data = fileIn.readIntRev();

                            for (int j = 0; j < count; j++) {
                                int x = index%sx + 1; // mod = modulo e.g. 12 mod 8 = 4
                                int y = index/sx + 1; // div = integer division e.g. 12 div 8 = 1
                                index++;
                                byteBuffer.position(0);
                                byteBuffer.putInt(data);
                                int c1 = byteBuffer.get(3) & 0x0000FF;
                                int c2 = byteBuffer.get(2) & 0x0000FF;
                                int c3 = byteBuffer.get(1) & 0x0000FF;
                                int a = byteBuffer.get(0) & 0x0000FF; // read visibility encoding
                                if (a != 0) { // if voxel is not invisible
                                    int rgb = colorFormat == 0 ? new Color(c1,c2,c3).getRGB() : new Color(c3, c2, c1).getRGB();
                                    if (zAxisOrientation == 1) {
                                        addVoxel(x + cx, y + cy, z + cz, rgb);
                                    } else {
                                        addVoxel(z + cz, y + cy, x + cx, rgb);
                                    }
                                }
                            }
                        } else {
                            int x = index%sx + 1;
                            int y = index/sx + 1;
                            index++;
                            byteBuffer.position(0);
                            byteBuffer.putInt(data);
                            int c1 = byteBuffer.get(3) & 0x0000FF;
                            int c2 = byteBuffer.get(2) & 0x0000FF;
                            int c3 = byteBuffer.get(1) & 0x0000FF;
                            int a = byteBuffer.get(0) & 0x0000FF; // read visibility encoding
                            if (a != 0) { // if voxel is not invisible
                                int rgb = colorFormat == 0 ? new Color(c1,c2,c3).getRGB() : new Color(c3, c2, c1).getRGB();
                                if (zAxisOrientation == 1) {
                                    addVoxel(x + cx, y + cy, z + cz, rgb);
                                } else {
                                    addVoxel(z + cz, y + cy, x + cx, rgb);
                                }
                            }
                        }
                    }
                }

            }

        }

        return true;
    }
}
