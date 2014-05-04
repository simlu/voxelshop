package com.vitco.util.components.progressbar;

import com.jidesoft.swing.JideLabel;
import com.vitco.util.components.dialog.components.DialogButton;
import com.vitco.util.misc.SaveResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A progress bar that encapsulates a ProgressWorker and displays the status of this action.
 *
 * The progress bar blocks all user interaction but prevents the program from freezing.
 */
public class ProgressDialog extends JDialog {

    // reference to this instance
    private final ProgressDialog thisInstance = this;

    // reference: the progress bar itself
    private final JProgressBar progressBar;
    // reference: the cancel button (if visible)
    private final DialogButton cancelButton;
    // reference: the JLabel that displays the current activity
    private final JideLabel activityLabel;

    // reference to the currently active worker
    private ProgressWorker worker = null;

    // owner frame
    private final Frame owner;

    // true if currently executing something
    private boolean running = false;

    // true if the progress bar should be automatically increased
    // (only suitable for very short tasks!)
    private boolean autoIncrease = false;

    // the current value (as float) of the progress bar
    private float currentValue = 0;

    // constructor
    public ProgressDialog(Frame owner) {
        // make sure this JDialog is blocking (set modal flag)
        super(owner, true);

        // hide title and close button of frame
        this.setUndecorated(true);
        // store owner reference
        this.owner = owner;

        // set up the wrapper panel (for entire content)
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(5, 10, 10, 10)
        ));

        // add wrapper for loading button and text right of it
        JPanel iconTextWrapper = new JPanel();
        iconTextWrapper.setLayout(new BorderLayout());

        // add loading button
        Icon icon = new SaveResourceLoader("resource/img/icons/loading.gif").asIconImage();
        JideLabel label = new JideLabel(icon);
        label.setBorder(BorderFactory.createEmptyBorder(0,0,0,6));
        iconTextWrapper.add(label, BorderLayout.WEST);

        // create top over label
        label = new JideLabel("Processing Task");
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        iconTextWrapper.add(label, BorderLayout.CENTER);

        // add wrapper to layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(iconTextWrapper, gbc);

        // ---------

        // create current task label
        activityLabel = new JideLabel("Initializing Task...");
        gbc.gridy++;
        contentPane.add(activityLabel, gbc);

        // create progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(300, 20));
        gbc.gridy++;
        contentPane.add(progressBar, gbc);

        // create cancel button
        cancelButton = new DialogButton("Cancel");
        cancelButton.setVisible(false); // hide cancel button by default
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy++;
        contentPane.add(cancelButton, gbc);

        // set cancel action
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                thisInstance.setVisible(false);
                worker.cancel(true);
            }
        });

        // add the panel to this JDialog
        this.setLayout(new BorderLayout());
        this.add(contentPane);
    }

    // set the visible state of the cancel button (note that cancel needs
    // to be specifically handles by the processing task!)
    public final void enableCancel(boolean showCancelButton) {
        cancelButton.setVisible(showCancelButton);
    }

    // start the processing of executing a task
    public final void start(ProgressWorker worker) {
        // make sure the dialog is not already running
        if (!running && worker != null && !isVisible()) {
            running = true;
            this.worker = worker;

            // used to automatically increase the progress value
            new ProgressWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    // keep alive while the worker is running
                    while (running) {
                        if (autoIncrease) {
                            final float value = thisInstance.getProgress();
                            thisInstance.setProgress(value + (100 - value) / 4);
                        }
                        Thread.sleep(50);
                    }
                    return null;
                }
            }.execute();

            // start the worker
            worker.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String name = evt.getPropertyName();
                    if (name.equals("state")) {
                        SwingWorker.StateValue state = (SwingWorker.StateValue) evt.getNewValue();
                        switch (state) {
                            case DONE:
                                // reset the progress to zero (for the next start)
                                setProgress(0);
                                // hide this frame
                                thisInstance.setVisible(false);
                                // remove the reference to the worker
                                thisInstance.worker = null;
                                // not running anymore
                                running = false;
                                break;
                        }
                    }
                }

            });
            worker.execute();

            // show the dialog
            thisInstance.pack();
            thisInstance.setLocationRelativeTo(owner);
            thisInstance.setVisible(true);
        }
    }

    // set the activity that is currently performed and reset the progress to zero
    // the auto increase flag indicates whether the bar should progress automatically
    // or if the worker is explicitly setting the value
    public final void setActivity(String activity, boolean autoIncrease) {
        activityLabel.setText(activity);
        setProgress(0);
        this.autoIncrease = autoIncrease;
    }

    // set the progress (this should only be done if the last
    // "setActivity" call didn't set the "autoIncrease" flag
    public final void setProgress(float percent) {
        synchronized (ProgressDialog.class) {
            currentValue = percent;
            progressBar.setValue((int) percent);
        }
    }

    // get the current progress value
    private float getProgress() {
        synchronized (ProgressDialog.class) {
            return currentValue;
        }
    }

    // check if this dialog is canceled
    public final boolean isCancelled() {
        return worker == null || worker.isCancelled();
    }
}
