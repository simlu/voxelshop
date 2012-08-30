package com.vitco.frames.engine.data;

import com.vitco.frames.engine.data.listener.DataChangeListener;

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
