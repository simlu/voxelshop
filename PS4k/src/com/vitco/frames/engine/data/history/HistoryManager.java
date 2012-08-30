package com.vitco.frames.engine.data.history;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Manages BasicActionIntents. History manager.
 */
public class HistoryManager implements Serializable {
    // holds the history data
    private int historyPosition = -1;
    private final ArrayList<BasicActionIntent> history = new ArrayList<BasicActionIntent>();

    public final boolean canUndo() {
        return (historyPosition > -1);
    }

    public final boolean canRedo() {
        return (history.size() > historyPosition + 1);
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
        // invalidate the cache
        notifyListener();
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

    ArrayList<HistoryChangeListener> listeners = new ArrayList<HistoryChangeListener>();
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

//    // displays current historyA information
//    public final void debug() {
//        int i = -1;
//        for (BasicActionIntent ai : history) {
//            i++;
//            System.out.println(ai + " @ " + ai.attach + (i == historyPosition ? " XXX " : ""));
//        }
//        System.out.println("=================");
//    }

}
