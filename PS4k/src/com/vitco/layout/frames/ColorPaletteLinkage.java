package com.vitco.layout.frames;

import com.jidesoft.action.CommandMenuBar;
import com.jidesoft.swing.JideToggleButton;
import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.layout.content.colorchooser.ColorPaletteChooser;
import com.vitco.layout.content.colorchooser.basic.ColorChangeListener;
import com.vitco.layout.content.console.ConsoleInterface;
import com.vitco.layout.frames.custom.CDockableFrame;
import com.vitco.manager.action.ComplexActionManager;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.manager.error.ErrorHandlerInterface;
import com.vitco.manager.menu.MenuGeneratorInterface;
import com.vitco.manager.pref.PrefChangeListener;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.file.FileTools;
import com.vitco.util.misc.CFileDialog;
import com.vitco.util.misc.SaveResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * construct the color palette frame
 */
public class ColorPaletteLinkage extends FrameLinkagePrototype {

    // the dialog that is shown for import and export
    private final CFileDialog dialog = new CFileDialog();

    // register palette color picker button
    private final ColorPaletteChooser colorPaletteChooser = new ColorPaletteChooser();

    // var & setter
    private ComplexActionManager complexActionManager;
    @Autowired
    public final void setComplexActionManager(ComplexActionManager complexActionManager) {
        this.complexActionManager = complexActionManager;
    }

    // var & setter
    protected Data data;
    @Autowired
    public final void setData(Data data) {
        this.data = data;
    }

    // var & setter
    protected MenuGeneratorInterface menuGenerator;
    @Autowired(required=true)
    public final void setMenuGenerator(MenuGeneratorInterface menuGenerator) {
        this.menuGenerator = menuGenerator;
    }

    // var & setter
    private ErrorHandlerInterface errorHandler;
    @Autowired(required=true)
    public final void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    // var & setter
    protected ConsoleInterface console;
    @Autowired
    public final void setConsole(ConsoleInterface console) {
        this.console = console;
    }

