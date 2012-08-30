package com.vitco.frames.engine.data;

import com.vitco.frames.engine.data.listener.DataChangeListener;

import java.util.ArrayList;

/**
 * Abstract class that defines methods to notify all Data Change Listeners.
 */
public abstract class ListenerData implements ListenerDataInterface {

    private final ArrayList<DataChangeListener> listeners = new ArrayList<DataChangeListener>();

    // todo add these method calls to the correct places (do last)
    protected final NotificationService notifier = new NotificationService();
    protected final class NotificationService implements DataChangeListener {

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
        public void onLayerDataChanged() {
            for (DataChangeListener listener : listeners) {
                listener.onLayerDataChanged();
            }
        }
    }

    @Override
    public final void addDataChangeListener(DataChangeListener dcl) {
        listeners.add(dcl);
    }

    @Override
    public final void removeDataChangeListener(DataChangeListener dcl) {
        listeners.remove(dcl);
    }
}
