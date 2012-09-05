package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;
import com.vitco.engine.data.Data;
import com.vitco.engine.data.container.DataContainer;
import com.vitco.util.action.types.StateActionPrototype;
import org.springframework.beans.factory.annotation.Autowired;

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
    StateActionPrototype viewAction = new StateActionPrototype() {
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
    StateActionPrototype eraseAction = new StateActionPrototype() {
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
    StateActionPrototype drawAction = new StateActionPrototype() {
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
    StateActionPrototype pickerAction = new StateActionPrototype() {
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
    public CommandBar buildBar(String key) {
        CommandBar bar = new CommandBar(key);

        menuGenerator.buildMenuFromXML(bar, "com/vitco/layout/bars/tool_bar.xml");

        // register the toggle animation mode action
        actionManager.registerAction("toggle_animation_mode", new StateActionPrototype() {
            // default mode is not animationMode
            private boolean animationMode = false;

            @Override
            public void action(ActionEvent actionEvent) {
                animationMode = !animationMode;
            }

            @Override
            public boolean getStatus() {
                return animationMode;
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
