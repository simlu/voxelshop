package com.vitco.util.misc;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Custom file dialog
 */

public class CFileDialog extends JFileChooser {

    // list of files
    private final ArrayList<GeneralFilter> accepted = new ArrayList<GeneralFilter>();

    // constructor
    public CFileDialog() {
        super();
    }

    // =========================

    // add a file type
    public void addFileType(String ext) {
        accepted.add(new GeneralFilter(ext.toLowerCase(), ext.toUpperCase()));
    }

    // add a file type
    public void addFileType(String ext, String name) {
        accepted.add(new GeneralFilter(ext.toLowerCase(), name));
    }

    // select an existing file
    public File openFile(Frame mainFrame) {
        prepare();
        if (showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File selectedfile = getSelectedFile();
            if (selectedfile.exists()) {
                return selectedfile;
            }
        }
        return null;
    }

    // select a file (doesn't need to exist)
    public File saveFile(Frame mainFrame) {
        prepare();
        if (showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            // selected file
            File selectedFile = getSelectedFile();
            // currently selected ext
            if (getFileFilter() instanceof GeneralFilter) {
                String ext = ((GeneralFilter)getFileFilter()).getExt();
                // make sure filename ends correctly
                String dir = selectedFile.getPath();
                if(!dir.toLowerCase().endsWith("." + ext)) {
                    selectedFile = new File(dir + "." + ext);
                }
            }
            // dae file
            return selectedFile;
        }
        return null;
    }

    // get the current path (that is shown when this dialog is opened)
    public String getDialogPath() {
        return getCurrentDirectory().getAbsolutePath();
    }

    // set the current path (that is shown when this dialog is opened)
    public void setDialogPath(File path) {
        if (path.exists() && path.isDirectory()) {
            setCurrentDirectory(path);
        }
    }

    // =========================

    // helper - prepare this file chooser
    private void prepare() {
        resetChoosableFileFilters();
        if (!accepted.isEmpty()) {
            for (GeneralFilter filter : accepted) {
                addChoosableFileFilter(filter);
            }
            setFileFilter(accepted.get(0));
            setAcceptAllFileFilterUsed(false);
        }
    }

    // set the title of this dialog
    public void setTitle(String title) {
        setDialogTitle(title);
    }

    // helper - filter class
    private final class GeneralFilter extends FileFilter {
        private final String desc;
        private final String ext;

        private GeneralFilter(String ext, String name) {
            this.ext = ext;
            desc = name + " (*." + ext + ")";
        }

        public final String getExt() {
            return ext;
        }

        @Override
        public boolean accept(File f) {
            // we want to display folders
            return f.isDirectory() || f.getName().endsWith("." + ext);
        }

        @Override
        public String getDescription() {
            return desc;
        }
    }

}
