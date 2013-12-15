package com.vitco.core.data.history;

/**
 * Called when history changes
 */
public interface HistoryChangeListener<T extends BasicActionIntent> {
    public void onChange(T action);
}
