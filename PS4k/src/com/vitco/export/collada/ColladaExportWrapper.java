package com.vitco.export.collada;

import com.vitco.Main;
import com.vitco.core.data.Data;
import com.vitco.export.generic.ExportDataManager;
import com.vitco.layout.content.console.ConsoleInterface;
import com.vitco.manager.error.ErrorHandlerInterface;
import com.vitco.util.components.progressbar.ProgressDialog;
import com.vitco.util.components.progressbar.ProgressReporter;
import com.vitco.util.file.FileTools;
import com.vitco.util.misc.SaveResourceLoader;
import com.vitco.util.xml.XmlTools;

import java.io.File;

/**
 * Wrapper class for the collada export.
 */
public class ColladaExportWrapper extends ProgressReporter {

    // the object name (only used if layers are not exported separately)
    private String objectName = "modelTEX";

    // constructor
    public ColladaExportWrapper(ProgressDialog dialog, ConsoleInterface console) {
        super(dialog, console);
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    // true if every voxel layer should be exported as its own object
    private boolean useLayers = false;
    public final void setUseLayers(boolean state) {
        useLayers = state;
    }

    // true if holes should be removed
    private boolean removeHoles = true;
    public final void setRemoveHoles(boolean state) {
        removeHoles = state;
    }

    // true if texture padding is enabled
    private boolean padTextures = true;
    public final void setPadTextures(boolean state) {
        padTextures = state;
    }

    // true if colored vertices should be used
    private boolean useColoredVertices = false;
    public final void setUseColoredVertices(boolean state) {
        useColoredVertices = state;
    }

    // true if the export should have the "black outline"
    private boolean useBlackOutline = false;
    public final void setUseBlackOutline(boolean state) {
        useBlackOutline = state;
    }

    // true if textures are forced to be power of two dimensions
    private boolean forcePOT = false;
    public final void setForcePOT(boolean state) {
        forcePOT = state;
    }

    // setter for algorithm that is used
    private int algorithm = ExportDataManager.POLY2TRI_ALGORITHM;
    public final void setAlgorithm(int id) {
        algorithm = id;
    }

    // setter for Y-UP instead of Z-UP
    private boolean useYUP = false;
    public final void setUseYUP(boolean useYUP) {
        this.useYUP = useYUP;
    }

    private boolean exportOrthogonalVertexNormals = false;
    public void setExportOrthogonalVertexNormals(boolean exportOrthogonalVertexNormals) {
        this.exportOrthogonalVertexNormals = exportOrthogonalVertexNormals;
    }

    // the origin mode
    public static final int ORIGIN_CROSS = 0;
    public static final int ORIGIN_CENTER = 1;
    public static final int ORIGIN_PLANE_CENTER = 2;
    public static final int ORIGIN_BOX_CENTER = 3;
    public static final int ORIGIN_BOX_PLANE_CENTER = 4;
    // setter for origin mode
    private int originMode = ORIGIN_CROSS;
    public final void setOriginMode(int originMode) {
        this.originMode = originMode;
    }

    // do the exporting
    public final boolean export(Data data, ErrorHandlerInterface errorHandler, File colladaFile) {
        boolean result = true;

        // define the prefix for the texture files
        String prefix = FileTools.extractNameWithoutExtension(colladaFile) + "_texture";

        // create data export objects
        ExportDataManager exportDataManager = new ExportDataManager(getProgressDialog(), getConsole(), data, padTextures, removeHoles, algorithm, useYUP, originMode, forcePOT, useLayers);
        ColladaFileExporter colladaFileExporter = new ColladaFileExporter(getProgressDialog(), getConsole(), exportDataManager, prefix, objectName, useYUP, exportOrthogonalVertexNormals);

        setActivity("Writing Data File...", true);
        // write the dae file
        if (!colladaFileExporter.writeToFile(colladaFile, errorHandler)) {
            result = false;
        }

        setActivity("Writing Textures...", true);
        // write the texture files
        File folder = colladaFile.getParentFile();
        if (!colladaFileExporter.writeTexturesToFolder(folder, errorHandler)) {
            result = false;
        }

        // validation - only check in debug mode
        if (Main.isDebugMode()) {
            setActivity("Validating File...", true);
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
