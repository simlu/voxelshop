package com.vitco.engine.data;

import com.vitco.engine.data.notification.DataChangeListener;
import com.vitco.engine.data.notification.NotificationService;

/**
 * Abstract class that defines methods add Data Change Listeners.
 */
public abstract class ListenerData extends DataDefinition implements ListenerDataInterface {
    protected final NotificationService notifier = new NotificationService();

    @Override
    public final void addDataChangeListener(DataChangeListener dcl) {
        notifier.add(dcl);
    }

    @Override
    public final void removeDataChangeListener(DataChangeListener dcl) {
        notifier.remove(dcl);
    }
}
