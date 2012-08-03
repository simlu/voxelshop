package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;
import com.jidesoft.action.CommandMenuBar;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 12:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainMenuLinkage extends BarLinkagePrototype {
    @Override
    public CommandBar buildBar(String key) {
        CommandMenuBar bar = new CommandMenuBar(key);

        menuGenerator.buildMenuFromXML(bar, "com/vitco/logic/main_menu.xml");

        return bar;
    }
}
