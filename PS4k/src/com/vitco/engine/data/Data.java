package com.vitco.engine.data;

import com.newbrightidea.util.RTree;
import com.vitco.engine.data.container.DataContainer;
import com.vitco.engine.data.container.Voxel;
import com.vitco.export.ColladaFile;
import com.vitco.util.FileTools;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.xml.XmlTools;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.io.File;
import java.util.HashMap;

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
    public final boolean exportToCollada(File file) {
        boolean result = true;

        ColladaFile colladaExport = new ColladaFile();

        Voxel[] voxels = getVisibleLayerVoxel();

        // build rtree for fast access
        RTree<Voxel> positions = new RTree<Voxel>(50,2,3);
        for (Voxel voxel : voxels) {
            positions.insert(voxel.getPosAsFloat(), voxel);
        }

        for (Voxel voxel : voxels) {

            float[] pos = voxel.getPosAsFloat();
            float[] ZEROS = new float[]{0,0,0};

            float[] colladaPos = pos.clone();
            colladaPos[2] = - pos[1] + 0.5f;
            colladaPos[1] = pos[2];

            Color col = voxel.getColor();

            // only add those plans that are visible

            if (positions.search(new float[] {pos[0], pos[1] + 1, pos[2]}, ZEROS).size() == 0) {
                colladaExport.addPlane(colladaPos, 0, col);
            }
            if (positions.search(new float[] {pos[0], pos[1] - 1, pos[2]}, ZEROS).size() == 0) {
                colladaExport.addPlane(colladaPos, 1, col);
            }

            if (positions.search(new float[] {pos[0], pos[1], pos[2] - 1}, ZEROS).size() == 0) {
                colladaExport.addPlane(colladaPos, 2, col);
            }
            if (positions.search(new float[] {pos[0], pos[1], pos[2] + 1}, ZEROS).size() == 0) {
                colladaExport.addPlane(colladaPos, 3, col);
            }

            if (positions.search(new float[] {pos[0] - 1, pos[1], pos[2]}, ZEROS).size() == 0) {
                colladaExport.addPlane(colladaPos, 4, col);
            }
            if (positions.search(new float[] {pos[0] + 1, pos[1], pos[2]}, ZEROS).size() == 0) {
                colladaExport.addPlane(colladaPos, 5, col);
            }
        }

        colladaExport.finish();

        // write the file
        if (!colladaExport.writeToFile(file, errorHandler)) {
            result = false;
        }

        // validate the file
        if (!XmlTools.validateAgainstXSD(
                file.getAbsolutePath(),
                new StreamSource(ClassLoader.getSystemResourceAsStream("resource/xsd/collada_schema_1_4_1.xsd")),
                errorHandler)) {
            result = false;
        }

        return result;
    }
}
