package com.vitco.app.util.components.dialog;

/**
 * Listener interface for the UserInputDialog that is
 * notified when the dialog is closed by the user.
 */
public interface UserInputDialogListener {
    // called when the dialog closes (uses presses cancel or submit)
    // needs to return true if closing should be permitted
    // Note: The result is ignored if the resultFlag is equal to the cancel flag
    boolean onClose(int resultFlag);
}
