package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;
import com.vitco.util.action.types.StateActionPrototype;

import java.awt.event.ActionEvent;

/**
 * the tool bar, uses menu generator to load content from file
 */
public class ToolBarLinkage extends BarLinkagePrototype {
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

        return bar;
    }
}
