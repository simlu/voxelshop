package com.vitco.layout.bars;

import com.jidesoft.action.CommandMenuBar;
import com.vitco.util.action.ComplexActionManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

/**
 * the tool bar, uses menu generator to load content from file
 *
 * defines interactions (from xml)
 */
public class OptionBarLinkage extends BarLinkagePrototype {
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
        menuGenerator.buildMenuFromXML(bar, "com/vitco/layout/bars/option_bar.xml");

        // build the settings sub-menu
        CommandMenuBar settingsBar = new CommandMenuBar();
        menuGenerator.buildMenuFromXML(settingsBar, "com/vitco/layout/bars/settings.xml");
        complexActionManager.registerAction("settings_menubar", settingsBar);

        // register the logic for this menu
        menuLogic.registerLogic(frame);

        return bar;
    }
}
