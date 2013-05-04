package com.vitco.engine.data;

import com.vitco.engine.data.container.DataContainer;
import com.vitco.engine.data.notification.DataChangeAdapter;

/**
 * Manages everything that has to do with general data
 */
public class GeneralData extends ListenerData implements GeneralDataInterface {

    // main data container
    protected DataContainer dataContainer = new DataContainer();

    // ######################

    // true if the data has changed since last save
    protected boolean hasChanged = false;

    public GeneralData() {
        this.addDataChangeListener(new DataChangeAdapter() {
            @Override
            public void onVoxelDataChanged() {
                hasChanged = true;
            }

            @Override
            public void onAnimationDataChanged() {
                hasChanged = true;
            }
        });
    }

    @Override
    public synchronized boolean hasChanged() {
        return hasChanged;
    }

    @Override
    public synchronized void resetHasChanged() {
        hasChanged = false;
    }

}
