package com.vitco.util.misc;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.List;

public abstract class AutoFileCloser {
    // the core action code that the implementer wants to run
    protected abstract void doWork() throws Throwable;

    // track a list of closeable thingies to close when finished
    private final List<Closeable> closeables_ = new LinkedList<Closeable>();

    // give the implementer a way to track things to close
    // assumes this is called in order for nested closeables,
    // inner-most to outer-most
    protected final <T extends Closeable> T autoClose(T closeable) {
        closeables_.add(0, closeable);
        return closeable;
    }

    public AutoFileCloser() {
        // a variable to track a "meaningful" exception, in case
        // a close() throws an exception
        Throwable pending = null;

        try {
            doWork(); // do the real work

        } catch (Throwable throwable) {
            pending = throwable;

        } finally {
            // close the watched streams
            for (Closeable closeable : closeables_) {
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (Throwable throwable) {
                        if (pending == null) {
                            pending = throwable;
                        }
                    }
                }
            }

            // if we had a pending exception, rethrow it
            // this is necessary b/c the close can throw an
            // exception, which would remove the pending
            // status of any exception thrown in the try block
            if (pending != null) {
                if (pending instanceof RuntimeException) {
                    //noinspection ThrowFromFinallyBlock
                    throw (RuntimeException) pending;
                } else {
                    //noinspection ThrowFromFinallyBlock
                    throw new RuntimeException(pending);
                }
            }
        }
    }
}