package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;
import com.vitco.util.action.ActionManagerInterface;
import com.vitco.util.menu.MenuGeneratorInterface;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Prototype of class that links bar to content.
 */
public abstract class BarLinkagePrototype {
    // constructs the bar (with content)
    public abstract CommandBar buildBar(String key);
    // var & setter
    protected MenuGeneratorInterface menuGenerator;
    @Autowired(required = true)
    public void setMenuGenerator(MenuGeneratorInterface menuGenerator) {
        this.menuGenerator = menuGenerator;
    }
    // var & setter
    protected ActionManagerInterface actionManager;
    @Autowired(required=true)
    public void setActionManager(ActionManagerInterface actionManager) {
        this.actionManager = actionManager;
    }
}
