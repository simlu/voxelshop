package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.action.types.StateActionPrototype;
import com.vitco.shortcut.ShortcutManagerViewInterface;

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
public class ShortcutManagerLinkage extends FrameLinkagePrototype {

    // var & setter (we only need the shortcutManager in this frame!)
    private ShortcutManagerViewInterface shortcutManagerView;
    public void setShortcutManagerView(ShortcutManagerViewInterface shortcutManagerView) {
        this.shortcutManagerView = shortcutManagerView;
    }

    @Override
    public DockableFrame buildFrame(String key) {
        frame = new DockableFrame(key, new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource("resource/img/icons/frames/shortcutManager.png")
        )));
        updateTitle();

        // add the tabbelPane to this frame
        frame.add(shortcutManagerView.getEditTables());

        // register action to show and hide this frame
        actionManager.registerAction("shortcut-mg_state-action_show", new StateActionPrototype() {
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
