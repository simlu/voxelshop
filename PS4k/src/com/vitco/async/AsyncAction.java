package com.vitco.async;

import java.util.UUID;

/**
 * Action to be performed by AsyncActionManager
 */
public abstract class AsyncAction {

    public abstract void performAction();

    // name of this AsyncAction
    private final String name;

    public boolean ready() {
        return true;
    }

    public AsyncAction(String name) {
        this.name = name;
    }

    public AsyncAction() {
        name = UUID.randomUUID().toString() + System.currentTimeMillis();
    }

    protected String getName() {
        return name;
    }
}
