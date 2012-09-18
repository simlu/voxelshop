package com.vitco.layout.bars;

import com.jidesoft.action.CommandMenuBar;
import com.vitco.logic.menu.MenuLogicInterface;
import com.vitco.util.menu.MenuGeneratorInterface;
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
    public void setMenuGenerator(MenuGeneratorInterface menuGenerator) {
        this.menuGenerator = menuGenerator;
    }
    // var & setter
    protected MenuLogicInterface menuLogic;
    public void setMenuLogic(MenuLogicInterface menuLogic) {
        this.menuLogic = menuLogic;
    }
}
