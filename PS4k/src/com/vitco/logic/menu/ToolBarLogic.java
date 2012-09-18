package com.vitco.logic.menu;

import com.vitco.engine.data.container.VOXELMODE;
import com.vitco.engine.data.notification.DataChangeAdapter;
import com.vitco.util.action.types.StateActionPrototype;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Handles the toolbar logic.
 */
public class ToolBarLogic extends MenuLogicPrototype implements MenuLogicInterface {

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
                data.setVoxelMode(tool);
            }
        }

        @Override
        public boolean getStatus() {
            return data.getVoxelMode() == tool;
        }

        @Override
        public boolean isVisible() {
            return !data.isAnimate();
        }
    }

    public void registerLogic(Frame frame) {
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
                    if (data.isAnimate()) {
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
                if (data.isAnimate()) {
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
                    if (data.isAnimate()) {
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
                if (data.isAnimate()) {
                    return data.canRedoA();
                } else {
                    return data.canRedoV();
                }
            }
        });
        actionGroupManager.addAction("history_actions", "clear_history_action", new StateActionPrototype() {
            @Override
            public void action(ActionEvent e) {
                if (data.isAnimate()) {
                    data.clearHistoryA();
                } else {
                    data.clearHistoryV();
                }
            }

            @Override
            public boolean getStatus() {
                if (data.isAnimate()) {
                    return data.canUndoA() || data.canRedoA();
                } else {
                    return data.canUndoV() || data.canRedoV();
                }
            }
        });
        actionGroupManager.registerGroup("history_actions");
        // =====================================

        // register the toggle animate mode action (always possible)
        actionManager.registerAction("toggle_animation_mode", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                data.setAnimate(!data.isAnimate());
            }

            @Override
            public boolean getStatus() {
                return data.isAnimate();
            }
        });

        // register voxel snap toggle action
        final StateActionPrototype toggleVoxelSnapAction = new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (isVisible()) { // only if visible
                    voxelSnap = !voxelSnap;
                }
            }

            @Override
            public boolean getStatus() {
                return voxelSnap;
            }

            @Override
            public boolean isVisible() {
                return data.isAnimate();
            }
        };
        actionManager.registerAction("toggle_voxel_snap", toggleVoxelSnapAction);

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

            @Override
            public void onAnimateChanged() {
                actionGroupManager.refreshGroup("history_actions");
                actionGroupManager.refreshGroup("voxel_paint_modes");
                toggleVoxelSnapAction.refresh();
            }

            @Override
            public void onVoxelModeChanged() {
                actionGroupManager.refreshGroup("voxel_paint_modes");
            }
        });
    }

    // status of voxel snap
    private boolean voxelSnap = true;

    @PreDestroy
    public final void savePref() {
        // store "point snap on voxels" setting
        preferences.storeBoolean("voxel_snap_enabled", voxelSnap);
    }

    @PostConstruct
    public final void init() {
        if (preferences.contains("voxel_snap_enabled")) { // load previous settings
            voxelSnap = preferences.loadBoolean("voxel_snap_enabled");
        }
    }

}
