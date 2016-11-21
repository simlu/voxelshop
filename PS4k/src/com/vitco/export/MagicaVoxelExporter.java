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

    private final static int PALETTE_SIZE = 255;
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
        final int[] size = getSize();

        final int[] colors = getColors();
        if (colors.length > PALETTE_SIZE) {
            console.addLine("Error: MagicaVoxel *.vox format only supports " + PALETTE_SIZE + " colors.");
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
        fileOut.writeIntRev(0); // Main Chunk has no data, only children
        // size of (1) "SIZE" chunk + (2) "XYZI" chunk + (3) "RGBA" chunk
        fileOut.writeIntRev((12 + 4 * 3) + (12 + 4 + 4 * getCount()) + (12 + 4 * PALETTE_SIZE + 4));

        // Size Chunk
        fileOut.writeASCIIString("SIZE");
        fileOut.writeIntRev(4 * 3); // needed to store size
        fileOut.writeIntRev(0); // no children
        fileOut.writeIntRev(size[0]);
        fileOut.writeIntRev(size[2]);
        fileOut.writeIntRev(size[1]);

        // XYZI Chunk
        fileOut.writeASCIIString("XYZI");
        fileOut.writeIntRev(4 + 4 * getCount()); // needed to store size
        fileOut.writeIntRev(0); // no children
        fileOut.writeIntRev(getCount());

        for (Voxel voxel : data.getVisibleLayerVoxel()) {
            final int vx = -(voxel.x - max[0]);
            final int vy = -(voxel.y - max[1]);
            final int vz = voxel.z - min[2];
            final int colorId = colorPalette.get(voxel.getColor().getRGB()) + 1;

            fileOut.writeByte((byte)vx);
            fileOut.writeByte((byte)vz);
            fileOut.writeByte((byte)vy);
            fileOut.writeByte((byte)colorId);
        }

        // RGBA Chunk
        fileOut.writeASCIIString("RGBA");
        fileOut.writeIntRev(4 * PALETTE_SIZE + 4);
        fileOut.writeIntRev(0);

        for (int i = 0; i < PALETTE_SIZE; i++) {
            final int rgb = colorPalette.containsValue(i) ? colorPalette.getKey(i) : MV_DEFAULT_PALETTE_COLOR;
            fileOut.writeByte((byte) ((rgb >> 16) & 0xFF));
            fileOut.writeByte((byte) ((rgb >> 8) & 0xFF));
            fileOut.writeByte((byte) (rgb & 0xFF));
            fileOut.writeByte((byte) 255);
        }
        // last color is black
        fileOut.writeIntRev(0xff000000);
        return true;
    }
}