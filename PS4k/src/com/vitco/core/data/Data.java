package com.vitco.core.data;

import com.vitco.core.data.container.DataContainer;
import com.vitco.manager.error.ErrorHandlerInterface;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.file.FileTools;
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

    public final void setFrozen(boolean flag) {
        historyManagerA.setFrozen(flag);
        historyManagerV.setFrozen(flag);
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
}
