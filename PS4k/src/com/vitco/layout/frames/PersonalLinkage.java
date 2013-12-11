package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.util.SaveResourceLoader;
import com.vitco.util.action.types.StateActionPrototype;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct personal frame (upload, etc)
 */
public class PersonalLinkage extends FrameLinkagePrototype {
    @Override
    public DockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new DockableFrame(key,
                new SaveResourceLoader("resource/img/icons/frames/personal.png").asIconImage()
        );
        updateTitle(); // update the title

        // ...

        // register action to hide/show this frame and get visible state
        actionManager.registerAction("personal_state-action_show", new StateActionPrototype() {
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
