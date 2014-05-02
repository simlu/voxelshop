package com.vitco.util.dialog.components;

import com.vitco.util.dialog.BlankDialogModule;

/**
 * FieldSet class
 *
 * A fieldSet instance is a set of BlankDialogModule object which can be used by
 * the user to enter information. The information can be accessed through
 * "getValue(id)" where "id" is the identifier of the corresponding object
 *
 * This object is used to group input elements together that belong
 * logically together and also display them together.
 */
public class FieldSet extends BlankDialogModule {

    // caption of this fieldSet (for displaying purposes)
    private final String caption;
    protected final String getCaption() {
        return caption;
    }
    // -----------

    // constructor
    public FieldSet(String identifier, String caption) {
        super(identifier);
        this.caption = caption;
    }

    // add a component to this fieldSet
    public final void addComponent(BlankDialogModule component) {
        // register for value collection
        addModule(component, true);
    }
}
