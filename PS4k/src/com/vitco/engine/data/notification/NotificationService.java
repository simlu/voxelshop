package com.vitco.engine.data.notification;

import java.util.HashSet;

/**
 * Notification Service, deals with and notifies listeners.
 */
public final class NotificationService implements NotificationServiceInterface {

    private final HashSet<DataChangeListener> listeners = new HashSet<DataChangeListener>();

    // add a listener
    @Override
    public void add(DataChangeListener dcl) {
        listeners.add(dcl);
    }

    // remove a listener
    @Override
    public void remove(DataChangeListener dcl) {
        listeners.remove(dcl);
    }

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
     public void onVoxelHighlightingChanged() {
        for (DataChangeListener listener : listeners) {
            listener.onVoxelHighlightingChanged();
        }
    }

    @Override
    public void onVoxelSelectionShiftChanged() {
        for (DataChangeListener listener : listeners) {
            listener.onVoxelSelectionShiftChanged();
        }
    }

    @Override
    public void onTextureDataChanged() {
        for (DataChangeListener listener : listeners) {
            listener.onTextureDataChanged();
        }
    }

}
