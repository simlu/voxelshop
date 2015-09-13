package com.vitco.export;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.layout.content.console.ConsoleInterface;
import com.vitco.util.components.progressbar.ProgressDialog;
import com.vitco.util.misc.BiMap;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Exporter for the *.vox file for the VoxLap engine.
 */
public class VoxVoxLapExporter extends AbstractExporter {

    // constructor
    public VoxVoxLapExporter(File exportTo, Data data, ProgressDialog dialog, ConsoleInterface console) throws IOException {
        super(exportTo, data, dialog, console);
    }

    // write the file
    @Override
    protected boolean writeFile() throws IOException {

        // write dimension information
        int[] size = getSize();
        int sx = size[0];
        int sy = size[2];
        int sz = size[1];
        fileOut.writeIntRev(sx);
        fileOut.writeIntRev(sy);
        fileOut.writeIntRev(sz);

        // get and prepare variables
        int[] min = getMin();
        int[] max = getMax();

        // current position in palette
        Byte count = 0;
        BiMap<Color, Byte> palette = new BiMap<Color, Byte>();

        // write the voxel data
        for (int x = min[0]; x <= max[0]; x++) {
            for (int z = max[2]; z > min[2] - 1; z--) {
                for (int y = min[1]; y <= max[1]; y++) {
                    Voxel voxel = data.searchVoxel(new int[]{x,y,z}, false);
                    if (voxel == null) {
                        fileOut.writeByte((byte)255);
                    } else {
                        Color color = voxel.getColor();
                        Byte id = palette.get(color);
                        if (id == null) {
                            palette.put(color, count);
                            id = count;
                            if (id > 254) { // color 255 is reserved for empty block
                                console.addLine("Error: More than 254 colors not allowed for selected format.");
                                return false;
                            }
                            count++;
                        }
                        fileOut.writeByte(id);
                    }
                }
            }
        }

        // write the palette
        for (int i = 0; i < 256; i++) {
            Color col = palette.getKey((byte) i);
            if (col != null) {
                byte r = (byte) Math.round((col.getRed() * 63) / 255f);
                byte g = (byte) Math.round((col.getGreen() * 63) / 255f);
                byte b = (byte) Math.round((col.getBlue() * 63) / 255f);
                fileOut.writeBytes(new byte[] {r, g, b});
            } else {
                fileOut.writeBytes(new byte[3]);
            }
        }

        // success
        return true;
    }
}
