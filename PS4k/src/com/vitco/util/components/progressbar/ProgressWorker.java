package com.vitco.util.components.progressbar;

import javax.swing.*;

/**
 * A worker thread that encapsulates a task handled by a ProgressDialog.
 */
public abstract class ProgressWorker extends SwingWorker<Object, Object> {

    @Override
    protected abstract Object doInBackground() throws Exception;

}
