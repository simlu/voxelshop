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
            // this *should* be ok as long as this event doesn't read/write
            // to synchronized data
            SwingUtilities.invokeLater(runnable);
//            try {
//                SwingUtilities.invokeAndWait(runnable);
//            } catch (InterruptedException e) {
//                errorHandler.handle(e);
//            } catch (InvocationTargetException e) {
//                errorHandler.handle(e);
//            }
        }
    }
}
