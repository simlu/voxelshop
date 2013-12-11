package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.logic.sideview.SideViewInterface;
import com.vitco.util.SaveResourceLoader;
import com.vitco.util.action.types.StateActionPrototype;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct a side view frame
 */
public class SideViewLinkage extends FrameLinkagePrototype {

    // var & setter
    private SideViewInterface sideView;
    public final void setSideView(SideViewInterface sideView) {
        this.sideView = sideView;
    }

    @Override
    public DockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new DockableFrame(key,
                new SaveResourceLoader("resource/img/icons/frames/sideView.png").asIconImage()
        );
        updateTitle(); // update the title

        frame.add(sideView.build());

        // register action to hide/show this frame and get visible state
        actionManager.registerAction("side-view_state-action_show" + (sideView.getSide()+1), new StateActionPrototype() {
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
