package com.vitco.layout.content.texture;

import com.jidesoft.action.CommandMenuBar;
import com.jidesoft.swing.JideScrollPane;
import com.vitco.core.data.Data;
import com.vitco.core.data.notification.DataChangeAdapter;
import com.vitco.core.world.WorldManager;
import com.vitco.layout.content.ViewPrototype;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.manager.async.AsyncAction;
import com.vitco.manager.async.AsyncActionManager;
import com.vitco.manager.pref.PrefChangeListener;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.misc.CFileDialog;
import com.vitco.util.misc.SwingAsyncHelper;
import com.vitco.util.misc.ThumbnailFileChooser;
import com.vitco.util.misc.WrapLayout;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Manages the different Textures (in the window where the user adds them!).
 */
public class TextureManager extends ViewPrototype implements TextureManagerInterface {

    protected AsyncActionManager asyncActionManager;

    @Autowired
    public final void setAsyncActionManager(AsyncActionManager asyncActionManager) {
        this.asyncActionManager = asyncActionManager;
    }

    // var & setter
    protected Data data;
    @Autowired
    public final void setData(Data data) {
        this.data = data;
    }

    // true if there exist textured
    private boolean texturesExist = false;

    // currently selected texture
    private int selectedTexture = -1;

    // texture panel class
    private final class TexturePanel extends JPanel {

        private final Integer textureId;

        // we need the hash to determine when to change the picture
        private String hash = "";

        private final Color inactiveColor = VitcoSettings.TEXTURE_BORDER;
        private final Color activeColor = VitcoSettings.TEXTURE_BORDER_ACTIVE;
        private final Color selectedColor = VitcoSettings.TEXTURE_BORDER_SELECTED;

        private boolean selected = false;

        // reference to this instance
        private final TexturePanel thisTexturePanel = this;

