package com.vitco.layout.content.menu;

import com.jidesoft.action.DefaultDockableBarDockableHolder;
import com.vitco.importer.BinVox;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.file.FileTools;
import com.vitco.util.misc.CFileDialog;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Handles the main menu logic.
 */
public class MainMenuLogic extends MenuLogicPrototype implements MenuLogicInterface {

    // util for save/load/new file
    // ======================================
    // the location of active file (or null if none active)
    final String[] save_location = new String[] {null};
    // the file chooser
    final CFileDialog fc_vsd = new CFileDialog();
    // export file chooser
    final CFileDialog fc_export = new CFileDialog();
    // import file chooser
    final CFileDialog fc_import = new CFileDialog();

    // save file prompt (and overwrite prompt): true iff save was successful
    private boolean handleSaveDialog(Frame frame) {
        boolean result = false;
        File saveTo = fc_vsd.saveFile(frame);
        if (saveTo != null) {
            // make sure filename ends with *.vsd
            String dir = saveTo.getPath();
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
                default: break;
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
                    if (voxelCount >= VitcoSettings.MAX_VOXEL_COUNT_PER_LAYER) {
                        stop = true;
                        console.addLine(
                                langSelector.getString("import_voxel_limit_reached_pre") + " " +
                                        VitcoSettings.MAX_VOXEL_COUNT_PER_LAYER +
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
        fc_vsd.addFileType("vsd", "PS4k File");

        fc_import.addFileType("png");
        fc_import.addFileType("jpg");
        fc_import.addFileType("jpeg");
        fc_import.addFileType("binvox");

        fc_export.addFileType("dae");

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
                    File toOpen = fc_vsd.openFile(frame);
                    if (toOpen != null) {
                        data.loadFromFile(toOpen);
                        save_location[0] = toOpen.getPath(); // remember load location
                    }
                }
            }
        });

        // import file
        actionManager.registerAction("import_file_action", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkUnsavedChanges(frame)) {
                    File toOpen = fc_import.openFile(frame);
                    if (toOpen != null) {
                        String ext = fc_import.getCurrentExt();
                        if ("png".equals(ext) || "jpg".equals(ext) || "jpeg".equals(ext)) {
                            // -----------------
                            // import image data
                            data.selectLayer(data.createLayer("Import"));
                            try {
                                BufferedImage img = ImageIO.read(toOpen);
                                importImage(img);
                            } catch (IOException e1) {
                                errorHandler.handle(e1);
                            }
                        } else if ("binvox".equals(ext)) {
                            // ----------------
                            // import .binvox files
                            try {
                                BinVox binVox = new BinVox(toOpen);
                                if (binVox.hasLoaded()) {
                                    int[] l = binVox.getMin();
                                    int[] c = binVox.getCenter();
                                    byte[] vox = binVox.getVoxels();
                                    int[] s = binVox.getSize();
                                    data.selectLayer(data.createLayer("Import"));
                                    Color col = new Color(158, 194, 88);
                                    for (int x = 0; x < s[0]; x++) {
                                        for (int y = 0; y < s[1]; y++) {
                                            for (int z = 0; z < s[2]; z++) {
                                                if (vox[x + y*s[0] + z*s[0]*s[1]] == 1) {
                                                    data.addVoxelDirect(col, new int[] {y - c[1], -x + l[0], z - c[2]});
                                                }
                                            }
                                        }
                                    }
                                    // force a refresh of the data (redraw)
                                    data.setVisible(data.getSelectedLayer(), false);
                                    data.setVisible(data.getSelectedLayer(), true);
                                    data.clearHistoryV();
                                    data.resetHasChanged();
                                }
                            } catch (FileNotFoundException e1) {
                                errorHandler.handle(e1);
                            } catch (IOException e1) {
                                errorHandler.handle(e1);
                            }
                        }
                    }
                }
            }
        });

        // export file
        actionManager.registerAction("export_file_action", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    File exportTo = fc_export.saveFile(frame);
                    if (exportTo != null) {
                        String dir = exportTo.getPath();
                        // attached texture file
                        String textureImgDir = FileTools.changeExtension(dir, ".png");
                        File exportTextureTo = new File(textureImgDir);
                        // query if file already exists
                        if ((!exportTo.exists() ||
                                JOptionPane.showConfirmDialog(frame,
                                        dir + " " + langSelector.getString("replace_file_query"),
                                        langSelector.getString("replace_file_query_title"),
                                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) &&
                                (!exportTextureTo.exists() ||
                                        JOptionPane.showConfirmDialog(frame,
                                                textureImgDir + " " + langSelector.getString("replace_file_query"),
                                                langSelector.getString("replace_file_query_title"),
                                                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)) {

                            if (data.exportToCollada(exportTo, exportTextureTo)) {
                                console.addLine(langSelector.getString("export_file_successful"));
                            } else {
                                console.addLine(langSelector.getString("export_file_error"));
                            }
                        }
                    }
                }
            }

            @Override
            public boolean getStatus() {
                return data.anyLayerVoxelVisible();
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
                    actionManager.performWhenActionIsReady("program_closing_event", new Runnable() {
                        @Override
                        public void run() {
                            actionManager.getAction("program_closing_event").actionPerformed(e);
                        }
                    });
                    // save layout data
                    preferences.storeObject("custom_raw_layout_data",
                            ((DefaultDockableBarDockableHolder) frame).getLayoutPersistence().getLayoutRawData());

                    // do not print any thread errors (JFileChooser thread can cause this!)
                    try {
                        PrintStream nullStream = new PrintStream(new OutputStream() {
                            public void write(int b) throws IOException {
                            }

                            public void write(byte b[]) throws IOException {
                            }

                            public void write(byte b[], int off, int len) throws IOException {
                            }
                        }, true, "utf-8");
                        System.setErr(nullStream);
                        System.setOut(nullStream);
                    } catch (UnsupportedEncodingException e1) {
                        errorHandler.handle(e1);
                    }

                    // and exit
                    ((DefaultDockableBarDockableHolder) frame).setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    frame.dispose();
                }
            }
        });
    }

    @PreDestroy
    public final void finish() {
        // store folder locations (for open / close / import / export)
        preferences.storeString("file_open_close_dialog_last_directory", fc_vsd.getDialogPath());
        preferences.storeString("file_import_dialog_last_directory", fc_import.getDialogPath());
        preferences.storeString("file_export_dialog_last_directory", fc_export.getDialogPath());
    }

    @PostConstruct
    public final void init() {
        // load folder locations (for open / close / import / export)
        if (preferences.contains("file_open_close_dialog_last_directory")) {
            File file = new File(preferences.loadString("file_open_close_dialog_last_directory"));
            fc_vsd.setDialogPath(file);
        }
        if (preferences.contains("file_import_dialog_last_directory")) {
            File file = new File(preferences.loadString("file_import_dialog_last_directory"));
            fc_import.setDialogPath(file);
        }
        if (preferences.contains("file_export_dialog_last_directory")) {
            File file = new File(preferences.loadString("file_export_dialog_last_directory"));
            fc_export.setDialogPath(file);
        }
    }
}
