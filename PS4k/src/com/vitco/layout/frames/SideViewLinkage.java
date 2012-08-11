package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.action.types.StateActionPrototype;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 1:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class SideViewLinkage extends FrameLinkagePrototype {
    @Override
    public DockableFrame buildFrame(String key) {
        frame = new DockableFrame(key, new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource("resource/img/icons/frames/sideView.png")
        )));

        updateTitle();

        //...

        actionManager.registerAction("side-view_state-action_show", new StateActionPrototype() {
            @Override
            public boolean getStatus() {
                return isVisible();
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                toggleVisible();
            }
        });

        return frame;
    }
}
