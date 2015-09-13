package com.vitco.export;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.layout.content.console.ConsoleInterface;
import com.vitco.util.components.progressbar.ProgressDialog;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Exporter into *.vox format for the VOX game (http://www.vox-game.com/)
 */
public class VoxGameExporter extends AbstractExporter {

    // constructor
    public VoxGameExporter(File exportTo, Data data, ProgressDialog dialog, ConsoleInterface console) throws IOException {
        super(exportTo, data, dialog, console);
    }

    // write the file
    @Override
    protected boolean writeFile() throws IOException {
        // write dimension information
        int[] size = getSize();
        fileOut.writeBytes((size[0] + 1) + " " + (size[1] + 1) + " " + (size[2] + 1) + "\r\n\r\n");

        // get and prepare variables
        int[] min = getMin();
        int[] max = getMax();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        DecimalFormat df = new DecimalFormat("#.###", otherSymbols);
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setGroupingUsed(false);

        setActivity("Exporting to file...", false);

        // write data (set flag, r, g, b)
        for (int y = max[1]; y > min[1] - 1; y--) {
            setProgress((1 - ((y - min[1])/(float)size[1]))*100);
            for (int x = max[0]; x > min[0] - 1; x--) {
                for (int z = min[2]; z <= max[2]; z++) {
                    Voxel voxel = data.searchVoxel(new int[]{x,y,z}, false);
                    if (voxel == null) {
                        fileOut.writeBytes("0 1 1 1 ");
                    } else {
                        Color color = voxel.getColor();
                        fileOut.writeBytes(
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
