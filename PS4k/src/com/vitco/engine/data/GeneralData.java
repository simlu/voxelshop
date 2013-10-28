package com.vitco.engine.data;

import com.vitco.engine.data.container.DataContainer;
import com.vitco.engine.data.notification.DataChangeAdapter;
import com.vitco.res.VitcoSettings;

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
    public boolean hasChanged() {
        synchronized (VitcoSettings.SYNC) {
            return hasChanged;
        }
    }

    @Override
    public void resetHasChanged() {
        synchronized (VitcoSettings.SYNC) {
            hasChanged = false;
        }
    }

}
