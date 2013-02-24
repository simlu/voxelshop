package com.vitco.engine.data.notification;

/**
 * Notification Service, deals with and notifies listeners.
 */
public interface NotificationServiceInterface extends DataChangeListener {
    // add a listener
    void add(DataChangeListener dcl);

    // remove a listener
    void remove(DataChangeListener dcl);
}
