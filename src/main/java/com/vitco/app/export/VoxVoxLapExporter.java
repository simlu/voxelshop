package com.vitco.app.export;

import com.vitco.app.core.data.Data;
import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.layout.content.console.ConsoleInterface;
import com.vitco.app.util.components.progressbar.ProgressDialog;
import com.vitco.app.util.misc.BiMap;

import java.io.File;
import java.io.IOException;

/**
 * Exporter for the *.vox file for the VoxLap engine.
 */
public class VoxVoxLapExporter extends AbstractExporter {

    private static final int PALETTE_SIZE = 255;

    // constructor
    public VoxVoxLapExporter(File exportTo, Data data, ProgressDialog dialog, ConsoleInterface console) throws IOException {
        super(exportTo, data, dialog, console);
    }

    // write the file
    @Override
    protected boolean writeFile() throws IOException {
        // write dimension information (x, z, y)
        int[] size = getSize();
        fileOut.writeIntRev(size[0]);
        fileOut.writeIntRev(size[2]);
        fileOut.writeIntRev(size[1]);

        // prepare palette
        int[] colors = getColors();
        if (colors.length >= PALETTE_SIZE) { // last entry reserved for empty block
            console.addLine("Error: VoxLap Engine *.vox format only supports " + PALETTE_SIZE + " colors.");
            return false;
        }
        BiMap<Integer, Integer> colorPalette = new BiMap<>();
        for (int i = 0; i < colors.length; i++) {
            colorPalette.put(colors[i], i);
        }

        // fetch min and max so we can loop
        int[] min = getMin();
        int[] max = getMax();

        // write the voxel data
        for (int x = min[0]; x <= max[0]; x++) {
            for (int z = max[2]; z > min[2] - 1; z--) {
                for (int y = min[1]; y <= max[1]; y++) {
                    Voxel voxel = data.searchVoxel(new int[]{x,y,z}, false);
                    if (voxel == null) {
                        fileOut.writeByte((byte)255);
                    } else {
                        int colorId = colorPalette.get(voxel.getColor().getRGB());
                        fileOut.writeByte((byte) colorId);
                    }
                }
            }
        }

        // write the palette
        for (int i = 0; i <= PALETTE_SIZE; i++) {
            int rgb = colorPalette.containsValue(i) ? colorPalette.getKey(i) : 0;
            fileOut.writeBytes(new byte[] { // write rgb
                    (byte) Math.round((((rgb >> 16) & 0xFF) * 63) / 255f),
                    (byte) Math.round((((rgb >> 8) & 0xFF) * 63) / 255f),
                    (byte) Math.round(((rgb & 0xFF) * 63) / 255f)
            });
        }

        // success
        return true;
    }
}
