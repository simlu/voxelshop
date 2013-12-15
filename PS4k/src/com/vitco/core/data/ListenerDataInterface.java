package com.vitco.core.data;

import com.vitco.core.data.notification.DataChangeListener;

/**
 * Data Listener interface.
 *
 * Implements methods that allow setting of data change listeners.
 */
public interface ListenerDataInterface {
    // add a data change listener
    void addDataChangeListener(DataChangeListener dcl);
    // remove a data change listener
    void removeDataChangeListener(DataChangeListener dcl);
}
