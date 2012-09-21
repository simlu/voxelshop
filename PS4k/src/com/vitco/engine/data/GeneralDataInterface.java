package com.vitco.engine.data;

/**
 * Manages everything that has to do with general data
 */
public interface GeneralDataInterface {
    // true iff the data has changed since last reset (triggered by save to file)
    boolean hasChanged();
    // reset the changed of data (nothing has changed after this is called)
    void resetHasChanged();
}
