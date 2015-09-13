package com.vitco.export;

import com.vitco.core.data.Data;
import com.vitco.importer.AbstractImporter;
import com.vitco.importer.VoxImporter;
import com.vitco.util.components.progressbar.ProgressDialog;
import org.junit.Test;

import java.awt.*;
import java.io.File;

/**
 * Exporter test so that the program doesn't need to be loaded to test.
 */

public class ExporterTest {

    private final static String input_file = "C:\\Users\\flux\\Desktop\\Troll_VOX\\troll_armright.vox";
    private final static String output_file = "C:\\Users\\flux\\Desktop\\Troll_VOX\\troll_armright_export.vox";

    @Test
    public void testVoxExporter() throws Exception {
        Data data = new Data();
        data.deleteLayer(data.getLayers()[0]);
        AbstractImporter importer = new VoxImporter(new File(input_file), "Import");
        if (importer.hasLoaded()) {
            System.out.println("Loading finished.");
        }

        for (AbstractImporter.Layer layer : importer.getVoxel()) {
            int layerId = data.createLayer(layer.name);
            data.selectLayer(layerId);
            data.setVisible(layerId, layer.isVisible());
            for (int[] vox; layer.hasNext();) {
                vox = layer.next();
                data.addVoxelDirect(new Color(vox[3]),new int[] {vox[0], vox[1], vox[2]});
            }
        }

        VoxVoxLapExporter exporter = new VoxVoxLapExporter(new File(output_file), data, new ProgressDialog(null), null);
        exporter.writeData();

        new VoxImporter(new File(output_file), "Import");
    }

}
