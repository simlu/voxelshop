package com.vitco.export;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.layout.content.console.ConsoleInterface;
import com.vitco.util.components.progressbar.ProgressDialog;
import com.vitco.util.misc.BiMap;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MagicaVoxelExporter extends AbstractExporter {

    private final static int PALETTE_SIZE = 256;
    private final static int MV_VERSION = 150;
    private final static int MV_DEFAULT_PALETTE_COLOR = new Color(75, 75, 75).getRGB();

    public MagicaVoxelExporter(File exportTo, Data data, ProgressDialog dialog, ConsoleInterface console) throws IOException {
        super(exportTo, data, dialog, console);
    }

    // Write File
    @Override
    protected boolean writeFile() throws IOException {

        final int[] min = getMin();
        final int[] max = getMax();

        final int[] colors = getColors();
        if (colors.length > PALETTE_SIZE) {
            console.addLine("Error: MagicaVoxel *.vox format only supports 256 colors.");
            return false;
        }
        // prepare palette
        BiMap<Integer, Integer> colorPalette = new BiMap<Integer, Integer>();
        for (int i = 0; i < colors.length; i++) {
            colorPalette.put(colors[i], i);
        }

        // Magic number
        fileOut.writeASCIIString("VOX ");
        // Write version
        fileOut.writeIntRev(MV_VERSION);

        // MAIN Chunk
        fileOut.writeASCIIString("MAIN");
        fileOut.writeIntRev(0); // Content Size
        fileOut.writeIntRev((12 + 4 * 3) + (12 + 4 + 4 * getCount()) + (12 + 4 * PALETTE_SIZE));

        // Size Chunk
        fileOut.writeASCIIString("SIZE");
        fileOut.writeIntRev(4 * 3);
        fileOut.writeIntRev(0);
        fileOut.writeIntRev(max[0] + min[0]);
        fileOut.writeIntRev(max[2] + min[2]); // Maybe MV is Z-UP, Not sure but this just works
        fileOut.writeIntRev(max[1] + min[1]);


        // XYZI Chunk
        fileOut.writeASCIIString("XYZI");
        fileOut.writeIntRev(4 + 4 * getCount());
        fileOut.writeIntRev(0);
        fileOut.writeIntRev(getCount());

        for (Voxel voxel : data.getVisibleLayerVoxel()) {
            int colorId = colorPalette.get(voxel.getColor().getRGB()) + 1;

            final int vx = max[0] - voxel.x;
            final int vy = max[1] - voxel.y;
            final int vz = max[2] - voxel.z;

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
            final int rgb = colorPalette.containsValue(i) ? colorPalette.getKey(i) : MV_DEFAULT_PALETTE_COLOR;
            fileOut.writeByte((byte) ((rgb >> 16) & 0xFF));
            fileOut.writeByte((byte) ((rgb >> 8) & 0xFF));
            fileOut.writeByte((byte) (rgb & 0xFF));
            fileOut.writeByte((byte) 255);
        }
        return true;
    }
}