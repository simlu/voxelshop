package com.vitco.engine.data.notification;

import java.util.ArrayList;

/**
 * Notification Service, deals with and notifies listeners.
 */
public final class NotificationService implements DataChangeListener {
    private final ArrayList<DataChangeListener> listeners = new ArrayList<DataChangeListener>();

    // add a listener
    public void add(DataChangeListener dcl) {
        listeners.add(dcl);
    }

    // remove a listener
    public void remove(DataChangeListener dcl) {
        listeners.remove(dcl);
    }

    // todo add these method calls to the correct places (do last)
    @Override
    public void onAnimationDataChanged() {
        for (DataChangeListener listener : listeners) {
            listener.onAnimationDataChanged();
        }
    }

    @Override
    public void onAnimationSelectionChanged() {
        for (DataChangeListener listener : listeners) {
            listener.onAnimationSelectionChanged();
        }
    }

    @Override
    public void onVoxelDataChanged() {
        for (DataChangeListener listener : listeners) {
            listener.onVoxelDataChanged();
        }
    }

    @Override
    public void onVoxelSelectionChanged() {
        for (DataChangeListener listener : listeners) {
            listener.onVoxelSelectionChanged();
        }
    }
}
