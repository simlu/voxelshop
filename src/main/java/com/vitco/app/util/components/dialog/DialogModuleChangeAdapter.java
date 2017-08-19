package com.vitco.app.util.components.dialog;

/**
 * Simple adapter for the DialogModuleStateListener
 */
public abstract class DialogModuleChangeAdapter implements DialogModuleChangeListener {
    @Override
    public void onReadyStateChanged() {}

    @Override
    public void onContentChanged() {}
}
