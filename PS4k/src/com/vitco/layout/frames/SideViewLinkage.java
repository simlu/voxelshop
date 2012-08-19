package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.frames.engine.model.SideViewInterface;
import com.vitco.util.action.types.StateActionPrototype;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct the side view frame
 */
public class SideViewLinkage extends FrameLinkagePrototype {

    // var & setter
    private SideViewInterface sideView;
    public void setSideView(SideViewInterface sideView) {
        this.sideView = sideView;
    }

    @Override
    public DockableFrame buildFrame(String key) {
        // construct frame
        frame = new DockableFrame(key, new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource("resource/img/icons/frames/sideView.png")
        )));
        updateTitle(); // update the title

        final JPanel pane = new JPanel();
        sideView.buildSides(pane); // build content of frame
        frame.add(pane);

        // register action to hide/show this frame and get visible state
        actionManager.registerAction("side-view_state-action_show", new StateActionPrototype() {
            @Override
            public boolean getStatus() {
                return isVisible();
            }

            @Override
            public void action(ActionEvent e) {
                toggleVisible();
            }
        });

        return frame;
    }
}
