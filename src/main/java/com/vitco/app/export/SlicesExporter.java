package com.vitco.app.export;

import com.vitco.app.core.data.Data;
import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.layout.content.console.ConsoleInterface;
import com.vitco.app.util.components.progressbar.ProgressDialog;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Export cross-section slices of the voxel into
 * series of bitmap files.
 */
public class SlicesExporter extends AbstractExporter {

    public SlicesExporter(File exportTo, Data data, ProgressDialog dialog, ConsoleInterface console) throws IOException {
        super(exportTo, data, dialog, console);
    }

    private int ax1 = -1;
    private int ax2 = -1;
    private int ax3 = -1;
    public void setSliceDirection(String sliceAxis) {
        ax1 = sliceAxis.charAt(0) - 120; // x = 0, y = 1, z = 2
        ax2 = sliceAxis.equals("x") ? 2 : 0;
        ax3 = sliceAxis.equals("y") ? 2 : 1;
    }

    private String exportFormat = "png";
    public void setExportFormat(String format) {
        this.exportFormat = format;
    }

    private boolean invertOrder = false;
    public void setInvertOrder(boolean invert) {
        this.invertOrder = invert;
    }

    // allow control over how this is called
    public boolean generateImages() throws IOException {
        return writeFile();
    }

    @Override
    protected boolean writeFile() throws IOException {
        int[] size = getSize();
        int nSlices = size[ax1];  // number of slices
        int width = size[ax2];  // width of slice bitmaps
        int height = size[ax3];  // height of slice bitmaps

        // create images
        BufferedImage[] slices = new BufferedImage[nSlices];
        for (int idx = 0; idx < nSlices; idx++) {
            slices[idx] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        // paint images
        int[] min = getMin();
        for (Voxel voxel : this.data.getVisibleLayerVoxel()) {
            int[] pos = voxel.getPosAsInt();
            slices[pos[ax1] - min[ax1]].setRGB(pos[ax2] - min[ax2], pos[ax3] - min[ax3], voxel.getColor().getRGB());
        }

        // save images
        for (int idx = 0; idx < nSlices; idx++) {
            String fileName = String.format(
                    "%s_%d.%s",
                    exportTo.getAbsolutePath(),
                    invertOrder ? slices.length - idx : idx + 1,
                    exportFormat
            );
            System.out.println("Creating file: " + fileName);
            ImageIO.write(slices[idx], exportFormat, new File(fileName));
        }

        return true; // success
    }
}
