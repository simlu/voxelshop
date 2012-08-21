package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.frames.engine.mainview.MainViewInterface;
import com.vitco.res.VitcoSettings;

import javax.swing.*;
import java.awt.event.MouseListener;

/**
 * construct the main view
 */
public class MainViewLinkage extends FrameLinkagePrototype {

    // var & setter
    private MainViewInterface mainView;
    public void setMainView(MainViewInterface mainView) {
        this.mainView = mainView;
    }

    @Override
    public DockableFrame buildFrame(String key) {
        // construct the frame
        frame = new DockableFrame(key, null);
        // update the title
        updateTitle();
        // remove all existing mouse listeners (no idea why there exist any.. jide?!)
        // prevent stupid popup
        for (MouseListener ml : frame.getMouseListeners()) {
            frame.removeMouseListener(ml);
        }

        mainView.build(frame);

        frame.setBorder(BorderFactory.createLineBorder(VitcoSettings.ANIMATION_BORDER_COLOR));


        return frame;
    }
}
