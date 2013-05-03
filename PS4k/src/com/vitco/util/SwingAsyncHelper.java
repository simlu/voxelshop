package com.vitco.util;

import com.vitco.util.error.ErrorHandlerInterface;

import javax.swing.*;

/**
 * Helps executing a "swing" event correctly
 */
public class SwingAsyncHelper {
    public static void handle(Runnable runnable, ErrorHandlerInterface errorHandler) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
}
