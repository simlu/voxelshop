package com.vitco.core.data;

import com.vitco.Main;
import com.vitco.core.data.container.DataContainer;
import com.vitco.export.ColladaFileExporter;
import com.vitco.export.ExportDataManager;
import com.vitco.manager.error.ErrorHandlerInterface;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.file.FileTools;
import com.vitco.util.misc.SaveResourceLoader;
import com.vitco.util.xml.XmlTools;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;

/**
 * Data class that puts everything together and defines general data interaction (e.g. save/load)
 */
public final class Data extends VoxelHighlighting implements DataInterface {

    @Override
    public final void clearHistoryA() {
        synchronized (VitcoSettings.SYNC) {
            historyManagerA.clear();
        }
    }

    @Override
    public final void clearHistoryV() {
        synchronized (VitcoSettings.SYNC) {
            historyManagerV.clear();
        }
    }

    // var & setter
    private ErrorHandlerInterface errorHandler;
    @Autowired(required=true)
    public final void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    public Data() {
        super();
        freshStart();
    }

    // called when data structure is initialized
    private void initialNotification() {
        notifier.onAnimationDataChanged();
        notifier.onVoxelDataChanged();
        notifier.onTextureDataChanged();
        notifier.onLayerStateChanged();
    }

    @Override
    public final void freshStart() {
        synchronized (VitcoSettings.SYNC) {
            // reset
            dataContainer = new DataContainer();
            // create initial layer and select it
            selectLayerSoft(createLayer("Layer"));
            // remove history
            clearHistoryA();
            clearHistoryV();
            initialNotification();
            // file has not changed yet
            hasChanged = false;
        }
    }

    @Override
    public final boolean loadFromFile(File file) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            // VSD file format
            DataContainer loaded = new DataContainer(file, errorHandler);
            if (loaded.hasLoaded) {
                clearHistoryA();
                clearHistoryV();
                dataContainer = loaded;

                result = true;
            } else { // todo remove legacy support (later)
                // old file format
                Object loadedLegacy = FileTools.loadFromFile(file, errorHandler);
                if (loadedLegacy != null) {
                    clearHistoryA();
                    clearHistoryV();
                    dataContainer = (DataContainer) loadedLegacy;
                    if (dataContainer.textures == null) {
                        dataContainer.textures = new HashMap<Integer, ImageIcon>();
                    }
                    result = true;
                }
            }
            if (result) {
                invalidateA();
                invalidateV(null);
                initialNotification();
                // file has not changed yet
                hasChanged = false;
            }
            return result;
        }
    }

    @Override
    public final boolean saveToFile(File file) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = dataContainer.saveToVsdFile(file, errorHandler);
            if (result) {
                hasChanged = false;
            }
            return result;
        }
    }

    // todo: move this method somewhere more appropriate (it doesn't belong into the data class!)
    @Override
    public final boolean exportToCollada(File file, File textureFile) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = true;

            // define the prefix for the texture filex
            String prefix = FileTools.extractNameWithoutExtension(file) + "_texture";

            // create data export objects
            ExportDataManager exportDataManager = new ExportDataManager(this);
            ColladaFileExporter colladaFileExporter = new ColladaFileExporter(exportDataManager, prefix);

            // write the dae file
            if (!colladaFileExporter.writeToFile(file, errorHandler)) {
                result = false;
            }

            // write the texture files
            File folder = file.getParentFile();
            if (!colladaFileExporter.writeTexturesToFolder(folder, errorHandler)) {
                result = false;
            }

//            // hull manager that exposes hull information
//            Voxel[] voxels = getVisibleLayerVoxel();
//            HullManager<Voxel> hullManager = new HullManager<Voxel>();
//            for (Voxel voxel : voxels) {
//                hullManager.update(voxel.posId, voxel);
//            }
//
//            ColladaFile colladaExport = new ColladaFile();
//
//            for (int i = 0; i < 6; i++) {
//                for (Voxel voxel : hullManager.getHullAdditions(i)) {
//                    int[] textureId = voxel.getTexture();
//                    int[] rotation = voxel.getRotation();
//                    boolean[] flip = voxel.getFlip();
//                    colladaExport.addPlane(
//                            voxel.getPosAsInt(),
//                            i,
//                            voxel.getColor(),
//                            textureId == null ? null : textureId[i],
//                            rotation == null ? 0 : rotation[i],
//                            flip != null && flip[i]
//                    );
//                    if (textureId != null) {
//                        colladaExport.registerTexture(textureId[i], this.getTexture(textureId[i]));
//                    }
//                }
//            }
//
//
//            colladaExport.finish(textureFile.getName());
//
//            // write the file
//            if (!colladaExport.writeToFile(file, errorHandler)) {
//                result = false;
//            }
//
//            // write the texture image file (if there is one)
//            if (colladaExport.hasTextureMap()) {
//                if (!colladaExport.writeTextureMap(textureFile, errorHandler)) {
//                    result = false;
//                }
//            }

            // validation - only check in debug mode
            if (Main.isDebugMode()) {
                // validate the file
                if (!XmlTools.validateAgainstXSD(
                        file.getAbsolutePath(),
                        new SaveResourceLoader("resource/xsd/collada_schema_1_4_1.xsd").asStreamSource(),
                        errorHandler)) {
                    result = false;
                }
            }

            return result;
        }
    }
}
