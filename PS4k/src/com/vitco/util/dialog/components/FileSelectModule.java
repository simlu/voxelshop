package com.vitco.util.dialog.components;

import com.vitco.util.dialog.BlankDialogModule;

import javax.swing.*;
import java.io.File;

/**
 * A module that can be used to select a file location.
 */
public class FileSelectModule extends BlankDialogModule {

    // constructor
    public FileSelectModule(String identifier, File initTo, JFrame owner) {
        super(identifier);
        // create the file select part
        addModule(new TextInputModule("file", "Name: ", initTo, true), true);
        // create the folder select part
        addModule(new FolderSelectModule("folder", owner, initTo), true);
    }

    @Override
    public Object getValue(String identifier) {
        // return the file that is currently selected
        return new File(super.getValue("folder") + "/" + super.getValue("file"));
    }
}
