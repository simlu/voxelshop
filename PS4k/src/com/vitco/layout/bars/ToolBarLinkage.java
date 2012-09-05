package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;
import com.vitco.engine.data.Data;
import com.vitco.engine.data.container.DataContainer;
import com.vitco.engine.data.notification.DataChangeAdapter;
import com.vitco.util.action.types.StateActionPrototype;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * the tool bar, uses menu generator to load content from file
 */
public class ToolBarLinkage extends BarLinkagePrototype {

    // var & setter (can not be interface!!)
    protected Data data;
    @Autowired
    public void setData(Data data) {
        this.data = data;
    }

    // update all tool action states
    private void refreshAllActions() {
        viewAction.refresh();
        eraseAction.refresh();
        drawAction.refresh();
        pickerAction.refresh();
    }
    // all tool actions
    final StateActionPrototype viewAction = new StateActionPrototype() {
        @Override
        public void action(ActionEvent actionEvent) {
            data.setVoxelMode(DataContainer.VOXELMODE.VIEW);
            refreshAllActions();
        }

        @Override
        public boolean getStatus() {
            return data.getVoxelMode() == DataContainer.VOXELMODE.VIEW;
        }
    };
    final StateActionPrototype eraseAction = new StateActionPrototype() {
        @Override
        public void action(ActionEvent actionEvent) {
            data.setVoxelMode(DataContainer.VOXELMODE.ERASE);
            refreshAllActions();
        }

        @Override
        public boolean getStatus() {
            return data.getVoxelMode() == DataContainer.VOXELMODE.ERASE;
        }
    };
    final StateActionPrototype drawAction = new StateActionPrototype() {
        @Override
        public void action(ActionEvent actionEvent) {
            data.setVoxelMode(DataContainer.VOXELMODE.DRAW);
            refreshAllActions();
        }

        @Override
        public boolean getStatus() {
            return data.getVoxelMode() == DataContainer.VOXELMODE.DRAW;
        }
    };
    final StateActionPrototype pickerAction = new StateActionPrototype() {
        @Override
        public void action(ActionEvent actionEvent) {
            data.setVoxelMode(DataContainer.VOXELMODE.PICKER);
            refreshAllActions();
        }

        @Override
        public boolean getStatus() {
            return data.getVoxelMode() == DataContainer.VOXELMODE.PICKER;
        }
    };

    @Override
    public CommandBar buildBar(String key, final Frame frame) {
        CommandBar bar = new CommandBar(key);

        menuGenerator.buildMenuFromXML(bar, "com/vitco/layout/bars/tool_bar.xml");

        // register the toggle animation mode action
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

        // register global shortcuts for data interaction
        // register undo action
        actionManager.registerAction("global_action_undo", new StateActionPrototype() {
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

        // register redo action
        actionManager.registerAction("global_action_redo", new StateActionPrototype() {
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

        // register clear history action
        actionManager.registerAction("clear_history_action", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                data.clearHistory();
            }
        });

        // make sure the history buttons are correctly enabled when mode changes
        data.addDataChangeListener(new DataChangeAdapter() {
            private void refreshHistoryButtons() {
                actionManager.performWhenActionIsReady("global_action_undo", new Runnable() {
                    @Override
                    public void run() {
                        ((StateActionPrototype)actionManager.getAction("global_action_undo")).refresh();
                    }
                });
                actionManager.performWhenActionIsReady("global_action_redo", new Runnable() {
                    @Override
                    public void run() {
                        ((StateActionPrototype)actionManager.getAction("global_action_redo")).refresh();
                    }
                });
            }

            @Override
            public void onAnimationDataChanged() {
                refreshHistoryButtons();
            }

            @Override
            public void onVoxelDataChanged() {
                refreshHistoryButtons();
            }

            @Override
            public void onAnimateChanged() {
                refreshHistoryButtons();
            }
        });

        // register the draw mode actions
        actionManager.registerAction("voxel_mode_select_type_view", viewAction);
        actionManager.registerAction("voxel_mode_select_type_draw", drawAction);
        actionManager.registerAction("voxel_mode_select_type_erase", eraseAction);
        actionManager.registerAction("voxel_mode_select_type_picker", pickerAction);

        return bar;
    }
}
