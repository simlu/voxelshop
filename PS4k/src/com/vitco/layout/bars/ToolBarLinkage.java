package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 12:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToolBarLinkage extends BarLinkagePrototype {
    @Override
    public CommandBar buildBar(String key) {
        CommandBar bar = new CommandBar(key);

        menuGenerator.buildMenuFromXML(bar, "com/vitco/logic/tool_bar.xml");

        return bar;
    }
}
