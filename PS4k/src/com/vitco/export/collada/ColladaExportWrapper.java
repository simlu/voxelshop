package com.vitco.export.collada;

import com.vitco.Main;
import com.vitco.core.data.Data;
import com.vitco.export.generic.ExportDataManager;
import com.vitco.manager.error.ErrorHandlerInterface;
import com.vitco.util.file.FileTools;
import com.vitco.util.misc.SaveResourceLoader;
import com.vitco.util.xml.XmlTools;

import java.io.File;

/**
 * Wrapper class for the collada export.
 */
public class ColladaExportWrapper {

    // true if every voxel layer should be exported as its own object
    private boolean useLayers = false;
    public final void setUseLayers(boolean state) {
        useLayers = state;
    }

    // setter for algorithm that is used
    private int algorithm = ExportDataManager.POLY2TRI_ALGORITHM;
    public final void setAlgorithm(int id) {
        algorithm = id;
    }

    // do the exporting
    public final boolean export(Data data, ErrorHandlerInterface errorHandler, File colladaFile) {
        boolean result = true;

        // define the prefix for the texture files
        String prefix = FileTools.extractNameWithoutExtension(colladaFile) + "_texture";

        // create data export objects
        ExportDataManager exportDataManager = new ExportDataManager(data, algorithm);
        ColladaFileExporter colladaFileExporter = new ColladaFileExporter(exportDataManager, prefix);

        // write the dae file
        if (!colladaFileExporter.writeToFile(colladaFile, errorHandler)) {
            result = false;
        }

        // write the texture files
        File folder = colladaFile.getParentFile();
        if (!colladaFileExporter.writeTexturesToFolder(folder, errorHandler)) {
            result = false;
        }

        // validation - only check in debug mode
        if (Main.isDebugMode()) {
            // validate the file
            if (!XmlTools.validateAgainstXSD(
                    colladaFile.getAbsolutePath(),
                    new SaveResourceLoader("resource/xsd/collada_schema_1_4_1.xsd").asStreamSource(),
                    errorHandler)) {
                result = false;
            }
        }

        return result;
    }
}
