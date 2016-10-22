package com.vitco.layout.content.menu;

import com.jidesoft.action.DefaultDockableBarDockableHolder;
import com.sun.imageio.plugins.gif.GIFImageReader;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;
import com.vitco.core.data.container.Voxel;
import com.vitco.export.*;
import com.vitco.export.collada.ColladaExportWrapper;
import com.vitco.export.collada.ColladaFile;
import com.vitco.export.generic.ExportDataManager;
import com.vitco.importer.*;
import com.vitco.layout.content.mainview.MainView;
import com.vitco.low.CubeIndexer;
import com.vitco.low.hull.HullManagerExt;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.components.dialog.UserInputDialog;
import com.vitco.util.components.dialog.UserInputDialogListener;
import com.vitco.util.components.dialog.components.*;
import com.vitco.util.components.progressbar.ProgressDialog;
import com.vitco.util.components.progressbar.ProgressWorker;
import com.vitco.util.file.FileTools;
import com.vitco.util.misc.CFileDialog;
import com.vitco.util.misc.ColorTools;
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
import java.util.ArrayList;

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
    private final String[] save_location = new String[] {null};
    // the file chooser
    private final CFileDialog fc_vsd = new CFileDialog();
    // import file chooser
    private final CFileDialog fc_import = new CFileDialog();

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
                    int layerId = data.createLayer(layer.name);
                    data.selectLayer(layerId);
                    data.setVisible(layerId, layer.isVisible());
                    for (int[] vox; layer.hasNext();) {
                        vox = layer.next();
                        data.addVoxelDirect(new Color(vox[3]),
                                new int[] {vox[0] - center[0], vox[1] - highest[1], vox[2] - center[2]});
                    }
                }
            } else {
                for (AbstractImporter.Layer layer : importer.getVoxel()) {
                    int layerId = data.createLayer(layer.name);
                    data.selectLayer(layerId);
                    data.setVisible(layerId, layer.isVisible());
                    for (int[] vox; layer.hasNext();) {
                        vox = layer.next();
                        data.addVoxelDirect(new Color(vox[3]),new int[] {vox[0], vox[1], vox[2]});
                    }
                }
            }
        }
    }
    // ======================================

    public final void openFile(File file) {
        if (!data.loadFromFile(file)) {
            console.addLine(langSelector.getString("error_on_file_load"));
        }
        save_location[0] = file.getPath(); // remember load location
    }

    public final void registerLogic(final Frame frame) {
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
                        openFile(toOpen);
                    }
                }
            }
        });

        fc_import.addFileType(new String[] {"png", "jpg", "jpeg", "bmp"}, "Image");
        fc_import.addFileType("gif", "Animated Image");
        fc_import.addFileType("binvox");
        fc_import.addFileType("kv6");
        fc_import.addFileType("pnx", "Pnx Exchange File");
        fc_import.addFileType("kvx");
        fc_import.addFileType("qb", "Qubicle Binary");
        fc_import.addFileType("vox", "Voxlap Engine File");
        fc_import.addFileType("vox", "MagicaVoxel File");
        fc_import.addFileType("vox", "Vox Game File");
        fc_import.addFileType("vxl", "C&C File Format");
        fc_import.addFileType("rawvox", "Raw Voxel Format");

        // import file
        actionManager.registerAction("import_file_action", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkUnsavedChanges(frame)) {
                    final File toOpen = fc_import.openFile(frame);
                    if (toOpen != null) {
                        String ext = fc_import.getCurrentExt();
                        // todo: rework import so that history can stay (!)
                        //data.freshStart();
                        //data.deleteLayer(data.getSelectedLayer());
                        final ProgressDialog dialog = new ProgressDialog(frame);
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
                                dialog.start(new ProgressWorker() {
                                    @Override
                                    protected Object doInBackground() throws Exception {
                                        dialog.setActivity("Importing File...", true);
                                        AbstractImporter importer = new BinVoxImporter(toOpen, FileTools.extractNameWithoutExtension(toOpen));
                                        importVoxelData(importer, true);
                                        return null;
                                    }
                                });

                            } else if ("kv6".equals(ext)) {
                                // ----------------
                                // import .kv6 files
                                dialog.start(new ProgressWorker() {
                                    @Override
                                    protected Object doInBackground() throws Exception {
                                        dialog.setActivity("Importing File...", true);
                                        AbstractImporter importer = new Kv6Importer(toOpen, FileTools.extractNameWithoutExtension(toOpen));
                                        importVoxelData(importer, false);
                                        return null;
                                    }
                                });
                            } else if ("pnx".equals(ext)) {
                                // ----------------
                                // import .pnx files
                                dialog.start(new ProgressWorker() {
                                    @Override
                                    protected Object doInBackground() throws Exception {
                                        dialog.setActivity("Importing File...", true);
                                        AbstractImporter importer = new PnxImporter(toOpen, FileTools.extractNameWithoutExtension(toOpen));
                                        importVoxelData(importer, false);
                                        return null;
                                    }
                                });
                            } else if ("kvx".equals(ext)) {
                                // ----------------
                                // import .kvx files
                                dialog.start(new ProgressWorker() {
                                    @Override
                                    protected Object doInBackground() throws Exception {
                                        dialog.setActivity("Importing File...", true);
                                        AbstractImporter importer = new KvxImporter(toOpen, FileTools.extractNameWithoutExtension(toOpen));
                                        importVoxelData(importer, false);
                                        return null;
                                    }
                                });
                            } else if ("qb".equals(ext)) {
                                // ----------------
                                // import .qb files
                                dialog.start(new ProgressWorker() {
                                    @Override
                                    protected Object doInBackground() throws Exception {
                                        dialog.setActivity("Importing File...", true);
                                        AbstractImporter importer = new QbImporter(toOpen, FileTools.extractNameWithoutExtension(toOpen));
                                        importVoxelData(importer, false);
                                        return null;
                                    }
                                });
                            } else if ("vox".equals(ext)) {
                                // ----------------
                                // import .vox files
                                dialog.start(new ProgressWorker() {
                                    @Override
                                    protected Object doInBackground() throws Exception {
                                        dialog.setActivity("Importing File...", true);
                                        AbstractImporter importer = new VoxImporter(toOpen, FileTools.extractNameWithoutExtension(toOpen));
                                        importVoxelData(importer, true);
                                        return null;
                                    }
                                });
                            } else if ("rawvox".equals(ext)) {
                                // ----------------
                                // import .rawvox files
                                dialog.start(new ProgressWorker() {
                                    @Override
                                    protected Object doInBackground() throws Exception {
                                        dialog.setActivity("Importing File...", true);
                                        AbstractImporter importer = new RawVoxImporter(toOpen, FileTools.extractNameWithoutExtension(toOpen));
                                        importVoxelData(importer, true);
                                        return null;
                                    }
                                });
                            } else if ("vxl".equals(ext)) {
                                // ----------------
                                // import .vxl files (C&C)
                                dialog.start(new ProgressWorker() {
                                    @Override
                                    protected Object doInBackground() throws Exception {
                                        dialog.setActivity("Importing File...", true);
                                        AbstractImporter importer = new CCVxlImporter(toOpen, FileTools.extractNameWithoutExtension(toOpen));
                                        importVoxelData(importer, true);
                                        return null;
                                    }
                                });
                            }
                        } catch (IOException e1) {
                            console.addLine(langSelector.getString("error_on_file_import"));
                            //errorHandler.handle(e1);
                        }

                        // force a refresh of the data (redraw)
                        // todo: do this properly
                        int layerId = data.getSelectedLayer();
                        boolean vis = data.getLayerVisible(layerId);
                        data.setVisible(layerId, !vis);
                        data.setVisible(layerId, vis);
                        data.clearHistoryV();
                    }
                }
            }
        });

        // ==========================
        // set up exporter dialog
        final UserInputDialog dialog = new UserInputDialog(frame, "Export Voxels", JOptionPane.CANCEL_OPTION);

        // add submit buttons
        dialog.addButton("Export", JOptionPane.OK_OPTION);
        dialog.addButton("Cancel", JOptionPane.CANCEL_OPTION);

        // add file select
        FieldSet location = new FieldSet("location", "Location");
        location.addComponent(new FileSelectModule("file", new File("exported"), frame));
        dialog.addFieldSet(location);

        // ---------------

        // set up Collada format
        final FieldSet collada = new FieldSet("collada", "Collada (*.dae)");
        collada.addComponent(new LabelModule("Select Export Options:"));
        collada.addComponent(new ComboBoxModule("type", new String[][] {
                new String[] {"poly2tri", "Optimal (Poly2Tri)"},
                new String[] {"minimal", "Low Poly (Rectangular)"},
                new String[] {"naive", "Naive (Unoptimized)"},
                new String[] {"legacy", "Legacy (Unoptimized)"}
        }, 0));
        // add information for "poly2tri"
        LabelModule poly2triInfo = new LabelModule("Info: This is the preferred exporter. The mesh is highly " +
                "optimized and no rendering artifacts can appear.");
        poly2triInfo.setVisibleLookup("collada.type=poly2tri");
        collada.addComponent(poly2triInfo);
        // add information for "optimalGreedy"
        LabelModule minimalInfo = new LabelModule("Info: This exporter results in a very low triangle " +
                "count, however rendering artifacts can appear (T-Junction problems).");
        minimalInfo.setVisibleLookup("collada.type=minimal");
        collada.addComponent(minimalInfo);
        // add information for "naive"
        LabelModule naiveInfo = new LabelModule("Info: Unoptimized exporter. Creates two triangles for each voxel. " +
                "This might be useful if the voxel mesh needs further processing.");
        naiveInfo.setVisibleLookup("collada.type=naive");
        collada.addComponent(naiveInfo);
        // add information for "legacy"
        LabelModule legacyInfo = new LabelModule("Info: Unoptimized legacy exporter. Useful if you want to " +
                "process the mesh further. Suitable for 3D printing (uses vertex coloring). Also currently the " +
                "only exporter that correctly exports textured voxels (will change in the future).");
        legacyInfo.setVisibleLookup("collada.type=legacy");
        collada.addComponent(legacyInfo);

        // option: remove holes
        CheckBoxModule removeEnclosed = new CheckBoxModule("remove_holes", "Fill in enclosed holes", true);
        removeEnclosed.setInvisibleLookup("collada.type=legacy");
        collada.addComponent(removeEnclosed);

        // option: layer as object
        CheckBoxModule layersAsObjects = new CheckBoxModule("layers_as_objects", "Create a new object for every layer", true);
        layersAsObjects.setInvisibleLookup("collada.type=legacy");
        collada.addComponent(layersAsObjects);

        // option: make texture edges save (pad textures)
        CheckBoxModule padTextures = new CheckBoxModule("pad_textures", "Use Textures Padding", true);
        padTextures.setInvisibleLookup("collada.type=legacy");
        collada.addComponent(padTextures);

        // option: export orthogonal vertex normals
        CheckBoxModule exportOrthogonalVertexNormals = new CheckBoxModule(
                "export_orthogonal_vertex_normals", "Export orthogonal vertex normals", false);
        exportOrthogonalVertexNormals.setInvisibleLookup("collada.type=legacy");
        collada.addComponent(exportOrthogonalVertexNormals);
        LabelModule exportOrthogonalVertexNormalsInfo = new LabelModule(
                "Info: Exporting orthogonal vertex normals can help with flat shading, but significantly increases vertex count. " +
                "Usually the preferred way is to instead enable flat shading for the engine itself. No normals are exported when unchecked."
        );
        exportOrthogonalVertexNormalsInfo.setInvisibleLookup("collada.type=legacy");
        collada.addComponent(exportOrthogonalVertexNormalsInfo);

        // option: use vertex colors
        CheckBoxModule useVertexColors = new CheckBoxModule("use_vertex_coloring", "Use vertex coloring (higher triangle count)", false);
        useVertexColors.setInvisibleLookup("collada.type=legacy");
        useVertexColors.setVisibleLookup("collada.use_black_edges=false");
        useVertexColors.setStrikeThrough(true);
        collada.addComponent(useVertexColors);

        // option: export with black outline
        CheckBoxModule useBlackOutline = new CheckBoxModule("use_black_edges", "Use black edges", false);
        useBlackOutline.setInvisibleLookup("collada.type=legacy");
        useBlackOutline.setVisibleLookup("collada.use_vertex_coloring=false");
        useBlackOutline.setStrikeThrough(true);
        collada.addComponent(useBlackOutline);

        // option: force power of two textures
        CheckBoxModule forcePOT = new CheckBoxModule("force_pot", "Use Power of Two textures", false);
        forcePOT.setInvisibleLookup("collada.type=legacy");
        forcePOT.setVisibleLookup("collada.use_vertex_coloring=false");
        collada.addComponent(forcePOT);

        // option: export with y-up or z-up
        CheckBoxModule useYup = new CheckBoxModule("use_yup", "Set Y instead of Z as the up axis", false);
        useYup.setInvisibleLookup("collada.type=legacy");
        collada.addComponent(useYup);

        LabelModule setOriginModeText = new LabelModule("Select Origin Mode:");
        setOriginModeText.setInvisibleLookup("collada.type=legacy");
        collada.addComponent(setOriginModeText);
        ComboBoxModule setOriginModeSelect = new ComboBoxModule("origin_mode", new String[][]{
                new String[]{"cross", "Use Cross"},
                new String[]{"center", "Use Object Center"},
                new String[]{"plane_center", "Use Object Center Projected onto Plane"},
                new String[]{"box_center", "Use Bounding Box Center"},
                new String[]{"box_plane_center", "Use Bounding Box Center Projected onto Plane"}
        }, 0);
        setOriginModeSelect.setInvisibleLookup("collada.type=legacy");
        collada.addComponent(setOriginModeSelect);

        // ---------------

        // add "render" export
        FieldSet imageRenderer = new FieldSet("image_renderer", "Render (*.png)");
        imageRenderer.addComponent(new LabelModule("Select Export Options:"));
        TextInputModule depthMapFileName = new TextInputModule("depth_map", "Name (Depth Render):", "depth", true);
        depthMapFileName.setEnabledLookup("export_type=image_renderer&image_renderer.render_depth=true");
        depthMapFileName.setVisibleLookup("export_type=image_renderer");
        imageRenderer.addComponent(depthMapFileName);
        imageRenderer.addComponent(new CheckBoxModule("render_depth", "Render Depth Image", true));

        // ---------------

        // add "vox (the game)" exporter
        FieldSet voxExporter = new FieldSet("vox_game_format", "Vox Game Format (*.vox)");

        // add information for voxel format
        LabelModule label_vox_game = new LabelModule("Note: Does not support textured voxels. This file format is used by vox-game.com");
        label_vox_game.setVisibleLookup("export_type=vox_game_format");
        voxExporter.addComponent(label_vox_game);

        // ---------------

        // add "kv6" exporter
        FieldSet kv6Exporter = new FieldSet("kv6_format", "Kv6 Format (*.kv6)");

        // add option to set weighted center
        final CheckBoxModule use_weighted_center = new CheckBoxModule("use_weighted_center", "Use weighted center instead of origin", false);
        kv6Exporter.addComponent(use_weighted_center);

        // add information for voxel format
        LabelModule label_kv6 = new LabelModule("Note: Does not support textured voxels. This file format is used by Ace of Spades and Slab6.");
        label_kv6.setVisibleLookup("export_type=kv6_format");
        kv6Exporter.addComponent(label_kv6);

        // ---------------

        // add "vox (voxlap)" exporter
        FieldSet voxVoxLapExporter = new FieldSet("voxlap_format", "VoxLap Format (*.vox)");

        // ---------------

        // add "pnx" exporter
        FieldSet pnxExporter = new FieldSet("pnx_format", "Pnx Format (*.pnx)");

        // add information for voxel format
        LabelModule label_pnx = new LabelModule("Highly compressed and flexible format that is easy to import.");
        label_pnx.setVisibleLookup("export_type=pnx_format");
        pnxExporter.addComponent(label_pnx);

        // ---------------

        // add "qb" exporter
        FieldSet qbExporter = new FieldSet("qb_format", "QB Format (*.qb)");

        // add information for voxel format
        LabelModule label_qb = new LabelModule("Qubicle 1.0 exchange format.");
        label_qb.setVisibleLookup("export_type=qb_format");
        qbExporter.addComponent(label_qb);

        final CheckBoxModule use_compression = new CheckBoxModule("use_compression", "Use compression", true);
        qbExporter.addComponent(use_compression);

        LabelModule compression_info = new LabelModule("Info: Compression saves a lot of space and makes opening and " +
                "saving the file faster. Un-check for StoneHearth.");
        qbExporter.addComponent(compression_info);

        final CheckBoxModule use_box_as_matrix = new CheckBoxModule("use_box_as_matrix", "Use bounding box as matrix", false);
        qbExporter.addComponent(use_box_as_matrix);

        LabelModule box_as_matrix_info = new LabelModule(
                "Warning: This option will result in loss of information for voxels outside the bounding box. " +
                "Use this setting to gain control over the matrix size. " +
                "Check for StoneHearth and set bounding box to 31 41 31."
        );
        qbExporter.addComponent(box_as_matrix_info);

        final CheckBoxModule use_origin_as_zero = new CheckBoxModule("use_origin_as_zero", "Use origin as zero", true);
        qbExporter.addComponent(use_origin_as_zero);

        LabelModule origin_as_zero_info = new LabelModule(
                "Info: Un-checking will move exported voxel into positive space. This means voxels are " +
                "shifted when re-importing the exported file. Un-check for StoneHearth."
        );
        qbExporter.addComponent(origin_as_zero_info);

        final CheckBoxModule use_vis_mask_encoding = new CheckBoxModule("use_vis_mask_encoding", "Use visibility mask encoding.", true);
        qbExporter.addComponent(use_vis_mask_encoding);

        LabelModule vis_mask_encoding_info = new LabelModule(
                "Info: This will encode voxel side visibility information, which can " +
                "result in faster load time. Un-check for StoneHearth."
        );
        qbExporter.addComponent(vis_mask_encoding_info);

        final CheckBoxModule use_right_handed_z_axis_orientation = new CheckBoxModule("use_right_handed_z_axis_orientation", "Use right handed z-axis orientation.", true);
        qbExporter.addComponent(use_right_handed_z_axis_orientation);

        LabelModule right_handed_z_axis_orientation_info = new LabelModule(
                "Info: This option can affect file size. Check for StoneHearth."
        );
        qbExporter.addComponent(right_handed_z_axis_orientation_info);

        // ---------------

        // add all formats
        dialog.addComboBox("export_type", new FieldSet[] {
                collada, voxExporter, voxVoxLapExporter, kv6Exporter, pnxExporter, qbExporter, imageRenderer
        }, 0);

        // ---------------

        // try to load the serialization
        Object serialization = preferences.loadObject("export_dialog_serialization");
        if (serialization != null && serialization instanceof ArrayList) {
            ArrayList data = (ArrayList) serialization;
            ArrayList<String[]> validatedData = new ArrayList<String[]>();
            for (Object pair : data) {
                if (pair instanceof String[]) {
                    String[] confirmedPair = (String[]) pair;
                    if (((String[]) pair).length == 2) {
                        validatedData.add(confirmedPair);
                    }
                }
            }
            dialog.loadSerialization(validatedData);
        }

        // listen to events
        dialog.setListener(new UserInputDialogListener() {
            @Override
            public boolean onClose(int resultFlag) {
                // user approved the dialog
                if (resultFlag == JOptionPane.OK_OPTION) {
                    final String baseName = dialog.getValue("location.file");
                    // validate folder (that it exists and is actually a folder)
                    File toValidateFolder = new File(baseName);
                    if (!toValidateFolder.getParentFile().exists() || !toValidateFolder.getParentFile().isDirectory()) {
                        JOptionPane.showMessageDialog(frame, langSelector.getString("error_invalid_folder"));
                        return false;
                    }

                    // handle logic
                    if (dialog.is("export_type=image_renderer")) {

                        // ===========

                        // -- export render
                        // extract file name
                        final File exportRenderTo = new File(baseName + (baseName.endsWith(".png") ? "" : ".png"));
                        // check if file exists
                        if (exportRenderTo.exists()) {
                            if (JOptionPane.showConfirmDialog(frame,
                                    exportRenderTo.getPath() + " " + langSelector.getString("replace_file_query"),
                                    langSelector.getString("replace_file_query_title"),
                                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                                return false;
                            }
                        }

                        // -- export depth map
                        boolean exportDepthMap = false;
                        File exportDepthMapTo = null;
                        if (dialog.is("image_renderer.render_depth=true")) {
                            // extract file name
                            String depthBaseName = dialog.getValue("image_renderer.depth_map");
                            exportDepthMapTo = new File(FileTools.ensureTrailingSeparator(exportRenderTo.getParent()) + depthBaseName + (depthBaseName.endsWith(".png") ? "" : ".png"));
                            // check if file exists
                            if (exportDepthMapTo.exists()) {
                                if (JOptionPane.showConfirmDialog(frame,
                                        exportDepthMapTo.getPath() + " " + langSelector.getString("replace_file_query"),
                                        langSelector.getString("replace_file_query_title"),
                                        JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                                    return false;
                                }
                            }
                            exportDepthMap = true;
                        }

                        // create progress dialog
                        final ProgressDialog progressDialog = new ProgressDialog(frame);

                        final File finalExportDepthMapTo = exportDepthMapTo;
                        final boolean finalExportDepthMap = exportDepthMap;
                        progressDialog.start(new ProgressWorker() {
                            @Override
                            protected Object doInBackground() throws Exception {
                                // export color render (image)
                                progressDialog.setActivity("Writing Render...", true);
                                BufferedImage image = mainView.getImage();
                                try {
                                    ImageIO.write(image,"png", exportRenderTo);
                                } catch (IOException e) {
                                    errorHandler.handle(e);
                                }
                                // export depth map
                                if (finalExportDepthMap) {
                                    progressDialog.setActivity("Writing Depth Render...", true);
                                    BufferedImage depth = mainView.getDepthImage();
                                    try {
                                        ImageIO.write(depth,"png", finalExportDepthMapTo);
                                    } catch (IOException e) {
                                        errorHandler.handle(e);
                                    }
                                }
                                return null;
                            }
                        });

                        // ===========

                    } else if (dialog.is("export_type=collada")) {

                        // ===========

                        // -- export collada
                        // extract file name
                        final File exportColladaTo = new File(baseName + (baseName.endsWith(".dae") ? "" : ".dae"));
                        // check if file exists
                        if (exportColladaTo.exists()) {
                            if (JOptionPane.showConfirmDialog(frame,
                                    exportColladaTo.getPath() + " " + langSelector.getString("replace_file_query"),
                                    langSelector.getString("replace_file_query_title"),
                                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                                return false;
                            }
                        }
                        // extract texture name (this is only actually used by the legacy
                        // exporter and to query the user if the file should be overwritten)
                        final File exportTextureTo = new File(FileTools.changeExtension(exportColladaTo.getPath(), "_texture0.png"));
                        // check if file exists
                        if (exportTextureTo.exists()) {
                            if (JOptionPane.showConfirmDialog(frame,
                                    exportTextureTo.getPath() + " " + langSelector.getString("replace_file_query"),
                                    langSelector.getString("replace_file_query_title"),
                                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                                return false;
                            }
                        }

                        // -- do the actual exporting
                        if (dialog.is("collada.type=legacy")) {
                            // -- export legacy (todo: remove when other is strictly better)
                            final ProgressDialog progressDialog = new ProgressDialog(frame);

                            progressDialog.start(new ProgressWorker() {
                                @Override
                                protected Object doInBackground() throws Exception {
                                    progressDialog.setActivity("Writing Legacy Format...", true);
                                    long time = System.currentTimeMillis();
                                    if (ColladaFile.exportLegacy(data, errorHandler, exportColladaTo, exportTextureTo)) {
                                        console.addLine(
                                                String.format(langSelector.getString("export_file_successful"),
                                                        System.currentTimeMillis() - time)
                                        );
                                    } else {
                                        console.addLine(langSelector.getString("export_file_error"));
                                    }
                                    return null;
                                }
                            });
                            // ----
                        } else {
                            // -- default export
                            // create progress dialog
                            final ProgressDialog progressDialog = new ProgressDialog(frame);
                            // do the exporting
                            progressDialog.start(new ProgressWorker() {
                                @Override
                                protected Object doInBackground() throws Exception {

                                    ColladaExportWrapper colladaExportWrapper = new ColladaExportWrapper(progressDialog, console);

                                    // set the "use layers" flag
                                    colladaExportWrapper.setUseLayers(dialog.is("collada.layers_as_objects=true"));
                                    // set remove holes flag
                                    colladaExportWrapper.setRemoveHoles(dialog.is("collada.remove_holes=true"));
                                    // set pad textures flag
                                    colladaExportWrapper.setPadTextures(dialog.is("collada.pad_textures=true"));
                                    // set use vertex colors flag
                                    colladaExportWrapper.setUseColoredVertices(dialog.is("collada.use_vertex_coloring=true"));
                                    // set use black outline
                                    colladaExportWrapper.setUseBlackOutline(dialog.is("collada.use_black_edges=true"));
                                    // set force power of two force textures
                                    colladaExportWrapper.setForcePOT(dialog.is("collada.force_pot=true"));
                                    // set the file name (only used if the layers are not used)
                                    colladaExportWrapper.setObjectName(FileTools.extractNameWithoutExtension(exportColladaTo));
                                    // set the YUP flag (whether to use z-up or y-up)
                                    colladaExportWrapper.setUseYUP(dialog.is("collada.use_yup=true"));
                                    // set "export exportOrthogonalVertexNormals vertex normals" flag
                                    colladaExportWrapper.setExportOrthogonalVertexNormals(dialog.is("collada.export_orthogonal_vertex_normals=true"));

                                    // set the center mode
                                    if (dialog.is("collada.origin_mode=cross")) {
                                        colladaExportWrapper.setOriginMode(ColladaExportWrapper.ORIGIN_CROSS);
                                    } else if (dialog.is("collada.origin_mode=center")) {
                                        colladaExportWrapper.setOriginMode(ColladaExportWrapper.ORIGIN_CENTER);
                                    } else if (dialog.is("collada.origin_mode=plane_center")) {
                                        colladaExportWrapper.setOriginMode(ColladaExportWrapper.ORIGIN_PLANE_CENTER);
                                    } else if (dialog.is("collada.origin_mode=box_center")) {
                                        colladaExportWrapper.setOriginMode(ColladaExportWrapper.ORIGIN_BOX_CENTER);
                                    } else if (dialog.is("collada.origin_mode=box_plane_center")) {
                                        colladaExportWrapper.setOriginMode(ColladaExportWrapper.ORIGIN_BOX_PLANE_CENTER);
                                    }

                                    // set the algorithm type
                                    if (dialog.is("collada.type=minimal")) {
                                        colladaExportWrapper.setAlgorithm(ExportDataManager.MINIMAL_RECT_ALGORITHM);
                                    } else if (dialog.is("collada.type=poly2tri")) {
                                        colladaExportWrapper.setAlgorithm(ExportDataManager.POLY2TRI_ALGORITHM);
                                    } else if (dialog.is("collada.type=naive")) {
                                        colladaExportWrapper.setAlgorithm(ExportDataManager.NAIVE_ALGORITHM);
                                    }

                                    long time = System.currentTimeMillis();
                                    if (colladaExportWrapper.export(data, errorHandler, exportColladaTo)) {
                                        console.addLine(
                                                String.format(langSelector.getString("export_file_successful"),
                                                        System.currentTimeMillis() - time)
                                        );
                                    } else {
                                        console.addLine(langSelector.getString("export_file_error"));
                                    }
                                    return null;
                                }
                            });
                            // ------------

                        }

                        // ===========

                    } else if (dialog.is("export_type=vox_game_format")) {

                        // ===========
                        // -- handle vox file format

                        // create progress dialog
                        final ProgressDialog progressDialog = new ProgressDialog(frame);

                        // do the exporting
                        progressDialog.start(new ProgressWorker() {
                            @Override
                            protected Object doInBackground() throws Exception {

                                // extract file name
                                final File exportTo = new File(baseName + (baseName.endsWith(".vox") ? "" : ".vox"));
                                // check if file exists
                                if (exportTo.exists()) {
                                    if (JOptionPane.showConfirmDialog(frame,
                                            exportTo.getPath() + " " + langSelector.getString("replace_file_query"),
                                            langSelector.getString("replace_file_query_title"),
                                            JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                                        return false;
                                    }
                                }

                                // export vox engine format
                                boolean success;
                                long time = System.currentTimeMillis();
                                try {
                                    VoxGameExporter exporter = new VoxGameExporter(exportTo, data, progressDialog, console);
                                    success = exporter.writeData();
                                } catch (IOException ignored) {
                                    success = false;
                                }
                                if (success) {
                                    console.addLine(
                                            String.format(langSelector.getString("export_file_successful"),
                                                    System.currentTimeMillis() - time)
                                    );
                                } else {
                                    console.addLine(langSelector.getString("export_file_error"));
                                }

                                return null;
                            }
                        });

                        // ===========
                    } else if (dialog.is("export_type=kv6_format")) {

                        // ===========
                        // -- handle kv6 file format

                        // create progress dialog
                        final ProgressDialog progressDialog = new ProgressDialog(frame);

                        // do the exporting
                        progressDialog.start(new ProgressWorker() {
                            @Override
                            protected Object doInBackground() throws Exception {

                                // extract file name
                                final File exportTo = new File(baseName + (baseName.endsWith(".kv6") ? "" : ".kv6"));
                                // check if file exists
                                if (exportTo.exists()) {
                                    if (JOptionPane.showConfirmDialog(frame,
                                            exportTo.getPath() + " " + langSelector.getString("replace_file_query"),
                                            langSelector.getString("replace_file_query_title"),
                                            JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                                        return false;
                                    }
                                }

                                // export kv6 engine format
                                boolean success;
                                long time = System.currentTimeMillis();
                                try {
                                    Kv6Exporter exporter = new Kv6Exporter(exportTo, data, progressDialog, console);
                                    exporter.setUseWeightedCenter(dialog.is("kv6_format.use_weighted_center=true"));
                                    success = exporter.writeData();
                                } catch (IOException ignored) {
                                    success = false;
                                }
                                if (success) {
                                    console.addLine(
                                            String.format(langSelector.getString("export_file_successful"),
                                                    System.currentTimeMillis() - time)
                                    );
                                } else {
                                    console.addLine(langSelector.getString("export_file_error"));
                                }

                                return null;
                            }
                        });

                        // ===========
                    } else if (dialog.is("export_type=voxlap_format")) {

                        // ===========
                        // -- handle vox voxlap file format

                        // create progress dialog
                        final ProgressDialog progressDialog = new ProgressDialog(frame);

                        // do the exporting
                        progressDialog.start(new ProgressWorker() {
                            @Override
                            protected Object doInBackground() throws Exception {

                                // extract file name
                                final File exportTo = new File(baseName + (baseName.endsWith(".vox") ? "" : ".vox"));
                                // check if file exists
                                if (exportTo.exists()) {
                                    if (JOptionPane.showConfirmDialog(frame,
                                            exportTo.getPath() + " " + langSelector.getString("replace_file_query"),
                                            langSelector.getString("replace_file_query_title"),
                                            JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                                        return false;
                                    }
                                }

                                // export vox voxlap format
                                boolean success;
                                long time = System.currentTimeMillis();
                                try {
                                    VoxVoxLapExporter exporter = new VoxVoxLapExporter(exportTo, data, progressDialog, console);
                                    success = exporter.writeData();
                                } catch (IOException ignored) {
                                    success = false;
                                }
                                if (success) {
                                    console.addLine(
                                            String.format(langSelector.getString("export_file_successful"),
                                                    System.currentTimeMillis() - time)
                                    );
                                } else {
                                    console.addLine(langSelector.getString("export_file_error"));
                                }

                                return null;
                            }
                        });

                        // ===========
                    } else if (dialog.is("export_type=pnx_format")) {

                        // ===========
                        // -- handle pnx file format

                        // create progress dialog
                        final ProgressDialog progressDialog = new ProgressDialog(frame);

                        // do the exporting
                        progressDialog.start(new ProgressWorker() {
                            @Override
                            protected Object doInBackground() throws Exception {

                                // extract file name
                                final File exportTo = new File(baseName + (baseName.endsWith(".pnx") ? "" : ".pnx"));
                                // check if file exists
                                if (exportTo.exists()) {
                                    if (JOptionPane.showConfirmDialog(frame,
                                            exportTo.getPath() + " " + langSelector.getString("replace_file_query"),
                                            langSelector.getString("replace_file_query_title"),
                                            JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                                        return false;
                                    }
                                }

                                // export pnx file format
                                boolean success;
                                long time = System.currentTimeMillis();
                                try {
                                    PnxExporter exporter = new PnxExporter(exportTo, data, progressDialog, console);
                                    success = exporter.writeData();
                                } catch (IOException ignored) {
                                    success = false;
                                }
                                if (success) {
                                    console.addLine(
                                            String.format(langSelector.getString("export_file_successful"),
                                                    System.currentTimeMillis() - time)
                                    );
                                } else {
                                    console.addLine(langSelector.getString("export_file_error"));
                                }

                                return null;
                            }
                        });

                        // ===========
                    } else if (dialog.is("export_type=qb_format")) {

                        // ===========
                        // -- handle qb file format

                        // create progress dialog
                        final ProgressDialog progressDialog = new ProgressDialog(frame);

                        // do the exporting
                        progressDialog.start(new ProgressWorker() {
                            @Override
                            protected Object doInBackground() throws Exception {

                                // extract file name
                                final File exportTo = new File(baseName + (baseName.endsWith(".qb") ? "" : ".qb"));
                                // check if file exists
                                if (exportTo.exists()) {
                                    if (JOptionPane.showConfirmDialog(frame,
                                            exportTo.getPath() + " " + langSelector.getString("replace_file_query"),
                                            langSelector.getString("replace_file_query_title"),
                                            JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                                        return false;
                                    }
                                }

                                // export qb file format
                                boolean success;
                                long time = System.currentTimeMillis();
                                try {
                                    QbExporter exporter = new QbExporter(exportTo, data, progressDialog, console);
                                    exporter.setUseCompression(dialog.is("qb_format.use_compression=true"));
                                    exporter.setUseBoxAsMatrix(dialog.is("qb_format.use_box_as_matrix=true"));
                                    exporter.setUseOriginAsZero(dialog.is("qb_format.use_origin_as_zero=true"));
                                    exporter.setUseVisMaskEncoding(dialog.is("qb_format.use_vis_mask_encoding=true"));
                                    exporter.setUseRightHandedZAxisOrientation(dialog.is("qb_format.use_right_handed_z_axis_orientation=true"));
                                    success = exporter.writeData();
                                } catch (IOException ignored) {
                                    success = false;
                                }
                                if (success) {
                                    console.addLine(
                                            String.format(langSelector.getString("export_file_successful"),
                                                    System.currentTimeMillis() - time)
                                    );
                                } else {
                                    console.addLine(langSelector.getString("export_file_error"));
                                }

                                return null;
                            }
                        });

                        // ===========
                    }
                    // -----
                    // store serialization
                    preferences.storeObject("export_dialog_serialization", dialog.getSerialization());
                }
                return true;
            }
        });

        // -----------------

        // export file
        actionManager.registerAction("export_file_action", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    // show dialog
                    dialog.setVisible(true);
                }
            }

            @Override
            public boolean getStatus() {
                return data.anyLayerVoxelVisible();
            }
        });
        // =========================

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

        // fill voxels action
        actionManager.registerAction("fill_voxels_action", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                synchronized (VitcoSettings.SYNC) {
                    // compute the hull manager
                    HullManagerExt<String> hullManager = new HullManagerExt<String>();
                    for (Voxel voxel : data.getVisibleLayerVoxel()) {
                        hullManager.update(voxel.posId, null);
                    }
                    hullManager.computeExterior();
                    // fetch the empty interior
                    int[] emptyInterior = hullManager.getEmptyInterior();
                    // create and add the missing voxels
                    Voxel[] voxels = new Voxel[emptyInterior.length];
                    Color color = ColorTools.hsbToColor((float[]) preferences.loadObject("currently_used_color"));
                    for (int i = 0; i < emptyInterior.length; i++) {
                        short[] pos = CubeIndexer.getPos(emptyInterior[i]);
                        voxels[i] = new Voxel(-1, new int[]{pos[0], pos[1], pos[2]}, color, false, null, data.getSelectedLayer());
                    }
                    data.massAddVoxel(voxels);
                }
            }

            @Override
            public boolean getStatus() {
                return true;
            }
        });
        // hollow voxels action
        actionManager.registerAction("hollow_voxels_action", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                synchronized (VitcoSettings.SYNC) {
                    // compute the hull manager
                    HullManagerExt<String> hullManager = new HullManagerExt<String>();
                    for (Voxel voxel : data.getVisibleLayerVoxel()) {
                        hullManager.update(voxel.posId, null);
                    }
                    hullManager.computeExterior();
                    // fetch the filled interior
                    int[] filledInterior = hullManager.getFilledInterior();
                    // search for the interior voxels and remove
                    Integer[] voxelIds = new Integer[filledInterior.length];
                    // todo: This will only remove the top voxel, change it so that it removes all voxels in all layers at this position
                    for (int i = 0; i < filledInterior.length; i++) {
                        short[] pos = CubeIndexer.getPos(filledInterior[i]);
                        Voxel voxel = data.searchVoxel(new int[]{pos[0], pos[1], pos[2]}, false);
                        assert voxel != null;
                        voxelIds[i] = voxel.id;
                    }
                    data.massRemoveVoxel(voxelIds);
                }
            }

            @Override
            public boolean getStatus() {
                return true;
            }
        });

    }

    @PreDestroy
    public final void finish() {
        // store folder locations (for open / close / import / export)
        preferences.storeString("file_open_close_dialog_last_directory", fc_vsd.getDialogPath());
        preferences.storeString("file_import_dialog_last_directory", fc_import.getDialogPath());
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
    }
}
