package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.logic.mainview.MainViewInterface;
import com.vitco.util.SaveResourceLoader;
import com.vitco.util.action.types.StateActionPrototype;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;

/**
 * construct the main view
 */
public class MainViewLinkage extends FrameLinkagePrototype {

    // var & setter
    private MainViewInterface mainView;
    public final void setMainView(MainViewInterface mainView) {
        this.mainView = mainView;
    }

    @Override
    public DockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new DockableFrame(key,
                new SaveResourceLoader("resource/img/icons/frames/mainview.png").asIconImage()
        );
        updateTitle(); // update the title
        // remove all existing mouse listeners (no idea why there exist any.. jide?!)
        // prevent stupid popup
        for (MouseListener ml : frame.getMouseListeners()) {
            frame.removeMouseListener(ml);
        }

        frame.add(mainView.build());

        // register action to hide/show this frame and get visible state
        actionManager.registerAction("mainview_state-action_show", new StateActionPrototype() {
            @Override
            public boolean getStatus() {
                return frame.isVisible();
            }

            @Override
            public void action(ActionEvent e) {
                toggleVisible();
            }
        });

        return frame;
    }
}
