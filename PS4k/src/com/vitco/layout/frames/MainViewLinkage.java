package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 1:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainViewLinkage extends FrameLinkagePrototype {
    @Override
    public DockableFrame buildFrame(String key) {
        frame = new DockableFrame(key, null);

        updateTitle();

        // ...

        return frame;
    }
}
