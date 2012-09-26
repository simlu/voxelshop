package com.vitco.logic.menu;

import com.jidesoft.swing.JideSplitButton;
import com.vitco.engine.data.container.VOXELMODE;
import com.vitco.engine.data.notification.DataChangeAdapter;
import com.vitco.res.VitcoSettings;
import com.vitco.util.ColorTools;
import com.vitco.util.action.ComplexActionManager;
import com.vitco.util.action.types.StateActionPrototype;
import com.vitco.util.colors.ColorChangeListener;
import com.vitco.util.colors.SimpleColorChooser;
import com.vitco.util.pref.PrefChangeListener;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

/**
 * Handles the toolbar logic.
 */
public class ToolBarLogic extends MenuLogicPrototype implements MenuLogicInterface {

    // var & setter
    private ComplexActionManager complexActionManager;
    @Autowired
    public final void setComplexActionManager(ComplexActionManager complexActionManager) {
        this.complexActionManager = complexActionManager;
    }

    private boolean isAnimate = VitcoSettings.INITIAL_MODE_IS_ANIMATION;
    private VOXELMODE voxelmode = VitcoSettings.INITIAL_VOXEL_MODE;

    // status of voxel snap
    private boolean voxelSnap = true;

    // basic tool action to be reused
    private class ToolAction extends StateActionPrototype {
        private final VOXELMODE tool;
        private ToolAction(VOXELMODE tool) {
            super();
            this.tool = tool;
        }

        @Override
        public void action(ActionEvent actionEvent) {
            if (isVisible()) { // only if visible
                preferences.storeObject("active_voxel_submode", tool);
            }
        }

        @Override
        public boolean getStatus() {
            return voxelmode == tool;
        }

        @Override
        public boolean isVisible() {
            return !isAnimate;
        }
    }

    public void registerLogic(Frame frame) {
        // register the color preview "button" and picker
        // =====================================
        // register the popup and action
        SimpleColorChooser scc = new SimpleColorChooser();
        scc.addColorChangeListener(new ColorChangeListener() {
            @Override
            public void colorChanged(Color color) {
                preferences.storeObject("currently_used_color", ColorTools.colorToHSB(color));
            }
        });
        complexActionManager.registerAction("current_color_button_popup", scc);
        // to perform validity check we need to register this name
        complexActionManager.registerActionIsUsed("current_color_button_icon");
        // lazy action linking (the action might not be ready!)
        preferences.addPrefChangeListener("currently_used_color", new PrefChangeListener() {
            @Override
            public void onPrefChange(final Object o) {
                complexActionManager.performWhenActionIsReady("current_color_button_icon", new Runnable() {
                    @Override
                    public void run() {
                        // create the image that is used as icon
                        BufferedImage image = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D ig = (Graphics2D)image.getGraphics();
                        ig.setColor(ColorTools.hsbToColor((float[])o));
                        ig.fillRect(1,1,22,22);
                        ig.setColor(Color.BLACK);
                        ig.drawRect(0,0,23,23);
                        // create and set the icon
                        ImageIcon icon = new ImageIcon();
                        icon.setImage(image);
                        ((JideSplitButton) complexActionManager.getAction("current_color_button_icon")).setIcon(icon);
                    }
                });
            }
        });
        // =====================================

        // register the tool actions
        // =====================================
        actionGroupManager.addAction("voxel_paint_modes", "voxel_mode_select_type_view", new ToolAction(VOXELMODE.VIEW));
        actionGroupManager.addAction("voxel_paint_modes", "voxel_mode_select_type_draw", new ToolAction(VOXELMODE.DRAW));
        actionGroupManager.addAction("voxel_paint_modes", "voxel_mode_select_type_erase", new ToolAction(VOXELMODE.ERASE));
        actionGroupManager.addAction("voxel_paint_modes", "voxel_mode_select_type_picker", new ToolAction(VOXELMODE.PICKER));
        actionGroupManager.addAction("voxel_paint_modes", "voxel_mode_select_type_color_changer", new ToolAction(VOXELMODE.COLORCHANGER));
        actionGroupManager.registerGroup("voxel_paint_modes");
        // =====================================

        // register history actions
        // =====================================
        actionGroupManager.addAction("history_actions", "global_action_undo", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    if (isAnimate) {
                        data.removeAnimationHighlights();
                        data.undoA();
                    } else {
                        data.removeVoxelHighlights();
                        data.undoV();
                    }
                }
            }

