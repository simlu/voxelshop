package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.util.action.ActionManagerInterface;
import com.vitco.util.lang.LangSelectorInterface;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class FrameLinkagePrototype {
    LangSelectorInterface langSelector;
    @Autowired(required=true)
    public void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }
    ActionManagerInterface actionManager;
    @Autowired(required=true)
    public void setActionManager(ActionManagerInterface actionManager) {
        this.actionManager = actionManager;
    }

    DockableFrame frame;
    void toggleVisible() {
        if (frame.getDockingManager().getFrame(frame.getName()).isVisible()) {
            frame.getDockingManager().hideFrame(frame.getName());
        } else {
            frame.getDockingManager().showFrame(frame.getName());
        }
    }
    boolean isVisible() {
        return frame.getDockingManager().getFrame(frame.getName()).isVisible();
    }

    abstract public DockableFrame buildFrame(String key);
}

