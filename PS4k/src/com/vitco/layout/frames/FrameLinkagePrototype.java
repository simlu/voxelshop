package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.action.ActionManagerInterface;
import com.vitco.util.LangSelectorInterface;
import org.springframework.beans.factory.annotation.Autowired;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class FrameLinkagePrototype {
    protected LangSelectorInterface langSelector;
    @Autowired(required=true)
    public void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }

    protected ActionManagerInterface actionManager;
    @Autowired(required=true)
    public void setActionManager(ActionManagerInterface actionManager) {
        this.actionManager = actionManager;
    }

    protected void toggleVisible() {
        if (frame.getDockingManager().getFrame(frame.getName()).isVisible()) {
            frame.getDockingManager().hideFrame(frame.getName());
        } else {
            frame.getDockingManager().showFrame(frame.getName());
        }
    }

    protected boolean isVisible() {
        return frame.getDockingManager().getFrame(frame.getName()).isVisible();
    }

    protected void updateTitle() {
        frame.addPropertyChangeListener("title", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String title = langSelector.getString(frame.getName() + "_caption");
                if (evt.getNewValue() != title) {
                    frame.setTitle(title);
                    frame.setTabTitle(title);
                    frame.setSideTitle(title);
                }
            }
        });
    }

    protected DockableFrame frame;
    abstract public DockableFrame buildFrame(String key);
}

