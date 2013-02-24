package com.vitco.engine.data.history;

/**
 * Called when history changes
 */
public interface HistoryChangeListener<T extends BasicActionIntent> {
    public void onChange(T action);
}
