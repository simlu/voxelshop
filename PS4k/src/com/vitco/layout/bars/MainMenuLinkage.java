package com.vitco.layout.bars;

import com.jidesoft.action.CommandMenuBar;

import java.awt.*;

/**
 * the main menu, uses menu generator to load content from file
 *
 * defines interactions
 */
public class MainMenuLinkage extends BarLinkagePrototype {

    @Override
    public CommandMenuBar buildBar(String key, final Frame frame) {
        final CommandMenuBar bar = new CommandMenuBar(key);

        // build the menu
        menuGenerator.buildMenuFromXML(bar, "com/vitco/layout/bars/main_menu.xml");

        // register the logic for this menu
        menuLogic.registerLogic(frame);

        return bar;
    }



}
