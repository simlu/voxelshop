package com.vitco.export;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Exporter into *.vox format for the VOX game (http://www.vox-game.com/)
 */
public class VoxGameExporter extends AbstractExporter {

    // constructor
    public VoxGameExporter(File exportTo, Data data) throws IOException {
        super(exportTo, data);
    }

    // write the file
    @Override
    protected boolean writeFile() throws IOException {
        // write dimension information
        int[] size = getSize();
        raf.writeBytes((size[0]+1) + " " + (size[1]+1) + " " + (size[2]+1) + "\r\n\r\n");

        // get and prepare variables
        int[] min = getMin();
        int[] max = getMax();
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.HALF_UP);

        // write data (set flag, r, g, b)
        for (int y = max[1]; y > min[1] - 1; y--) {
            for (int x = max[0]; x > min[0] - 1; x--) {
                for (int z = min[2]; z <= max[2]; z++) {
                    Voxel voxel = data.searchVoxel(new int[]{x,y,z}, false);
                    if (voxel == null) {
                        raf.writeBytes("0 1 1 1 ");
                    } else {
                        Color color = voxel.getColor();
                        raf.writeBytes(
                                "1 " +
                                df.format(color.getRed()/255f) + " " +
                                df.format(color.getGreen()/255f) + " " +
                                df.format(color.getBlue()/255f) + " "
                        );
                    }
                }
            }
        }

        // success
        return true;
    }
}
