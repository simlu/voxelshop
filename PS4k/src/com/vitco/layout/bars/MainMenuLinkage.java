package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;
import com.jidesoft.action.CommandMenuBar;

/**
 * the main menu, uses menu generator to load content from file
 */
public class MainMenuLinkage extends BarLinkagePrototype {
    @Override
    public CommandBar buildBar(String key) {
        CommandMenuBar bar = new CommandMenuBar(key);

        menuGenerator.buildMenuFromXML(bar, "com/vitco/logic/main_menu.xml");

        return bar;
    }
}
