package com.vitco.async;

import java.util.UUID;

/**
 * Action to be performed by AsyncActionManager
 */
public abstract class AsyncAction implements Runnable {

    //public final String ident;

    public abstract void performAction();

    @Override
    public final void run() {
        performAction();
    }

    // name of this AsyncAction
    private final String name;

    public boolean ready() {
        return true;
    }

    public AsyncAction(String name) {
        this.name = name;
        // get the stack trace
//        StringWriter sw = new StringWriter();
//        new Exception().printStackTrace(new PrintWriter(sw));
//        this.ident = sw.toString(); // stack trace as a string
    }

    public AsyncAction() {
        name = UUID.randomUUID().toString() + System.currentTimeMillis();
        // get the stack trace
//        StringWriter sw = new StringWriter();
//        new Exception().printStackTrace(new PrintWriter(sw));
//        this.ident = sw.toString(); // stack trace as a string
    }

    public final String getActionName() {
        return name;
    }
}
