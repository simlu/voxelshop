package com.vitco.export;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.layout.content.console.ConsoleInterface;
import com.vitco.util.components.progressbar.ProgressDialog;
import com.vitco.util.file.FileOut;
import com.vitco.util.graphic.GraphicTools;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Exporter for *.pnx voxel format
 */
public class PnxExporter extends AbstractExporter {

    // constructor
    public PnxExporter(File exportTo, Data data, ProgressDialog dialog, ConsoleInterface console) throws IOException {
        super(exportTo, data, dialog, console);
    }

    // write the file
    @Override
    protected boolean writeFile() throws IOException {
        // for progress display
        setActivity("Exporting to file...", false);

        // write dimension information
        int[] overallSize = getSize();
        fileOut.writeIntRev(overallSize[0]);
        fileOut.writeIntRev(overallSize[1]);
        fileOut.writeIntRev(overallSize[2]);

        Integer[] layers = data.getLayers();

        // write amount of layers
        fileOut.writeIntRev(layers.length);

        // data cache
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileOut fileOutBuffer = new FileOut(baos);

        // holds unique images
        HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();
        ArrayList<String> imageOrder = new ArrayList<String>();

        for (int layerId : layers) {

            // write layer name
            String layerName = data.getLayerName(layerId);
            fileOutBuffer.writeIntRev(layerName.length());
            fileOutBuffer.writeUTF8String(layerName);

            // write visible flag
            fileOutBuffer.writeByte((byte) (data.getLayerVisible(layerId)?1:0));

            // write locked flag (not implemented yet)
            fileOutBuffer.writeByte((byte) 1);

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
            fileOutBuffer.writeIntRev(size[0]);
            fileOutBuffer.writeIntRev(size[1]);
            fileOutBuffer.writeIntRev(size[2]);

            // write minimum
            fileOutBuffer.writeIntRev(min[0]);
            fileOutBuffer.writeIntRev(min[1]);
            fileOutBuffer.writeIntRev(min[2]);

            // write image for layer
            for (int x = max[0]; x > min[0] - 1; x--) {
                BufferedImage img = new BufferedImage(size[1], size[2], BufferedImage.TYPE_INT_ARGB);
                for (Voxel voxel : data.getVoxelsYZ(x, layerId)) {
                    Color color = voxel.getColor();
                    img.setRGB(voxel.y - min[1], voxel.z - min[2], color.getRGB());
                }
                // hash and store if necessary
                String md5 = GraphicTools.getHash(img);
                if (!images.containsKey(md5)) {
                    images.put(md5, img);
                    imageOrder.add(md5);
                }
                fileOutBuffer.writeIntRev(imageOrder.indexOf(md5));
            }
        }

        // write images
        int count = imageOrder.size();
        int currentCount = 0;
        fileOut.writeIntRev(count);
        for (String md5 : imageOrder) {
            setProgress((currentCount / (float) count) * 100);
            fileOut.writeImageCompressed(images.get(md5));
            currentCount++;
        }

        // write data cache
        fileOut.writeBytes(baos.toByteArray());

        // success
        return true;
    }
}

