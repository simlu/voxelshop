package com.vitco.frames.engine.data.history;

/**
 * Base intent that allows to specify actions further.
 */
public abstract class BasicActionIntent {
    // true if this is the first time apply is called
    private boolean first_call = true;
    // true if this intent is attached to surrounding intents
    public final boolean attach;

    // returns true iff first call or before first call of apply
    protected final boolean isFirstCall() {
        return first_call;
    }

    // constructor
    protected BasicActionIntent(boolean attach) {
        this.attach = attach;
    }

    // wrapper: apply action
    public final void apply() {
        applyAction();
        first_call = false;
    }

    // wrapper: unapply action
    public final void unapply() {
        unapplyAction();
    }

    // action to be defined by child class
    protected abstract void applyAction();

    // action to be defined by child class
    protected abstract void unapplyAction();
}
