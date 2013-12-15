package com.vitco.manager.async;

/**
 * Action to be performed by AsyncActionManager
 */
public abstract class AsyncAction implements Runnable {

    public abstract void performAction();

    @Override
    public final void run() {
        performAction();
    }

    // name of this AsyncAction
    public final String name;

    // can be overriden to delay execution of this AsyncAction
    public boolean ready() {
        return true;
    }

    // giving a name to an async action guarantees that
    // there are not a lot of delayed actions with the
    // same name queueing up in the AsyncActionManager
    // IMPORTANT: it doesn't enforce this for not delayed actions
    public AsyncAction(String name) {
        this.name = name;
    }

    // once this reaches Integer.MAX_VALUE it will reset to Integer.MIN_VALUE
    private static int c = 0;

    public AsyncAction() {
        synchronized (AsyncAction.class) {
            name = String.valueOf(c++);
        }
    }

}