        private TexturePanel(final Integer textureId) {
            this.textureId = textureId;
            this.setBorder(BorderFactory.createLineBorder(inactiveColor));
            this.setLayout(new BorderLayout(0, 0));
            //this.setToolTipText("Texture #" + textureId);
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(final MouseEvent e) {
                    super.mousePressed(e);
                    asyncActionManager.addAsyncAction(new AsyncAction() {
                        @Override
                        public void performAction() {
                            // unselect if this is already selected
                            data.selectTextureSoft(selected ? -1 : textureId);
                        }
                    });
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    super.mouseEntered(e);
                    setBorder(BorderFactory.createLineBorder(activeColor));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    super.mouseExited(e);
                    setBorder(BorderFactory.createLineBorder(selected?selectedColor:inactiveColor));
                }
            });
            refresh();
        }

        private void refresh() {
            String newHash = data.getTextureHash(textureId);
            if (!hash.equals(newHash)) { // only redraw when hash changed
                removeAll(); // make sure nothing is in this container
                ImageIcon myPicture = data.getTexture(textureId);
                JLabel picLabel = new JLabel(myPicture);
                add( picLabel );
                hash = newHash;
            }
            boolean selectedNew = textureId == selectedTexture;
            if (selectedNew != selected) {
                selected = selectedNew;
                this.setBorder(BorderFactory.createLineBorder(selected?selectedColor:inactiveColor));
            }
            SwingAsyncHelper.handle(new Runnable() {
                @Override
                public void run() {
                    thisTexturePanel.revalidate();
                    thisTexturePanel.repaint();
                }
            }, errorHandler);
        }
    }

    // import texture file chooser
    final ThumbnailFileChooser fc_import = new ThumbnailFileChooser(32, 32);

    // export texture file chooser
    final CFileDialog fc_export = new CFileDialog();

    // handles the textures of the data class object
    @Override
    public JComponent build(final Frame mainFrame) {
        // panel that holds everything
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        final JPanel textureWrapperPanel = new JPanel();
        textureWrapperPanel.setLayout(new WrapLayout(FlowLayout.CENTER, 3, 3));
        textureWrapperPanel.setBackground(VitcoSettings.FRAME_BG_COLOR);
        final JideScrollPane scrollPane = new JideScrollPane(textureWrapperPanel);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, VitcoSettings.DEFAULT_BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);

        // add filter
        fc_import.addFileType("png");
        fc_export.addFileType("png");

        // create the menu actions
        actionManager.registerAction("texturemg_action_add", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File toOpen = fc_import.openFile(mainFrame);
                if (toOpen != null) {
                    try {
                        data.addTexture(ImageIO.read(toOpen));
                    } catch (IOException error) {
                        console.addLine(langSelector.getString("texturemg_general_file_error"));
                    }
                }
            }
        });
        actionGroupManager.addAction("texture_manager_buttons", "texturemg_action_remove", new StateActionPrototype() {
            @Override
            public boolean getStatus() {
                return selectedTexture != -1;
            }

            @Override
            public void action(ActionEvent e) {
                if (getStatus()) {
                    boolean success = data.removeTexture(selectedTexture);
                    if (!success) {
                        console.addLine(langSelector.getString("texturemg_delete_failed_texture_in_use"));
                    }
                }
            }
        });
        actionGroupManager.addAction("texture_manager_buttons", "texturemg_action_replace", new StateActionPrototype() {
            @Override
            public boolean getStatus() {
                return selectedTexture != -1;
            }

            @Override
            public void action(ActionEvent e) {
                if (getStatus()) {
                    File toOpen = fc_import.openFile(mainFrame);
                    if (toOpen != null) {
                        try {
                            ImageIcon texture = new ImageIcon(ImageIO.read(toOpen));
                            // make sure we can identify the texture
                            ImageIcon textureDrawnOnTop = data.getTexture(selectedTexture);
                            textureDrawnOnTop.getImage().getGraphics().drawImage(texture.getImage(), 0, 0, null);
                            data.replaceTexture(selectedTexture, textureDrawnOnTop);
                        } catch (IOException error) {
                            console.addLine(langSelector.getString("texturemg_general_file_error"));
                        }
                    }
                }
            }
        });
        actionGroupManager.addAction("texture_manager_buttons", "texturemg_action_export", new StateActionPrototype() {
            @Override
            public boolean getStatus() {
                return selectedTexture != -1;
            }

            @Override
            public void action(ActionEvent e) {
                if (getStatus()) {
                    File exportTo = fc_export.saveFile(mainFrame);
                    if (exportTo != null) {
                        String dir = exportTo.getPath();

                        // query if file already exists
                        if (!exportTo.exists() ||
                                JOptionPane.showConfirmDialog(mainFrame,
                                        dir + " " + langSelector.getString("replace_file_query"),
                                        langSelector.getString("replace_file_query_title"),
                                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

                            ImageIcon texture = data.getTexture(selectedTexture);
                            BufferedImage img = new BufferedImage(texture.getIconWidth(),
                                    texture.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                            img.getGraphics().drawImage(texture.getImage(), 0, 0, null);
                            try {
                                ImageIO.write(img, "png", exportTo);
                                console.addLine(langSelector.getString("texturemg_export_success"));
                            } catch (IOException e1) {
                                console.addLine(langSelector.getString("texturemg_export_failed"));
                            }
                        }
                    }
                }
            }
        });
        actionGroupManager.addAction("texture_manager_buttons", "texturemg_action_clear", new StateActionPrototype() {
            @Override
            public boolean getStatus() {
                return texturesExist;
            }

            @Override
            public void action(ActionEvent e) {
                if (getStatus()) {
                    data.removeAllTexture();
                }
            }
        });
        actionGroupManager.registerGroup("texture_manager_buttons");

        // handle texture change notification (logic + layout)
        data.addDataChangeListener(new DataChangeAdapter() {
            // list of texture images currently on display
            private final HashMap<Integer, TexturePanel> texturePanels = new HashMap<Integer, TexturePanel>();
            // list of textures with md5 hash
            private final HashMap<Integer, String> textureHash = new HashMap<Integer, String>();

            @Override
            public void onTextureDataChanged() {
                ArrayList<Integer> dataTextureList =
                        new ArrayList<Integer>(Arrays.asList(data.getTextureList()));

                // previously selected texture
                int prevSelectedTexture = selectedTexture;

                // remember if textures exist
                texturesExist = dataTextureList.size() > 0;
                // get the currently selected texture
                selectedTexture = data.getSelectedTexture();
                // update buttons
                actionGroupManager.refreshGroup("texture_manager_buttons");

                // removed textures
                for (Integer texId : new ArrayList<Integer>(textureHash.keySet())) {
                    if (!dataTextureList.contains(texId)) {
                        // delete from internal store
                        textureHash.remove(texId);
                        // delete texture from world
                        boolean removed = WorldManager.removeTile(String.valueOf(texId));
                        assert removed;
                        // remove panel
                        textureWrapperPanel.remove(texturePanels.get(texId));
                        texturePanels.remove(texId);
                    }
                }
                // added textures
                for (int texId : dataTextureList) {
                    boolean changed = false;
                    if (textureHash.containsKey(texId)) {
                        if (!data.getTextureHash(texId).equals(textureHash.get(texId))) {
                            // hash changed
                            changed = true;
                            // update panel
                            texturePanels.get(texId).refresh();
                        }
                    } else {
                        // new texture
                        changed = true;
                        // ===================
                        // create panel
                        TexturePanel panel = new TexturePanel(texId);
                        texturePanels.put(texId, panel);

                        // insert to correct place
                        Integer[] currentObjects = new Integer[texturePanels.size()];
                        texturePanels.keySet().toArray(currentObjects);
                        // sort
                        Arrays.sort(currentObjects, new Comparator<Integer>() {
                            @Override
                            public int compare(Integer o1, Integer o2) {
                                return o1.compareTo(o2);
                            }
                        });
                        int pos = 0;
                        while (texId > currentObjects[pos] && pos < currentObjects.length) {
                            pos++;
                        }
                        textureWrapperPanel.add(panel, null, pos);
                        // ===================
                    }
                    if (changed) {
                        // store/update internal hash
                        textureHash.put(texId, data.getTextureHash(texId));
                        // load/update texture into world
                        WorldManager.loadTile(String.valueOf(texId), data.getTexture(texId));
                    }
                }

                // update the UI (force!)
                SwingAsyncHelper.handle(new Runnable() {
                    @Override
                    public void run() {
                        textureWrapperPanel.validate();
                        textureWrapperPanel.repaint();
                    }
                }, errorHandler);


                // this updates the values for the getBound() function
                scrollPane.validate();

                // scroll the selected texture into view and refresh panel
                TexturePanel selectedTexturePanel = texturePanels.get(selectedTexture);
                if (selectedTexturePanel != null) {
                    textureWrapperPanel.scrollRectToVisible(selectedTexturePanel.getBounds());
                    selectedTexturePanel.refresh();
                }

                // refresh prev selected panels
                TexturePanel prevSelectedTexturePanel = texturePanels.get(prevSelectedTexture);
                if (prevSelectedTexturePanel != null) {
                    prevSelectedTexturePanel.refresh();
                }
            }
        });

        // deselect any texture when the color changes
        preferences.addPrefChangeListener("currently_used_color", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object newValue) {
                data.selectTexture(-1);
            }
        });

        // create menu bar to bottom
        CommandMenuBar menuPanel = new CommandMenuBar();
        //menuPanel.setOrientation(1); // top down orientation
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/layout/content/texture/toolbar.xml");
        panel.add(menuPanel, BorderLayout.SOUTH);

        return panel;
    }

    @PreDestroy
    public final void finish() {
        preferences.storeString("texture_import_dialog_last_directory", fc_import.getDialogPath());
        preferences.storeString("texture_export_dialog_last_directory", fc_export.getDialogPath());
    }

    @PostConstruct
    public final void init() {
        if (preferences.contains("texture_import_dialog_last_directory")) {
            File file = new File(preferences.loadString("texture_import_dialog_last_directory"));
            fc_import.setDialogPath(file);
        }
        if (preferences.contains("texture_export_dialog_last_directory")) {
            File file = new File(preferences.loadString("texture_export_dialog_last_directory"));
            fc_export.setDialogPath(file);
        }
    }
}
