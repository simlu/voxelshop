package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;
import com.vitco.engine.data.Data;
import com.vitco.util.action.ActionManagerInterface;
import com.vitco.util.menu.MenuGeneratorInterface;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

/**
 * Prototype of class that links bar to content.
 */
public abstract class BarLinkagePrototype {
    // constructs the bar (with content)
    public abstract CommandBar buildBar(String key, Frame frame);
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
    // var & setter (can not be interface!!)
    protected Data data;
    @Autowired
    public void setData(Data data) {
        this.data = data;
    }
}
