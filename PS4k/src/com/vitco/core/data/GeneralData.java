package com.vitco.core.data;

import com.vitco.core.data.container.DataContainer;
import com.vitco.core.data.notification.DataChangeAdapter;
import com.vitco.settings.VitcoSettings;

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
