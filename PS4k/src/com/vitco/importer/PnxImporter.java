package com.vitco.importer;

import com.vitco.util.file.FileIn;
import com.vitco.util.file.RandomAccessFileIn;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * *.qb importer
 */
public class PnxImporter extends AbstractImporter {

    // constructor
    public PnxImporter(File file, String name) throws IOException {
        super(file, name);
    }

    @Override
    protected boolean read(FileIn fileIn, RandomAccessFileIn raf) throws IOException {
        int[] size = new int[]{fileIn.readIntRev(), fileIn.readIntRev(), fileIn.readIntRev()};

        for (int x = 0; x < size[0]; x++) {
            byte[] data = new byte[fileIn.readIntRev()];
            fileIn.read(data);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
            for (int y = 0; y < size[1]; y++) {
                for (int z = 0; z < size[2]; z++) {
                    int rgb = img.getRGB(y, z);
                    if ((rgb >> 24) != 0x00) {
                        addVoxel(x, y, z, rgb);
                    }
                }
            }
        }
        return true;
    }
}
