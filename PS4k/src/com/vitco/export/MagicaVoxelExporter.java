package com.vitco.export;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.layout.content.console.ConsoleInterface;
import com.vitco.util.components.progressbar.ProgressDialog;

import java.io.File;
import java.io.IOException;
import java.awt.Color;
import com.vitco.util.misc.BiMap;

public class MagicaVoxelExporter extends AbstractExporter {

    private final static int PALETTE_SIZE = 256;
    private final static int MV_VERSION = 150;

    public MagicaVoxelExporter(File exportTo, Data data, ProgressDialog dialog, ConsoleInterface console) throws IOException {
        super(exportTo, data, dialog, console);
    }

    // Write File
    @Override
    protected boolean writeFile() throws IOException {
        final int voxelsCount = data.getVisibleLayerVoxel().length;
        final int[][] sizeMeta = this.getSizeMeta();

        final BiMap<Integer, Color> palette = new BiMap<>();
        int currentPaletteIndex = PALETTE_SIZE - 2;
        this.setColorDefault(palette);

        // Magic number
        fileOut.writeASCIIString("VOX ");
        // Write version
        fileOut.writeIntRev(MV_VERSION);

        //MAIN Chunk
        fileOut.writeASCIIString("MAIN");
        fileOut.writeIntRev(0); // Content Size
        fileOut.writeIntRev((12 + 4 * 3) + (12 + 4 + 4 * voxelsCount) + (12 + 4 * PALETTE_SIZE));

        // Size Chunk
        fileOut.writeASCIIString("SIZE");
        fileOut.writeIntRev(4 * 3);
        fileOut.writeIntRev(0);
        fileOut.writeIntRev(sizeMeta[1][0] + sizeMeta[0][0]);
        fileOut.writeIntRev(sizeMeta[1][2] + sizeMeta[0][2]); // Maybe MV is Z-UP, Not sure but this just works
        fileOut.writeIntRev(sizeMeta[1][1] + sizeMeta[0][1]);


        //XYZI Chunk
        fileOut.writeASCIIString("XYZI");
        fileOut.writeIntRev(4 + 4 * voxelsCount);
        fileOut.writeIntRev(0);
        fileOut.writeIntRev(voxelsCount);

        for (Voxel voxel : data.getVisibleLayerVoxel()) {

            int colorId;
            if (!palette.containsValue(voxel.getColor())) {
                if (currentPaletteIndex < 0) {
                    console.addLine("Error: exporter format supports only 256 colors");
                    return false;
                }
                palette.put(currentPaletteIndex, voxel.getColor());
                colorId = currentPaletteIndex + 1;
                currentPaletteIndex -= 1;
            } else {
                colorId = palette.getKey(voxel.getColor()) + 1;
            }


            final int vx = sizeMeta[1][0] - voxel.x;
            final int vy = sizeMeta[1][1] - voxel.y;
            final int vz = sizeMeta[1][2] - voxel.z;

            fileOut.writeByte((byte)vx);
            fileOut.writeByte((byte)vz); // Maybe MV is Z-UP, Not sure but this just works
            fileOut.writeByte((byte)vy);
            fileOut.writeByte((byte)colorId);
        }

        // RGBA Chunk
        fileOut.writeASCIIString("RGBA");
        fileOut.writeIntRev(4 * PALETTE_SIZE);
        fileOut.writeIntRev(0);

        for (int i = 0; i < PALETTE_SIZE; i++) {
            final Color color = palette.get(i);
            fileOut.writeByte((byte) color.getRed());
            fileOut.writeByte((byte) color.getGreen());
            fileOut.writeByte((byte) color.getBlue());
            fileOut.writeByte((byte) 255);
        }
        return true;
    }

    // Just want to match MV default palette "look"
    private void setColorDefault(final BiMap<Integer, Color> palette) {
        for (int i = 0; i < PALETTE_SIZE; i++) {
            palette.put(i, new Color(75, 75, 75));
        }
    }

    private int[][] getSizeMeta() {
        int[] max = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        int[] min = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};

        for (Voxel voxel : data.getVisibleLayerVoxel()) {
            max[0] = Math.max(voxel.x, max[0]);
            max[1] = Math.max(voxel.y, max[1]);
            max[2] = Math.max(voxel.z, max[2]);

            min[0] = Math.min(voxel.x, min[0]);
            min[1] = Math.min(voxel.y, min[1]);
            min[2] = Math.min(voxel.z, min[2]);
        }

        return new int[][] {
                min, max
        };
    }
}