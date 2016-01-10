package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.layout.content.colorAdjuster.ColorAdjusterInterface;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.util.misc.SaveResourceLoader;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct the color adjuster frame
 */
public class ColorAdjusterLinkage extends FrameLinkagePrototype {

    private ColorAdjusterInterface colorAdjuster;
    public void setColorAdjuster(ColorAdjusterInterface colorAdjuster) {
        this.colorAdjuster = colorAdjuster;
    }

    @Override
    public DockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new DockableFrame(key,
                new SaveResourceLoader("resource/img/icons/frames/colorAdjuster.png").asIconImage()
        );
        updateTitle(); // update the title

        frame.add(colorAdjuster.build(mainFrame));

        // register action to hide/show this frame and get visible state
        actionManager.registerAction("colorAdjuster_state-action_show", new StateActionPrototype() {
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
