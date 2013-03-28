package com.vitco.engine.data;

import com.vitco.Main;
import com.vitco.engine.CWorld;
import com.vitco.engine.data.container.DataContainer;
import com.vitco.engine.data.container.Voxel;
import com.vitco.export.ColladaFile;
import com.vitco.util.FileTools;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.xml.XmlTools;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Data class that puts everything together and defines general data interaction (e.g. save/load)
 */
public final class Data extends VoxelHighlighting implements DataInterface {

    @Override
    public final void clearHistoryA() {
        historyManagerA.clear();
    }

    @Override
    public final void clearHistoryV() {
        historyManagerV.clear();
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

    @Override
    public final void freshStart() {
        // reset
        dataContainer = new DataContainer();
        // create initial layer and select it
        selectLayerSoft(createLayer("Layer"));
        // remove history
        clearHistoryA();
        clearHistoryV();
        notifier.onAnimationDataChanged();
        notifier.onVoxelDataChanged();
        notifier.onTextureDataChanged();
        // file has not changed yet
        hasChanged = false;
    }

    @Override
    public final boolean loadFromFile(File file) {
        boolean result = false;
        // VSD file format
        DataContainer loaded = new DataContainer(file, errorHandler);
        if (loaded.hasLoaded) {
            clearHistoryA();
            clearHistoryV();
            dataContainer = loaded;
            invalidateA();
            invalidateV(null);
            notifier.onTextureDataChanged();
            // file has not changed yet
            hasChanged = false;
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
                invalidateA();
                invalidateV(null);
                notifier.onTextureDataChanged();
                // file has not changed yet
                hasChanged = false;
                result = true;
            }
        }
        return result;
    }

    @Override
    public final boolean saveToFile(File file) {
        boolean result = dataContainer.saveToVsdFile(file, errorHandler);
        if (result) {
            hasChanged = false;
        }
        return result;
    }

    @Override
    public final boolean exportToCollada(File file, File textureFile) {
        boolean result = true;

        ColladaFile colladaExport = new ColladaFile();

        // build the world that we will use for exporting
        Voxel[] voxels = getVisibleLayerVoxel();
        CWorld world = new CWorld(true, -1);
        for (Voxel voxel : voxels) {
            world.updateVoxel(voxel);
        }

        for (Map.Entry<Voxel, String> entry : world.getVisibleVoxel().entrySet()) {
            char[] sides = entry.getValue().toCharArray();
            Voxel voxel = entry.getKey();
            int[] textureId = voxel.getTexture();
            int[] rotation = voxel.getRotation();
            boolean[] flip = voxel.getFlip();
            for (int i = 0; i < sides.length; i++) {
                if (sides[i] == '0') {
                    colladaExport.addPlane(
                            voxel.getPosAsFloat(),
                            i,
                            voxel.getColor(),
                            textureId == null ? null : textureId[i],
                            rotation == null ? 0 : rotation[i],
                            flip != null && flip[i]
                    );
                    if (textureId != null) {
                        colladaExport.registerTexture(textureId[i], this.getTexture(textureId[i]));
                    }
                }
            }
        }

        colladaExport.finish(textureFile.getName());

        // write the file
        if (!colladaExport.writeToFile(file, errorHandler)) {
            result = false;
        }

        // write the texture image file (if there is one)
        if (colladaExport.hasTextureMap()) {
            colladaExport.writeTextureMap(textureFile, errorHandler);
        }

        // only check in debug mode
        if (Main.isDebugMode()) {
            // validate the file
            if (!XmlTools.validateAgainstXSD(
                    file.getAbsolutePath(),
                    new StreamSource(ClassLoader.getSystemResourceAsStream("resource/xsd/collada_schema_1_4_1.xsd")),
                    errorHandler)) {
                result = false;
            }
        }

        return result;
    }
}
