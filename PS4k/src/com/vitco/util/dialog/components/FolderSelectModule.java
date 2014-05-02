package com.vitco.util.dialog.components;

import com.vitco.util.dialog.BlankDialogModule;
import com.vitco.util.file.FileTools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Object that allows the user to select a folder.
 */
public class FolderSelectModule extends BlankDialogModule {

    // dialog that is used to select the folder
    private final JFileChooser fileDialog;

    // constructor
    public FolderSelectModule(String identifier, final JFrame owner, File initTo) {
        super(identifier);
        // set layout
        setLayout(new BorderLayout());

        // setup dialog
        fileDialog = new JFileChooser();
        fileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileDialog.setCurrentDirectory(initTo);

        // create the label that displays the current folder name
        final JLabel label = new JLabel(FileTools.shortenPath(fileDialog.getCurrentDirectory().getPath() + "\\", 50));
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
                    }
                }
            }
        });
    }

    @Override
    protected Object getValue(String identifier) {
        // return the directory of the selected folder
        return fileDialog.getCurrentDirectory();
    }

}
