package com.vitco.core.data.history;

/**
 * Called when history changes
 */
public interface HistoryChangeListener<T extends BasicActionIntent> {
    void onChange(T action);
    void onFrozenIntent(T actionIntent);
    void onFrozenApply();
    void onFrozenUnapply();
}
