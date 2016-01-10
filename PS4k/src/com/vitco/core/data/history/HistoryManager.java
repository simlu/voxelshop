package com.vitco.core.data.history;

import java.util.ArrayList;

/**
 * Manages BasicActionIntents. History manager.
 */
public class HistoryManager<T extends BasicActionIntent> {

    // holds the history data
    private int historyPosition = -1;
    private ArrayList<T> history = new ArrayList<T>();

    public void clear() {
        if (frozen) {return;}
        historyPosition = -1;
        history = new ArrayList<T>();
        // invalidate the cache
        notifyListener(null);
    }

    public final boolean canUndo() {
        return !frozen && (historyPosition > -1);
    }

    public final boolean canRedo() {
        return !frozen && (history.size() > historyPosition + 1);
    }

    public ArrayList<T> getHistory() {
        return new ArrayList<T>(history);
    }

    public int getHistoryPosition() {
        return historyPosition;
    }

    public final void setHistory(ArrayList<T> history) {
        if (frozen) {return;}
        this.history = new ArrayList<T>(history);
    }

    public final void setHistoryPosition(int historyPosition) {
        if (frozen) {return;}
        this.historyPosition = historyPosition;
    }

    private boolean frozen = false;
    public final void setFrozen(boolean flag) {
        frozen = flag;
    }

    // adds a new intent to the history and executes it
    public final void applyIntent(T actionIntent) {
        if (frozen) {
            for (HistoryChangeListener<T> hcl : listeners) {
                hcl.onFrozenIntent(actionIntent);
            }
            return;
        }
        // delete all "re-dos"
        while (history.size() > historyPosition + 1) {
            history.remove(historyPosition + 1);
        }
        // apply the intent
        actionIntent.apply();
        historyPosition++;
        // and add it to the history
        history.add(actionIntent);
        // invalidate the cache if the intent is not attached
        // (for the main intent)
        if (!actionIntent.attach) {
            notifyListener(actionIntent);
        }
    }

    // apply the next history intent
    public final void apply() {
        if (frozen) {
            for (HistoryChangeListener<T> hcl : listeners) {
                hcl.onFrozenApply();
            }
            return;
        }
        if (history.size() > historyPosition + 1) { // we can still "redo"
            historyPosition++; // move one "up"
            history.get(historyPosition).apply(); // redo action
            // make sure the attached histories are applied
            while (history.size() > historyPosition + 1 && history.get(historyPosition).attach) {
                historyPosition++; // move one "up"
                history.get(historyPosition).apply(); // redo action
            }
            notifyListener(history.get(historyPosition)); // ok
        }

    }

    // apply the last history intent
    public final void unapply() {
        if (frozen) {
            for (HistoryChangeListener<T> hcl : listeners) {
                hcl.onFrozenUnapply();
            }
            return;
        }
        if (historyPosition > -1) { // we can still undo
            T mainAction = history.get(historyPosition);
            _unapply();
            notifyListener(mainAction);
        }
    }

    // helper
    private void _unapply() {
        if (historyPosition > -1) { // we can still undo
            history.get(historyPosition).unapply(); // undo action
            historyPosition--; // move one "down"
            // make sure the attached histories are applied
            while (historyPosition > -1 && history.get(historyPosition).attach) {
                history.get(historyPosition).unapply(); // undo action
                historyPosition--; // move one "down"
            }
        }
    }

    private final ArrayList<HistoryChangeListener<T>> listeners
            = new ArrayList<HistoryChangeListener<T>>();
    public final void addChangeListener(HistoryChangeListener<T> hcl) {
        listeners.add(hcl);
    }
    public final void removeChangeListener(HistoryChangeListener<T> hcl) {
        listeners.remove(hcl);
    }
    private void notifyListener(T action) {
        for (HistoryChangeListener<T> hcl : listeners) {
            hcl.onChange(action);
        }
    }

    // displays current historyA information
    public final void debug() {
        System.out.println(Math.max(history.size()-50,0) == 0 ? "=================" : "[...]");
        for (int c = Math.max(history.size()-50,0), len = history.size()-1; c < len; c++) {
            System.out.println(history.get(c) + " @ " + history.get(c).attach + (c == historyPosition-1 ? " XXX " : ""));
        }
        System.out.println("=================");
    }

}
