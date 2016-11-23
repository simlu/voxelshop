package com.vitco.app.layout.bars;

import com.jidesoft.action.CommandMenuBar;
import com.vitco.app.manager.action.ComplexActionManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

/**
 * the tool bar, uses menu generator to load content from file
 *
 * defines interactions
 */
public class SelectBarLinkage extends BarLinkagePrototype {

    // var & setter
    private ComplexActionManager complexActionManager;
    @Autowired
    public final void setComplexActionManager(ComplexActionManager complexActionManager) {
        this.complexActionManager = complexActionManager;
    }

    @Override
    public final CommandMenuBar buildBar(String key, final Frame frame) {
        CommandMenuBar bar = new CommandMenuBar(key);

        // build the toolbar
        menuGenerator.buildMenuFromXML(bar, "com/vitco/app/layout/bars/select_bar.xml");

        // build the rotation sub-menus
        CommandMenuBar settingsBar = new CommandMenuBar(1);
        menuGenerator.buildMenuFromXML(settingsBar, "com/vitco/app/layout/bars/rotatex.xml");
        complexActionManager.registerAction("selection_tool_rotatex_popup", settingsBar);
        settingsBar = new CommandMenuBar(1);
        menuGenerator.buildMenuFromXML(settingsBar, "com/vitco/app/layout/bars/rotatey.xml");
        complexActionManager.registerAction("selection_tool_rotatey_popup", settingsBar);
        settingsBar = new CommandMenuBar(1);
        menuGenerator.buildMenuFromXML(settingsBar, "com/vitco/app/layout/bars/rotatez.xml");
        complexActionManager.registerAction("selection_tool_rotatez_popup", settingsBar);

        // register the logic for this menu
        menuLogic.registerLogic(frame);

        return bar;
    }
}
