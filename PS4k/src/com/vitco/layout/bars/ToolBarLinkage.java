package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;

/**
 * the tool bar, uses menu generator to load content from file
 */
public class ToolBarLinkage extends BarLinkagePrototype {
    @Override
    public CommandBar buildBar(String key) {
        CommandBar bar = new CommandBar(key);

        menuGenerator.buildMenuFromXML(bar, "com/vitco/logic/tool_bar.xml");

        return bar;
    }
}
