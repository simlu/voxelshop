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
        prepare(true);
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
        prepare(false);
        if (showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            // selected file
            File selectedFile = getSelectedFile();
            // currently selected ext
            String ext = getCurrentExt();
            if (ext != null) {
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
    private void prepare(boolean allowAllFiles) {
        resetChoosableFileFilters();
        // create general file chooser that holds all file types
        if (!accepted.isEmpty()) {
            for (GeneralFilter filter : accepted) {
                addChoosableFileFilter(filter);
            }
            if (allowAllFiles) {
                // create "all files" file chooser
                CumulativeGeneralFilter cumulativeGeneralFilter = new CumulativeGeneralFilter(accepted);
                setFileFilter(cumulativeGeneralFilter);
            } else {
                setFileFilter(accepted.get(0));
            }
            setAcceptAllFileFilterUsed(false);
        }
    }

    // set the title of this dialog
    public void setTitle(String title) {
        setDialogTitle(title);
    }

    // get the extension that is currently selected
    // returns null if the extension is not known
    public final String getCurrentExt() {
        FileFilter filter = this.getFileFilter();
        if (filter instanceof ExtensionFileFilter) {
            return ((ExtensionFileFilter)filter).getExt();
        }
        return null;
    }

    // helper - filter class for multiple endings
    private final class CumulativeGeneralFilter extends ExtensionFileFilter {
        private final ArrayList<String> exts = new ArrayList<String>();

        private CumulativeGeneralFilter(ArrayList<GeneralFilter> filters) {
            for (GeneralFilter filter : filters) {
                exts.add(filter.getExt());
            }
        }

        @Override
        public final String getExt() {
            String extension = "";
            String fileName = getSelectedFile().getName();
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i+1);
            }
            return extension;
        }

        @Override
        public boolean accept(File f) {
            // we want to display folders
            if (f.isDirectory()) {
                return true;
            } else {
                String name = f.getName();
                for (String ext : exts) {
                    if (name.endsWith("." + ext)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "All Files (*.*)";
        }
    }

    // helper - filter class
    private final class GeneralFilter extends ExtensionFileFilter {
        private final String desc;
        private final String ext;

        private GeneralFilter(String ext, String name) {
            this.ext = ext;
            desc = name + " (*." + ext + ")";
        }

        @Override
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

    // helper - abstract class to define the getExt() method
    private abstract class ExtensionFileFilter extends FileFilter  {
        abstract String getExt();
    }

}
