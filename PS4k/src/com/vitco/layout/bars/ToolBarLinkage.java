package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;
import com.vitco.util.lang.LangSelectorInterface;
import com.vitco.util.menu.MenuGeneratorInterface;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 12:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToolBarLinkage implements BarLinkageInterface {
    private MenuGeneratorInterface menuGenerator;

    @Override
    public void setMenuGenerator(MenuGeneratorInterface menuGenerator) {
        this.menuGenerator = menuGenerator;
    }

    @Override
    public CommandBar buildBar(String key) {
        CommandBar bar = new CommandBar(key);

        menuGenerator.buildMenuFromXML(bar, "com/vitco/logic/tool_bar.xml");

        return bar;
    }
}
