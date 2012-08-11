package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;
import com.vitco.util.MenuGeneratorInterface;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 12:12 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BarLinkagePrototype {
    abstract public CommandBar buildBar(String key);
    protected MenuGeneratorInterface menuGenerator;
    @Autowired(required = true)
    public void setMenuGenerator(MenuGeneratorInterface menuGenerator) {
        this.menuGenerator = menuGenerator;
    }
}
