package com.vitco.util.components.dialog.components;

import com.vitco.util.components.dialog.BlankDialogModule;

import java.awt.*;
import java.io.File;

/**
 * A module that can be used to select a file location.
 */
public class FileSelectModule extends BlankDialogModule {

    // constructor
    public FileSelectModule(String identifier, File initTo, Frame owner) {
        super(identifier);
        // create the file select part
        addModule(new TextInputModule("file", "Name: ", initTo.isDirectory() ? "" : initTo.getName(), true), true);
        // create the folder select part
        addModule(new FolderSelectModule("folder", owner, initTo), true);
    }

    @Override
    public String getValue(String identifier) {
        // return the file that is currently selected
        return super.getValue("folder") + "/" + super.getValue("file");
    }
}
