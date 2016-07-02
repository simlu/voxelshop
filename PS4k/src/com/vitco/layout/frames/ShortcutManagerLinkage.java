package com.vitco.layout.frames;

import com.vitco.layout.content.shortcut.ShortcutManagerViewInterface;
import com.vitco.layout.frames.custom.CDockableFrame;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.util.misc.SaveResourceLoader;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct the shortcut manager frame
 */
public class ShortcutManagerLinkage extends FrameLinkagePrototype {

    // var & setter (we only need the shortcutManager in this frame!)
    private ShortcutManagerViewInterface shortcutManagerView;
    public final void setShortcutManagerView(ShortcutManagerViewInterface shortcutManagerView) {
        this.shortcutManagerView = shortcutManagerView;
    }

    @Override
    public CDockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new CDockableFrame(key,
                new SaveResourceLoader("resource/img/icons/frames/shortcutManager.png").asIconImage(),
                langSelector
        );
        updateTitle(); // update the title

        // add the tabbelPane to this frame that manages all the shortcuts
        frame.add(shortcutManagerView.getEditTables());

        // register action to show and hide this frame
        actionManager.registerAction("shortcut-mg_state-action_show", new StateActionPrototype() {
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
