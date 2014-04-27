package com.vitco.layout.content.menu;

import com.jidesoft.action.DefaultDockableBarDockableHolder;
import com.sun.imageio.plugins.gif.GIFImageReader;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;
import com.vitco.importer.*;
import com.vitco.layout.content.mainview.MainView;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.file.FileTools;
import com.vitco.util.misc.CFileDialog;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Handles the main menu logic.
 */
public class MainMenuLogic extends MenuLogicPrototype implements MenuLogicInterface {

    // var & setter
    protected MainView mainView;
    @Autowired(required=true)
    public final void setMainView(MainView mainView) {
        this.mainView = mainView;
    }

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

    // import an image file
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
    }

    // import helper for voxel file
    private void importVoxelData(AbstractImporter importer, boolean shiftToCenter) {
        if (importer.hasLoaded()) {
            if (shiftToCenter) {
                int[] center = importer.getWeightedCenter();
                int[] highest = importer.getHighest();
                for (AbstractImporter.Layer layer : importer.getVoxel()) {
                    data.selectLayer(data.createLayer(layer.name));
                    for (int[] vox; layer.hasNext();) {
                        vox = layer.next();
                        data.addVoxelDirect(new Color(vox[3]),
                                new int[] {vox[0] - center[0], vox[1] - highest[1], vox[2] - center[2]});
                    }
                }
            } else {
                for (AbstractImporter.Layer layer : importer.getVoxel()) {
                    data.selectLayer(data.createLayer(layer.name));
                    for (int[] vox; layer.hasNext();) {
                        vox = layer.next();
                        data.addVoxelDirect(new Color(vox[3]),new int[] {vox[0], vox[1], vox[2]});
                    }
                }
            }
        }
    }
    // ======================================

    public void registerLogic(final Frame frame) {
        // initialize the filter
        fc_vsd.addFileType("vsd", "PS4k File");

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
                        if (!data.loadFromFile(toOpen)) {
                            console.addLine(langSelector.getString("error_on_file_load"));
                        }
                        save_location[0] = toOpen.getPath(); // remember load location
                    }
                }
            }
        });

        fc_import.addFileType(new String[] {"png", "jpg", "jpeg", "bmp"}, "Image");
        fc_import.addFileType("gif", "Animated Image");
        fc_import.addFileType("binvox");
        fc_import.addFileType("kv6");
        fc_import.addFileType("kvx");
        fc_import.addFileType("qb", "Qubicle Binary");
        fc_import.addFileType("vox", "Voxlap Engine File");
        fc_import.addFileType("vox", "MagicaVoxel File");
        fc_import.addFileType("vox", "Vox Game File");
        fc_import.addFileType("rawvox", "Raw Voxel Format");

        // import file
        actionManager.registerAction("import_file_action", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkUnsavedChanges(frame)) {
                    File toOpen = fc_import.openFile(frame);
                    if (toOpen != null) {
                        String ext = fc_import.getCurrentExt();
                        // todo: rework import so that history can stay (!)
                        //data.freshStart();
                        //data.deleteLayer(data.getSelectedLayer());
                        try {
                            if ("png".equals(ext) || "jpg".equals(ext) || "jpeg".equals(ext) || "bmp".equals(ext)) {
                                // -----------------
                                // import image data
                                BufferedImage img = ImageIO.read(toOpen);
                                if (img != null) {
                                    data.selectLayer(data.createLayer(FileTools.extractNameWithoutExtension(toOpen)));
                                    importImage(img);
                                } else {
                                    throw new IOException("Failed to load image with ImageIO.read()");
                                }
                            } else if ("gif".equals(ext)) {
                                // ----------------
                                // import gif image data (including frame animation)
                                ImageInputStream inputStream = ImageIO.createImageInputStream(toOpen);
                                try {
                                    ImageReader ir = new GIFImageReader(new GIFImageReaderSpi());
                                    ir.setInput(inputStream);
                                    int count = ir.getNumImages(true);
                                    if (count > 1) {
                                        for (int i = 0; i < count; i++) {
                                            int layerId = data.createLayer("Frame" + i);
                                            data.selectLayer(layerId);
                                            if (i < count - 1) {
                                                data.setVisible(layerId, false);
                                            }
                                            importImage(ir.read(i));
                                        }
                                    } else {
                                        data.selectLayer(data.createLayer(FileTools.extractNameWithoutExtension(toOpen)));
                                        importImage(ir.read(0));
                                    }
                                } finally {
                                    inputStream.close();
                                }
                            } else if ("binvox".equals(ext)) {
                                // ----------------
                                // import .binvox files
                                AbstractImporter importer = new BinVoxImporter(toOpen, FileTools.extractNameWithoutExtension(toOpen));
                                importVoxelData(importer, true);
                            } else if ("kv6".equals(ext)) {
                                // ----------------
                                // import .kv6 files
                                AbstractImporter importer = new Kv6Importer(toOpen, FileTools.extractNameWithoutExtension(toOpen));
                                importVoxelData(importer, false);
                            } else if ("kvx".equals(ext)) {
                                // ----------------
                                // import .kvx files
                                AbstractImporter importer = new KvxImporter(toOpen, FileTools.extractNameWithoutExtension(toOpen));
                                importVoxelData(importer, false);
                            } else if ("qb".equals(ext)) {
                                // ----------------
                                // import .qb files
                                AbstractImporter importer = new QbImporter(toOpen, FileTools.extractNameWithoutExtension(toOpen));
                                importVoxelData(importer, false);
                            } else if ("vox".equals(ext)) {
                                // ----------------
                                // import .vox files
                                AbstractImporter importer = new VoxImporter(toOpen, FileTools.extractNameWithoutExtension(toOpen));
                                importVoxelData(importer, true);
                            } else if ("rawvox".equals(ext)) {
                                // ----------------
                                // import .rawvox files
                                AbstractImporter importer = new RawVoxImporter(toOpen, FileTools.extractNameWithoutExtension(toOpen));
                                importVoxelData(importer, true);
                            }
                        } catch (IOException e1) {
                            console.addLine(langSelector.getString("error_on_file_import"));
                            //errorHandler.handle(e1);
                        }

                        // force a refresh of the data (redraw)
                        data.setVisible(data.getSelectedLayer(), false);
                        data.setVisible(data.getSelectedLayer(), true);
                        data.clearHistoryV();
                    }
                }
            }
        });

        fc_export.addFileType("dae", "COLLADA");
        fc_export.addFileType("png", "Image and Depth Map");

        // export file
        actionManager.registerAction("export_file_action", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {

                    File exportTo = fc_export.saveFile(frame);
                    String type = fc_export.getCurrentExt();
                    if (type != null && exportTo != null) {
                        String dir = exportTo.getPath();
                        // query if file already exists
                        if ((!exportTo.exists() ||
                                JOptionPane.showConfirmDialog(frame,
                                        dir + " " + langSelector.getString("replace_file_query"),
                                        langSelector.getString("replace_file_query_title"),
                                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)) {

                            if (type.equals("dae")) {
                                // -----------
                                // export collada (we need texture!)
                                String textureImgDir = FileTools.changeExtension(dir, ".png");
                                File exportTextureTo = new File(textureImgDir);
                                // query if texture file already exists
                                if ((!exportTextureTo.exists() ||
                                                JOptionPane.showConfirmDialog(frame,
                                                        textureImgDir + " " + langSelector.getString("replace_file_query"),
                                                        langSelector.getString("replace_file_query_title"),
                                                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)) {
                                    long time = System.currentTimeMillis();
                                    if (data.exportToCollada(exportTo, exportTextureTo)) {
                                        console.addLine(
                                                String.format(langSelector.getString("export_file_successful"), System.currentTimeMillis() - time)
                                        );
                                    } else {
                                        console.addLine(langSelector.getString("export_file_error"));
                                    }
                                }
                            } else if (type.equals("png")) {
                                // -----------
                                // export image and depth map
                                BufferedImage image = mainView.getImage();
                                try {
                                    ImageIO.write(image,"png",exportTo);
                                } catch (IOException e) {
                                    errorHandler.handle(e);
                                }
                                // check if we want to overwrite the depth map
                                File exportDepthMapTo = new File(FileTools.removeExtension(exportTo) + "_depth.png");
                                if ((!exportDepthMapTo.exists() ||
                                        JOptionPane.showConfirmDialog(frame,
                                                exportDepthMapTo.getPath() + " " + langSelector.getString("replace_file_query"),
                                                langSelector.getString("replace_file_query_title"),
                                                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)) {
                                    BufferedImage depth = mainView.getDepthImage();
                                    try {
                                        ImageIO.write(depth,"png",exportDepthMapTo);
                                    } catch (IOException e) {
                                        errorHandler.handle(e);
                                    }
                                }

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
                            @Override
                            public void write(int b) throws IOException {}

                            @Override
                            public void write(@SuppressWarnings("NullableProblems") byte b[]) throws IOException {}

                            @Override
                            public void write(@SuppressWarnings("NullableProblems") byte b[], int off, int len) throws IOException {}
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
