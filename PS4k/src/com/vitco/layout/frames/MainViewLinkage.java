package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;

/**
 * construct the main view
 */
public class MainViewLinkage extends FrameLinkagePrototype {
    @Override
    public DockableFrame buildFrame(String key) {
        // construct the frame
        frame = new DockableFrame(key, null);
        // update the title
        updateTitle();

        // ...

        return frame;
    }
}
