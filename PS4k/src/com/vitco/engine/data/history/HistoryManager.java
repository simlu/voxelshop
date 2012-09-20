package com.vitco.engine.data.history;

import java.util.ArrayList;

/**
 * Manages BasicActionIntents. History manager.
 */
public class HistoryManager {

    // holds the history data
    private int historyPosition = -1;
    private ArrayList<BasicActionIntent> history = new ArrayList<BasicActionIntent>();

    public void clear() {
        historyPosition = -1;
        history = new ArrayList<BasicActionIntent>();
        // invalidate the cache
        notifyListener();
    }

    public final boolean canUndo() {
        return (historyPosition > -1);
    }

    public final boolean canRedo() {
        return (history.size() > historyPosition + 1);
    }

    public ArrayList<BasicActionIntent> getHistory() {
        return new ArrayList<BasicActionIntent>(history);
    }

    public int getHistoryPosition() {
        return historyPosition;
    }

    public void setHistory(ArrayList<BasicActionIntent> history) {
        this.history = new ArrayList<BasicActionIntent>(history);
    }

    public void setHistoryPosition(int historyPosition) {
        this.historyPosition = historyPosition;
    }

    // adds a new intent to the history and executes it
    public final void applyIntent(BasicActionIntent actionIntent) {
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
            notifyListener();
        }
    }

    // apply the next history intent
    public final void apply() {
        if (history.size() > historyPosition + 1) { // we can still "redo"
            historyPosition++; // move one "up"
            history.get(historyPosition).apply(); // redo action
            // make sure the attached histories are applied
            if (history.size() > historyPosition + 1 && history.get(historyPosition).attach) {
                apply();
            } else {
                notifyListener();
            }
        }

    }

    // apply the last history intent
    public final void unapply() {
        if (historyPosition > -1) { // we can still undo
            history.get(historyPosition).unapply(); // undo action
            historyPosition--; // move one "down"
            // make sure the attached histories are applied
            if (historyPosition > -1 && history.get(historyPosition).attach) {
                unapply();
            } else {
                notifyListener();
            }
        }
    }

    private final ArrayList<HistoryChangeListener> listeners = new ArrayList<HistoryChangeListener>();
    public final void addChangeListener(HistoryChangeListener hcl) {
        listeners.add(hcl);
    }
    public final void removeChangeListener(HistoryChangeListener hcl) {
        listeners.remove(hcl);
    }
    private void notifyListener() {
        for (HistoryChangeListener hcl : listeners) {
            hcl.onChange();
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
