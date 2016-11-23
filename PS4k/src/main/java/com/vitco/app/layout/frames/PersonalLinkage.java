package com.vitco.app.layout.frames;

import com.vitco.app.layout.frames.custom.CDockableFrame;
import com.vitco.app.manager.action.types.StateActionPrototype;
import com.vitco.app.util.misc.SaveResourceLoader;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct personal frame (upload, etc)
 */
public class PersonalLinkage extends FrameLinkagePrototype {
    @Override
    public CDockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new CDockableFrame(key,
                new SaveResourceLoader("resource/img/icons/frames/personal.png").asIconImage(),
                langSelector
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