            @Override
            public boolean getStatus() {
                if (isAnimate) {
                    return data.canUndoA();
                } else {
                    return data.canUndoV();
                }
            }
        });
        actionGroupManager.addAction("history_actions", "global_action_redo", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    if (isAnimate) {
                        data.removeAnimationHighlights();
                        data.redoA();
                    } else {
                        data.removeVoxelHighlights();
                        data.redoV();
                    }
                }
            }

            @Override
            public boolean getStatus() {
                if (isAnimate) {
                    return data.canRedoA();
                } else {
                    return data.canRedoV();
                }
            }
        });
        actionGroupManager.addAction("history_actions", "clear_history_action", new StateActionPrototype() {
            @Override
            public void action(ActionEvent e) {
                if (isAnimate) {
                    data.clearHistoryA();
                } else {
                    data.clearHistoryV();
                }
            }

            @Override
            public boolean getStatus() {
                if (isAnimate) {
                    return data.canUndoA() || data.canRedoA();
                } else {
                    return data.canUndoV() || data.canRedoV();
                }
            }
        });
        actionGroupManager.registerGroup("history_actions");
        // =====================================

        // register voxel snap toggle action
        actionGroupManager.addAction("animation_paint_modes", "toggle_voxel_snap", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (isVisible()) { // only if visible
                    preferences.storeObject("voxel_snap_enabled", !voxelSnap);
                }
            }

            @Override
            public boolean getStatus() {
                return voxelSnap;
            }

            @Override
            public boolean isVisible() {
                return isAnimate;
            }
        });
        actionGroupManager.registerGroup("animation_paint_modes");
        // =====================================

        // register the toggle animate mode action (always possible)
        actionManager.registerAction("toggle_animation_mode", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                preferences.storeObject("is_animation_mode_active", !isAnimate);
            }

            @Override
            public boolean getStatus() {
                return isAnimate;
            }
        });

        // make sure the buttons are correctly enabled when modes change
        data.addDataChangeListener(new DataChangeAdapter() {
            @Override
            public void onAnimationDataChanged() {
                actionGroupManager.refreshGroup("history_actions");
            }

            @Override
            public void onVoxelDataChanged() {
                actionGroupManager.refreshGroup("history_actions");
            }
        });
    }

    @PostConstruct
    public final void init() {
        // register change of voxel snap
        preferences.addPrefChangeListener("voxel_snap_enabled", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object newValue) {
                voxelSnap = (Boolean) newValue;
            }
        });

        // register change of animation mode
        preferences.addPrefChangeListener("is_animation_mode_active", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object newValue) {
                isAnimate = (Boolean) newValue;
                actionGroupManager.refreshGroup("history_actions");
                actionGroupManager.refreshGroup("voxel_paint_modes");
                actionGroupManager.refreshGroup("animation_paint_modes");
            }
        });

        // register change of voxel mode
        preferences.addPrefChangeListener("active_voxel_submode", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object newValue) {
                voxelmode = (VOXELMODE) newValue;
                actionGroupManager.refreshGroup("voxel_paint_modes");
            }
        });

        // create the complex action to select the color of the background plane
        SimpleColorChooser bgPlaneColorChooser = new SimpleColorChooser();
        bgPlaneColorChooser.addColorChangeListener(new ColorChangeListener() {
            @Override
            public void colorChanged(Color newColor) {
                preferences.storeObject("main_view_ground_plane_color", newColor);

            }
        });
        complexActionManager.registerAction("pick_color_voxel_ground_plane", bgPlaneColorChooser);

        // create the complex action to select the color of the background
        SimpleColorChooser bgColorChooser = new SimpleColorChooser();
        if (!preferences.contains("engine_view_bg_color")) {
            preferences.storeObject("engine_view_bg_color", VitcoSettings.ANIMATION_BG_COLOR);
        }
        bgColorChooser.addColorChangeListener(new ColorChangeListener() {
            @Override
            public void colorChanged(Color newColor) {
                preferences.storeObject("engine_view_bg_color", newColor);
            }
        });
        complexActionManager.registerAction("pick_color_voxel_bg", bgColorChooser);

    }

}
