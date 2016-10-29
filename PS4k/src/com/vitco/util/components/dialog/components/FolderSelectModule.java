package com.vitco.util.components.dialog.components;

import com.vitco.util.components.dialog.BlankDialogModule;
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

    // set the folder for this module
    private void setSelectedFolder(String path) {
        File file = new File(FileTools.ensureTrailingSeparator(path));
        File dir = file.isFile() ? file.getParentFile() : file;
        dir = dir != null && dir.exists() ? dir : null;

        // set the information in the correct event queue
        fileDialog.setSelectedFile(dir);
        fileDialog.setCurrentDirectory(dir);
        label.setText(FileTools.shortenPath(getSelectedFolder(), 50));
    }

    // obtain the currently selected folder
    private String getSelectedFolder() {
        File file = fileDialog.getSelectedFile();
        file = file == null ? fileDialog.getCurrentDirectory() : file;
        file = file.isFile() ? file.getParentFile() : file;
        return FileTools.ensureTrailingSeparator(file.getAbsolutePath());
    }

    // constructor
    public FolderSelectModule(String identifier, final Frame owner, File initTo) {
        super(identifier);
        // set layout
        setLayout(new BorderLayout());

        // setup dialog
        fileDialog = new JFileChooser();
        fileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // setup label
        label = new JLabel();
        label.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
        add(label, BorderLayout.CENTER);

        // initialize
        setSelectedFolder(initTo.getAbsolutePath());

        // create the "select folder" button
        DialogButton selectFolderButton = new DialogButton("Select Folder...");
        selectFolderButton.setFocusable(false);
        add(selectFolderButton, BorderLayout.WEST);
        selectFolderButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // open dialog to select folder location
                String currentSelectedFolder = getSelectedFolder();
                if (fileDialog.showDialog(owner, "Select") == JFileChooser.APPROVE_OPTION) {
                    setSelectedFolder(getSelectedFolder());
                    notifyContentChanged();
                } else {
                    setSelectedFolder(currentSelectedFolder); // roll back
                }
            }
        });
    }

    @Override
    protected String getValue(String identifier) {
        return getSelectedFolder();
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
            setSelectedFolder(pair[1]);
            notifyContentChanged();
            return true;
        }
        return false;
    }

}
