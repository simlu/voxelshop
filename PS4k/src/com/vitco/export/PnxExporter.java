package com.vitco.export;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.util.components.progressbar.ProgressDialog;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Exporter for *.pnx voxel format
 */
public class PnxExporter extends AbstractExporter {

    // constructor
    public PnxExporter(File exportTo, Data data, ProgressDialog dialog) throws IOException {
        super(exportTo, data, dialog);
    }

    // write the file
    @Override
    protected boolean writeFile() throws IOException {
        // for progress display
        setActivity("Exporting to file...", false);
        int count = getCount();
        int currentCount = 0;

        // write dimension information
        int[] overallSize = getSize();
        fileOut.writeIntRev(overallSize[0]);
        fileOut.writeIntRev(overallSize[1]);
        fileOut.writeIntRev(overallSize[2]);

        Integer[] layers = data.getLayers();

        // write amount of layers
        fileOut.writeIntRev(layers.length);

        for (int layerId : layers) {

            // write layer name
            String layerName = data.getLayerName(layerId);
            fileOut.writeIntRev(layerName.length());
            fileOut.writeUTF8String(layerName);

            // write visible flag
            fileOut.writeByte((byte) (data.getLayerVisible(layerId)?1:0));

            // write locked flag (not implemented yet)
            fileOut.writeByte((byte) 1);

            // get min and max of layer
            int[] min = new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
            int[] max = new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
            int[] size = new int[] {0, 0, 0};
            boolean hasVoxel = false;
            for (Voxel voxel : data.getLayerVoxels(layerId)) {
                min[0] = Math.min(voxel.x, min[0]);
                min[1] = Math.min(voxel.y, min[1]);
                min[2] = Math.min(voxel.z, min[2]);
                max[0] = Math.max(voxel.x, max[0]);
                max[1] = Math.max(voxel.y, max[1]);
                max[2] = Math.max(voxel.z, max[2]);
                hasVoxel = true;
            }
            if (hasVoxel) {
                size = new int[]{max[0] - min[0] + 1, max[1] - min[1] + 1, max[2] - min[2] + 1};
            }

            // write size
            fileOut.writeIntRev(size[0]);
            fileOut.writeIntRev(size[1]);
            fileOut.writeIntRev(size[2]);

            // write minimum
            fileOut.writeIntRev(min[0]);
            fileOut.writeIntRev(min[1]);
            fileOut.writeIntRev(min[2]);

            // write image for layer
            for (int x = max[0]; x > min[0] - 1; x--) {
                setProgress((currentCount / (float) count) * 100);
                BufferedImage img = new BufferedImage(size[1], size[2], BufferedImage.TYPE_INT_ARGB);
                for (Voxel voxel : data.getVoxelsYZ(x, layerId)) {
                    currentCount++;
                    Color color = voxel.getColor();
                    img.setRGB(voxel.y - min[1], voxel.z - min[2], color.getRGB());
                }
                fileOut.writeImageCompressed(img);
            }

        }

        // success
        return true;
    }
}

