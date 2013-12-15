package com.vitco.core.data;

import com.vitco.core.data.notification.DataChangeListener;
import com.vitco.core.data.notification.NotificationService;
import com.vitco.settings.VitcoSettings;

/**
 * Abstract class that defines methods add Data Change Listeners.
 */
public abstract class ListenerData implements ListenerDataInterface {
    protected final NotificationService notifier = new NotificationService();

    @Override
    public final void addDataChangeListener(DataChangeListener dcl) {
        synchronized (VitcoSettings.SYNC) {
            notifier.add(dcl);
        }
    }

    @Override
    public final void removeDataChangeListener(DataChangeListener dcl) {
        synchronized (VitcoSettings.SYNC) {
            notifier.remove(dcl);
        }
    }
}
