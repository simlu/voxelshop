package com.vitco.engine.data;

import com.vitco.engine.data.container.DataContainer;
import com.vitco.util.FileTools;
import com.vitco.util.error.ErrorHandlerInterface;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.io.File;

/**
 * Data class that puts everything together and defines general data interaction (e.g. save/load)
 */
public final class Data extends VoxelHighlight implements DataInterface {

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
    public void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    public Data() {
        super();
        freshStart();
    }

    @Override
    public final void freshStart() {
        // reset, but save current color
        Color currentColor = CURRENT_COLOR;
        dataContainer = new DataContainer();
        CURRENT_COLOR = currentColor;
        // create initial layer and select it
        selectLayerSoft(createLayer("Layer"));
        // remove history
        clearHistoryA();
        clearHistoryV();
        notifier.onAnimationDataChanged();
        notifier.onVoxelDataChanged();
        notifier.onColorDataChanged();
        // file has not changed yet
        hasChanged = false;
    }

    @Override
    public final boolean loadFromFile(File file) {
        boolean result = false;
        Object loaded = FileTools.loadFromFile(file, errorHandler);
        if (loaded != null) {
            clearHistoryA();
            clearHistoryV();
            dataContainer = (DataContainer)loaded;
            invalidateA();
            invalidateV();
            // file has not changed yet
            hasChanged = false;
            result = true;
        }
        return result;
    }

    @Override
    public final boolean saveToFile(File file) {
        boolean result = FileTools.saveToFile(file, dataContainer, errorHandler);
        if (result) {
            hasChanged = false;
        }
        return result;
    }
}
