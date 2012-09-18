package com.vitco.logic.menu;

import com.jidesoft.action.DefaultDockableBarDockableHolder;
import com.vitco.res.VitcoSettings;
import com.vitco.util.action.types.StateActionPrototype;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Handles the main menu logic.
 */
public class MainMenuLogic extends MenuLogicPrototype implements MenuLogicInterface {

    // util for save/load/new file
    // ======================================
    // the location of active file (or null if none active)
    final String[] save_location = new String[] {null};
    // the file chooser
    final JFileChooser fc_vsd = new JFileChooser();
    // filter to only allow vsd files
    private static final class VSDFilter extends FileFilter
    {
        public boolean accept(File f)
        {
            return f.isDirectory() || f.getName().endsWith(".vsd");
        }

        public String getDescription()
        {
            return "PS4k File (*.vsd)";
        }
    }
    // import file chooser
    final JFileChooser fc_import = new JFileChooser();
    // filter to only allow import files (png)
    private static final class ImportFilter extends FileFilter
    {
        private final String[] names;
        private ImportFilter(String[] names) {
            this.names = names;
        }

        public boolean accept(File f)
        {
            if (f.isDirectory()) {
                return true;
            }
            for (String name : names) {
                if (f.getName().endsWith("." + name.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }

        public String getDescription()
        {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (String name : names) {
                result.append(!first ? "|" : "").append(name.toUpperCase()).append(" (*.").append(name.toLowerCase()).append(")");
                first = false;
            }
            return result.toString();
        }
    }
    // save file prompt (and overwrite prompt): true iff save was successful
    private boolean handleSaveDialog(Frame frame) {
        boolean result = false;
        int returnVal = fc_vsd.showSaveDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // make sure filename ends with *.vsd
            String dir = fc_vsd.getSelectedFile().getPath();
            if(!dir.toLowerCase().endsWith(".vsd")) {
                dir += ".vsd";
            }
            File saveTo = new File(dir);
            // query if file already exists
            if (!saveTo.exists() ||
                    JOptionPane.showConfirmDialog(frame,
                            dir + " " + langSelector.getString("replace_file_query"),
                            langSelector.getString("replace_file_query_title"),
                            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                // save file and remember it
                result = data.saveToFile(saveTo);
                if (result) {
                    save_location[0] = dir;
                }
            }
        }
        return result;
    }
    // handles unsaved changes: true iff we are save to discard after this was called
    private boolean checkUnsavedChanges(Frame frame) {
        boolean result = false;
        if (data.hasChanged()) {
            // option to save changes / erase changes / cancel
            switch (JOptionPane.showConfirmDialog(frame,
                    langSelector.getString("save_current_changes_query"),
                    langSelector.getString("save_current_changes_title"),
                    JOptionPane.YES_NO_CANCEL_OPTION)) {
                case JOptionPane.YES_OPTION: // save changes
                    if (save_location[0] != null) { // we already know where to save (ok)
                        File file = new File(save_location[0]);
                        result = data.saveToFile(file);
                    } else { // we dont know where
                        if (handleSaveDialog(frame)) {
                            result = true;
                        }
                    }
                    break;
                case JOptionPane.NO_OPTION: // don't save option
                    result = true;
                    break;
                case JOptionPane.CANCEL_OPTION: // cancel = do nothing
                    // cancel
                    break;
            }
        } else { // no unsaved changes
            result = true;
        }
        return result;
    }
    // import a file
    private void importImage(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        boolean stop = false;
        int voxelCount = 1;
        for (int y=height-1; y >= 0 && !stop; y--) {
            for (int x = 0; x < width && !stop; x++) {
                int rgb = img.getRGB(x,y);
                int alpha = (rgb & 0xff000000) >> 24;
                int  red = (rgb & 0x00ff0000) >> 16;
                int  green = (rgb & 0x0000ff00) >> 8;
                int  blue = rgb & 0x000000ff;
                if (alpha != 0) {
                    data.addVoxelDirect(new Color(red, green, blue), new int[]{
                            x - img.getWidth() / 2,
                            y + Math.round(VitcoSettings.VOXEL_GROUND_DISTANCE / VitcoSettings.VOXEL_SIZE) - img.getHeight(),
                            0
                    });
                    if (voxelCount >= VitcoSettings.VOXEL_COUNT_FILE_IMPORT_LIMIT) {
                        stop = true;
                        console.addLine(
                                langSelector.getString("import_voxel_limit_reached_pre") + " " +
                                        VitcoSettings.VOXEL_COUNT_FILE_IMPORT_LIMIT +
                                        " " + langSelector.getString("import_voxel_limit_reached_post"));
                    }
                    voxelCount++;
                }
            }
        }
        // force a refresh of the data (redraw)
        data.setVisible(data.getSelectedLayer(), false);
        data.setVisible(data.getSelectedLayer(), true);
        data.clearHistoryV();
        data.resetHasChanged();
    }
    // ======================================

    public void registerLogic(final Frame frame) {
        // initialize the filter
        fc_vsd.setFileFilter(new VSDFilter());
        fc_vsd.setAcceptAllFileFilterUsed(false);

        FileFilter pngFilter = new ImportFilter(new String[] {"png"});
        fc_import.addChoosableFileFilter(pngFilter);
        fc_import.addChoosableFileFilter(new ImportFilter(new String[] {"jpg"}));
        fc_import.addChoosableFileFilter(new ImportFilter(new String[] {"jpeg"}));
        fc_import.setFileFilter(pngFilter);
        fc_import.setAcceptAllFileFilterUsed(false);

        // save file
        actionManager.registerAction("save_file_action", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSaveDialog(frame);
            }
        });

        // load file
        actionManager.registerAction("load_file_action", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkUnsavedChanges(frame)) {
                    if (fc_vsd.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                        data.loadFromFile(fc_vsd.getSelectedFile());
                        save_location[0] = fc_vsd.getSelectedFile().getPath(); // remember load location
                    }
                }
            }
        });

        // import file
        actionManager.registerAction("import_file_action", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkUnsavedChanges(frame)) {
                    if (fc_import.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                        data.freshStart();
                        save_location[0] = null;
                        try {
                            BufferedImage img = ImageIO.read(fc_import.getSelectedFile());
                            importImage(img);
                        } catch (IOException e1) {
                            errorHandler.handle(e1);
                        }
                    }
                }
            }
        });

        // quick save
        actionManager.registerAction("quick_save_file_action", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) { // make sure we can save
                    File file = new File(save_location[0]);
                    data.saveToFile(file);
                }
            }

            @Override
            public boolean getStatus() {
                return save_location[0] != null;
            }
        });

        // new file
        actionManager.registerAction("new_file_action", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkUnsavedChanges(frame)) {
                    data.freshStart();
                    save_location[0] = null;
                }
            }
        });

        // register close event
        actionManager.registerAction("program_closing_event", new StateActionPrototype() {
            boolean shutdown = false;
            @Override
            public void action(ActionEvent actionEvent) {
                shutdown = true;
            }

            @Override
            public boolean getStatus() {
                return shutdown;
            }
        });

        // register closing action
        actionManager.registerActionIsUsed("program_closing_event");
        actionManager.registerAction("close_program_action", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (checkUnsavedChanges(frame)) {
                    // fire closing action
                    actionManager.tryExecuteAction("program_closing_event", e);
                    // save layout data
                    preferences.storeObject("custom_raw_layout_data",
                            ((DefaultDockableBarDockableHolder) frame).getLayoutPersistence().getLayoutRawData());

                    // do not print any thread errors (JFileChooser thread can cause this!)
                    PrintStream nullStream = new PrintStream(new OutputStream() {
                        public void write(int b) throws IOException {
                        }

                        public void write(byte b[]) throws IOException {
                        }

                        public void write(byte b[], int off, int len) throws IOException {
                        }
                    });
                    System.setErr(nullStream);
                    System.setOut(nullStream);

                    // and exit
                    ((DefaultDockableBarDockableHolder) frame).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.dispose();
                }
            }
        });
    }

    @PreDestroy
    public final void finish() {
        // store folder locations (for open / close / import)
        preferences.storeString("file_open_close_dialog_last_directory", fc_vsd.getCurrentDirectory().getAbsolutePath());
        preferences.storeString("file_import_dialog_last_directory", fc_import.getCurrentDirectory().getAbsolutePath());
    }

    @PostConstruct
    public final void init() {
        // load folder locations (for open / close / import)
        if (preferences.contains("file_open_close_dialog_last_directory")) {
            File file = new File(preferences.loadString("file_open_close_dialog_last_directory"));
            if (file.isDirectory()) {
                fc_vsd.setCurrentDirectory(file);
            }
        }
        if (preferences.contains("file_import_dialog_last_directory")) {
            File file = new File(preferences.loadString("file_import_dialog_last_directory"));
            if (file.isDirectory()) {
                fc_import.setCurrentDirectory(file);
            }
        }
    }
}
