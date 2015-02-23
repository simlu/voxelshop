package com.vitco.export;

import com.vitco.core.data.Data;
import com.vitco.importer.AbstractImporter;
import com.vitco.importer.QbImporter;
import com.vitco.util.components.progressbar.ProgressDialog;
import org.junit.Test;

import java.awt.*;
import java.io.File;

/**
 * Exporter test so that the program doesn't need to be loaded to test.
 */

public class ExporterTest {

    private final static String input_file = "C:\\Users\\flux\\Desktop\\nerds_zach.qb";
    private final static String output_file = "C:\\Users\\flux\\Desktop\\nerds_zach_out.qb";

    @Test
    public void testQBExporter() throws Exception {
        Data data = new Data();
        data.deleteLayer(data.getLayers()[0]);
        AbstractImporter importer = new QbImporter(new File(input_file), "Import");
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

        AbstractExporter exporter = new QbExporter(new File(output_file), data, new ProgressDialog(null));
        exporter.writeData();

        new QbImporter(new File(output_file), "Import");
    }

}
