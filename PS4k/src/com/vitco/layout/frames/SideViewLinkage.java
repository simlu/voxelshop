package com.vitco.layout.frames;

import com.vitco.layout.content.sideview.SideViewInterface;
import com.vitco.layout.frames.custom.CDockableFrame;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.util.misc.SaveResourceLoader;

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
    public CDockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new CDockableFrame(key,
                new SaveResourceLoader("resource/img/icons/frames/sideView.png").asIconImage(),
                langSelector
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
