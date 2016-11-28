package com.vitco.app.layout.bars;

import com.jidesoft.action.CommandMenuBar;
import com.vitco.app.layout.content.menu.MenuLogicInterface;
import com.vitco.app.manager.menu.MenuGeneratorInterface;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

/**
 * Prototype of class that links bar to content.
 */
public abstract class BarLinkagePrototype {
    // constructs the bar (with content)
    public abstract CommandMenuBar buildBar(String key, Frame frame);
    // var & setter
    protected MenuGeneratorInterface menuGenerator;
    @Autowired(required = true)
    public final void setMenuGenerator(MenuGeneratorInterface menuGenerator) {
        this.menuGenerator = menuGenerator;
    }
    // var & setter
    protected MenuLogicInterface menuLogic;
    public final void setMenuLogic(MenuLogicInterface menuLogic) {
        this.menuLogic = menuLogic;
    }
}
