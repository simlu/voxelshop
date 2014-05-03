package com.vitco.util.dialog.components;

import com.vitco.util.dialog.BlankDialogModule;
import com.vitco.util.file.FileTools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

/**
 * Object that allows the user to select a folder.
 */
public class FolderSelectModule extends BlankDialogModule {

    // dialog that is used to select the folder
    private final JFileChooser fileDialog;

    // the label that displays the folder
    private final JLabel label;

    // constructor
    public FolderSelectModule(String identifier, final Frame owner, File initTo) {
        super(identifier);
        // set layout
        setLayout(new BorderLayout());

        // setup dialog
        fileDialog = new JFileChooser();
        fileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileDialog.setCurrentDirectory(initTo);

        // create the label that displays the current folder name
        label = new JLabel(FileTools.shortenPath(fileDialog.getCurrentDirectory().getPath() + "\\", 50));
        label.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
        add(label, BorderLayout.CENTER);

        // create the "select folder" button
        DialogButton selectFolderButton = new DialogButton("Select Folder...");
        selectFolderButton.setFocusable(false);
        add(selectFolderButton, BorderLayout.WEST);
        selectFolderButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // open dialog to select folder location
                if (fileDialog.showDialog(owner, "Select") == JFileChooser.APPROVE_OPTION) {
                    File folder = fileDialog.getSelectedFile();
                    if (folder != null) {
                        label.setText(FileTools.shortenPath(folder.getPath() + "\\", 50));
                        // listen to changes
                        notifyContentChanged();
                    }
                }
            }
        });
    }

    @Override
    protected String getValue(String identifier) {
        // return the directory of the selected folder
        if (fileDialog.getSelectedFile() != null) {
            return fileDialog.getSelectedFile().getPath() + "\\";
        } else {
            // ensures that the path is stored even if it was not changed
            return fileDialog.getCurrentDirectory().getPath() + "\\";
        }
    }

    @Override
    protected ArrayList<String[]> getSerialization(String path) {
        ArrayList<String[]> keyValuePair = new ArrayList<String[]>();
        keyValuePair.add(new String[]{path, getValue(null)});
        return keyValuePair;
    }

    @Override
    protected boolean loadValue(String[] pair) {
        if (pair[0].equals("")) {
            // set the path
            fileDialog.setCurrentDirectory(new File(pair[1]));
            // update the text label
            label.setText(FileTools.shortenPath(pair[1], 50));
            // listen to changes
            notifyContentChanged();
            return true;
        }
        return false;
    }

}
