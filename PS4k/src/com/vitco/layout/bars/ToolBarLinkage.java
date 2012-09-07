package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;
import com.vitco.engine.data.container.VOXELMODE;
import com.vitco.engine.data.notification.DataChangeAdapter;
import com.vitco.util.action.types.StateActionPrototype;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * the tool bar, uses menu generator to load content from file
 *
 * defines interactions
 */
public class ToolBarLinkage extends BarLinkagePrototype {

    // update all action states
    private void refreshActions(HashMap<String, StateActionPrototype> map) {
        for (StateActionPrototype action : map.values()) {
            action.refresh();
        }
    }

    // register all action states
    private void registerActions(HashMap<String, StateActionPrototype> map) {
        for (Map.Entry<String,StateActionPrototype> entry : map.entrySet()) {
            actionManager.registerAction(entry.getKey(), entry.getValue());
        }
    }

    // the different action categories
    private final HashMap<String, StateActionPrototype> toolActions = new HashMap<String, StateActionPrototype>();
    private final HashMap<String, StateActionPrototype> historyActions = new HashMap<String, StateActionPrototype>();

    // basic tool action to be reused
    private class ToolAction extends StateActionPrototype {
        private final VOXELMODE tool;
        private ToolAction(VOXELMODE tool) {
            super();
            this.tool = tool;
        }

        @Override
        public void action(ActionEvent actionEvent) {
            data.setVoxelMode(tool);
        }

        @Override
        public boolean getStatus() {
            return data.getVoxelMode() == tool;
        }
    }

    @Override
    public CommandBar buildBar(String key, final Frame frame) {
        CommandBar bar = new CommandBar(key);

        menuGenerator.buildMenuFromXML(bar, "com/vitco/layout/bars/tool_bar.xml");

        // register the tool actions
        // =====================================
        toolActions.put("voxel_mode_select_type_view", new ToolAction(VOXELMODE.VIEW));
        toolActions.put("voxel_mode_select_type_draw", new ToolAction(VOXELMODE.DRAW));
        toolActions.put("voxel_mode_select_type_erase", new ToolAction(VOXELMODE.ERASE));
        toolActions.put("voxel_mode_select_type_picker", new ToolAction(VOXELMODE.PICKER));
        toolActions.put("voxel_mode_select_type_color_changer", new ToolAction(VOXELMODE.COLORCHANGER));
        registerActions(toolActions);
        // =====================================

        // register history actions
        // =====================================
        historyActions.put("global_action_undo", new StateActionPrototype() {
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
        historyActions.put("global_action_redo", new StateActionPrototype() {
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
        historyActions.put("clear_history_action", new StateActionPrototype() {
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
        registerActions(historyActions);
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

        // make sure the buttons are correctly enabled when modes change
        data.addDataChangeListener(new DataChangeAdapter() {

            @Override
            public void onAnimationDataChanged() {
                refreshActions(historyActions);
            }

            @Override
            public void onVoxelDataChanged() {
                refreshActions(historyActions);
            }

            @Override
            public void onAnimateChanged() {
                refreshActions(historyActions);
                refreshActions(toolActions);
            }

            @Override
            public void onVoxelModeChanged() {
                refreshActions(toolActions);
            }
        });

        return bar;
    }
}
