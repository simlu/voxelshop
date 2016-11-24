package com.vitco.app.importer;

import com.vitco.app.util.file.FileIn;
import com.vitco.app.util.file.RandomAccessFileIn;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * *.pnx importer
 */
public class PnxImporter extends AbstractImporter {

    // constructor
    public PnxImporter(File file, String name) throws IOException {
        super(file, name);
    }

    @Override
    protected boolean read(FileIn fileIn, RandomAccessFileIn raf) throws IOException {

        // read overall size
        fileIn.readIntRev(); fileIn.readIntRev(); fileIn.readIntRev();

        int layerCount = fileIn.readIntRev();

        // read all images
        int imageCount = fileIn.readIntRev();
        ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
        for (int i = 0; i < imageCount; i++) {
            images.add(fileIn.readImage());
        }

        for (int i = 0; i < layerCount; i++) {
            // read layer name
            int layerNameLength = fileIn.readIntRev();
            String layerName = fileIn.readUTF8String(layerNameLength);
            prependLayer(layerName);

            // read layer visibility
            boolean visible = fileIn.readByte() == 1;
            setLayerVisibility(visible);

            // read layer locked state
            fileIn.readByte(); // boolean locked = fileIn.readByte() == 0;

            // read layer size and corner
            int[] size = new int[]{fileIn.readIntRev(), fileIn.readIntRev(), fileIn.readIntRev()};
            int[] min = new int[]{fileIn.readIntRev(), fileIn.readIntRev(), fileIn.readIntRev()};

            // read layer slice by slice
            for (int x = size[0] + min[0] - 1; x > min[0] - 1; x--) {
                // read image for slice
                BufferedImage img = images.get(fileIn.readIntRev());
                for (int y = 0; y < size[1]; y++) {
                    for (int z = 0; z < size[2]; z++) {
                        //noinspection SuspiciousNameCombination
                        int rgb = img.getRGB(y, z);
                        if ((rgb >> 24) != 0x00) {
                            addVoxel(x, y + min[1], z + min[2], rgb);
                        }
                    }
                }
            }
        }
        return true;
    }
}