    @Override
    public CDockableFrame buildFrame(String key, final Frame mainFrame) {
        // construct frame
        frame = new CDockableFrame(key,
                new SaveResourceLoader("resource/img/icons/frames/colorpalette.png").asIconImage(),
                langSelector
        );
        updateTitle(); // update the title

        // load the colors
        if (preferences.contains("color-palette_set-colors")) {
            colorPaletteChooser.loadColors(
                    FileTools.castHash((HashMap) preferences.loadObject("color-palette_set-colors"), Point.class, Color.class)
            );
        } else {
            colorPaletteChooser.loadDefaultColors();
        }

        // add listener to this color palette (when the internal color changes)
        colorPaletteChooser.addColorChangeListener(new ColorChangeListener() {
            @Override
            public void colorChanged(float[] hsb) {
                preferences.storeObject("currently_used_color", hsb);
            }
        });

        // refresh the internal color when the current (global) color changes
        preferences.addPrefChangeListener("currently_used_color", new PrefChangeListener() {
            @Override
            public void onPrefChange(final Object o) {
                colorPaletteChooser.setColor((float[])o);
            }
        });

        // set border
        colorPaletteChooser.setBorder(BorderFactory.createMatteBorder(1,1,0,1, VitcoSettings.DEFAULT_BORDER_COLOR));

        frame.add(colorPaletteChooser, BorderLayout.CENTER);

        // --------------
        // register that this action is used (needed b/c this is inner action)
        complexActionManager.registerActionIsUsed("color-palette_lock_current-icon");
        // create locking logic
        actionManager.registerAction("color-palette_lock", new StateActionPrototype() {
            private boolean locked = true; // always false on startup
            @Override
            public void action(ActionEvent actionEvent) {
                locked = !locked;
                colorPaletteChooser.setLocked(locked);

                // show the correct icon
                complexActionManager.performWhenActionIsReady("color-palette_lock_current-icon", new Runnable() {
                    @Override
                    public void run() {
                        // create and set the icon
                        ImageIcon icon;
                        if (locked) {
                            icon = new SaveResourceLoader("resource/img/framebars/colorpalette/locked.png").asIconImage();
                        } else {
                            icon = new SaveResourceLoader("resource/img/framebars/colorpalette/unlocked.png").asIconImage();
                        }
                        ((JideToggleButton) complexActionManager.getAction("color-palette_lock_current-icon")).setIcon(icon);
                    }
                });

            }

            @Override
            public boolean getStatus() {
                return locked;
            }
        });
        // --------------

        // create shortcut action
        actionManager.registerAction("color-palette_color_left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                colorPaletteChooser.left();
            }
        });
        actionManager.registerAction("color-palette_color_right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                colorPaletteChooser.right();
            }
        });
        actionManager.registerAction("color-palette_color_up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                colorPaletteChooser.up();
            }
        });
        actionManager.registerAction("color-palette_color_down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                colorPaletteChooser.down();
            }
        });

        // ------------------

        // create import logic
        actionManager.registerAction("color-palette_import", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setTitle(langSelector.getString("color-palette_import-file_title"));
                File file = dialog.openFile(mainFrame);
                if (file != null) {
                    try {
                        BufferedImage img = ImageIO.read(file);
                        HashMap<Point, Color> colors = new HashMap<Point, Color>();
                        for (int x = 0, lenX = Math.min(img.getWidth(), 50); x < lenX; x++) {
                            for (int y = 0, lenY = Math.min(img.getHeight(), 50); y < lenY; y++) {
                                Color col = new Color(img.getRGB(x,y), true);
                                if (col.getAlpha() == 255) {
                                    colors.put(new Point(x,y), col);
                                }
                            }
                        }
                        colorPaletteChooser.loadColors(colors);
                        console.addLine("Color Palette imported successfully.");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        // create export logic
        actionManager.registerAction("color-palette_export", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setTitle(langSelector.getString("color-palette_export-file_title"));
                File file = dialog.saveFile(mainFrame);
                if (file != null) {
                    String dir = file.getPath();
                    if (!file.exists() ||
                            JOptionPane.showConfirmDialog(mainFrame,
                                    dir + " " + langSelector.getString("replace_file_query"),
                                    langSelector.getString("replace_file_query_title"),
                                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        HashMap<Point, Color> colors = colorPaletteChooser.getColors();
                        int xMax = 0, yMax = 0;
                        for (Point p : colors.keySet()) {
                            xMax = Math.max(xMax, p.x + 1);
                            yMax = Math.max(yMax, p.y + 1);
                        }
                        if (xMax > 0 && yMax > 0) {
                            BufferedImage img = new BufferedImage(xMax, yMax, BufferedImage.TYPE_INT_ARGB);
                            for (Map.Entry<Point, Color> entry : colors.entrySet()) {
                                img.setRGB(entry.getKey().x, entry.getKey().y, entry.getValue().getRGB());
                            }
                            try {
                                if (ImageIO.write(img, "png", file)) {
                                    console.addLine("Color Palette exported successfully.");
                                }
                            } catch (IOException e1) {
                                errorHandler.handle(e1);
                            }
                        }
                    }
                }
            }
        });

        // extract colors from selected voxels
        actionManager.registerAction("color-palette_extract-colors", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HashSet<Integer> colors = new HashSet<Integer>();
                for (Voxel voxel : data.getSelectedVoxels()) {
                    colors.add(voxel.getColor().getRGB());
                }
                for (Color color : colorPaletteChooser.getColors().values()) {
                    colors.remove(color.getRGB());
                }
                colorPaletteChooser.addColors(colors);
            }
        });

        // erase colors
        actionManager.registerAction("color-palette_erase-colors", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                colorPaletteChooser.loadColors(new HashMap<Point, Color>());
            }
        });

        // rearrange color palette
        actionManager.registerAction("color-palette_reorder-colors", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                colorPaletteChooser.reorderColors();
            }
        });

        // create menu
        CommandMenuBar menuPanel = new CommandMenuBar();
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/layout/frames/toolbars/colorpalette.xml");
        // so the background doesn't show
        menuPanel.setOpaque(true);
        frame.add(menuPanel, BorderLayout.SOUTH);

        // register action to hide/show this frame and get visible state
        actionManager.registerAction("colorPalette_state-action_show", new StateActionPrototype() {
            @Override
            public boolean getStatus() {
                return frame.isVisible();
            }

            @Override
            public void action(ActionEvent e) {
                toggleVisible();
            }
        });

        return frame;
    }

    @PostConstruct
    public final void init() {
        // load folder locations
        if (preferences.contains("color_palette_imp_exp_dialog_last_directory")) {
            File file = new File(preferences.loadString("color_palette_imp_exp_dialog_last_directory"));
            dialog.setDialogPath(file);
        }

        // set the filter for the imp/exp dialog
        dialog.addFileType("png");
    }

    @PreDestroy
    public final void savePref() {
        // store palette colors
        preferences.storeObject("color-palette_set-colors", colorPaletteChooser.getColors());
        // save folder locations
        preferences.storeString("color_palette_imp_exp_dialog_last_directory", dialog.getDialogPath());
    }
}
