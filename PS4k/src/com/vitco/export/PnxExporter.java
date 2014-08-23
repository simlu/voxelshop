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
        // write dimension information
        int[] size = getSize();
        fileOut.writeIntRev(size[0]);
        fileOut.writeIntRev(size[1]);
        fileOut.writeIntRev(size[2]);

        // get and prepare variables
        int[] min = getMin();
        int[] max = getMax();

        setActivity("Exporting to file...", false);

        for (int x = max[0]; x > min[0] - 1; x--) {
            setProgress((1 - ((x - min[0])/(float)size[0]))*100);

            BufferedImage img = new BufferedImage(size[1], size[2], BufferedImage.TYPE_INT_ARGB);
            for (int y = max[1]; y > min[1] - 1; y--) {
                for (int z = max[2]; z > min[2] - 1; z--) {
                    Voxel voxel = data.searchVoxel(new int[]{x,y,z}, false);
                    if (voxel != null) {
                        Color color = voxel.getColor();
                        img.setRGB(y - min[1], z - min[2], color.getRGB());
                    }
                }
            }
            fileOut.writeImageCompressed(img);
            //fileOut.writeImage(img);
        }

        // success
        return true;
    }
}

