package com.vitco.util.components.progressbar;

import com.vitco.layout.content.console.ConsoleInterface;

/**
 * Should be implemented by any class that lives inside a ProgressWorker
 * to report their status.
 */
public abstract class ProgressReporter {
    // reference to the dialog that should be used for reporting.
    private final ProgressDialog dialog;
    // grant access to console for any progress reporter
    protected final ConsoleInterface console;

    // constructor
    public ProgressReporter(ProgressDialog dialog, ConsoleInterface console) {
        this.dialog = dialog;
        this.console = console;
    }

    // retrieve the progress dialog (useful for passing it on to further subclasses)
    public final ProgressDialog getProgressDialog() {
        return dialog;
    }

    // retrieve the progress dialog (useful for passing it on to further subclasses)
    public final ConsoleInterface getConsole() {
        return console;
    }

    // set the current activity and whether the progress should
    // be automatically increased (for fast tasks only)
    public final void setActivity(String activity, boolean autoIncrease) {
        dialog.setActivity(activity, autoIncrease);
    }

    // returns true if the task is canceled (this needs to be checked by the task doing the work)
    public final boolean isCancelled() {
        return dialog.isCancelled();
    }

    // set the progress status
    public final void setProgress(float percent) {
        dialog.setProgress(percent);
    }

}
