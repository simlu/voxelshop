package com.vitco.layout.frames;

import com.vitco.layout.frames.custom.CDockableFrame;
import com.vitco.manager.action.ActionManager;
import com.vitco.manager.lang.LangSelectorInterface;
import com.vitco.manager.pref.PreferencesInterface;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;

/**
 * Prototype of class that links frame to content.
 */
public abstract class FrameLinkagePrototype {

    // var & setter
    protected PreferencesInterface preferences;
    @Autowired(required=true)
    public final void setPreferences(PreferencesInterface preferences) {
        this.preferences = preferences;
    }

    // var & setter
    protected LangSelectorInterface langSelector;
    @Autowired(required=true)
    public final void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }

    // var & setter
    protected ActionManager actionManager;
    @Autowired(required=true)
    public final void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    // show frame
    public final void setVisible(boolean b) {
        if (frame.getDockingManager() != null) {
            if (b) {
                frame.getDockingManager().showFrame(frame.getName());
            } else {
                frame.getDockingManager().hideFrame(frame.getName());
            }
        }
    }

    // hide / show frame
    public void toggleVisible() {
        if (frame.getDockingManager() != null) {
            setVisible(!frame.getDockingManager().getFrame(frame.getName()).isVisible());
        }
    }

    // returns true iff frame is visible
    public boolean isAutohideShowing() {
        return frame.getDockingManager() != null && frame.getDockingManager().getFrame(frame.getName()).isAutohideShowing();
    }

    // returns true iff frame is visible
    public boolean isAutohide() {
        return frame.getDockingManager() != null && frame.getDockingManager().getFrame(frame.getName()).isAutohide();
    }

    // returns true iff frame is visible
    public boolean isVisible() {
        return frame.getDockingManager() != null && frame.getDockingManager().getFrame(frame.getName()).isVisible();
    }

    // updates the title when the frame is ready
    protected void updateTitle() {
        // invoke when ready
        // "This method should be used when an application thread needs to update the GUI"
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String title = langSelector.getString(frame.getName() + "_caption");
                frame.setTitle(title);
                frame.setTabTitle(title);
                frame.setSideTitle(title);
            }
        });
    }

    // holds the reference of the actual frame container
    protected CDockableFrame frame;
    // constructs the frame (with content)
    public abstract CDockableFrame buildFrame(String key, Frame mainFrame);
}

