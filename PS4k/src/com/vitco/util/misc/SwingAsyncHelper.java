package com.vitco.util.misc;

import com.vitco.manager.error.ErrorHandlerInterface;

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
            // this will cause a deadlock as sync is not passed to the new runnable
            // (if this is called from sync context already)
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
