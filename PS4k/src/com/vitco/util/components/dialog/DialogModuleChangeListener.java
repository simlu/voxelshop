package com.vitco.util.components.dialog;

/**
 * Called when the state of a module changes
 */
public interface DialogModuleChangeListener {
    // called when the ready state of a component has changed
    // Note: this does propagate upwards
    void onReadyStateChanged();
    // called when the content has changed at run time (the component needs to call this!)
    // Note: this does propagate upwards
    void onContentChanged();
}
