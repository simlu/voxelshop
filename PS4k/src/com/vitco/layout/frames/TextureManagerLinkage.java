package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.logic.texture.TextureManagerInterface;
import com.vitco.util.action.types.StateActionPrototype;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct the texture manager frame
 */
public class TextureManagerLinkage extends FrameLinkagePrototype {

    // var & setter
    private TextureManagerInterface textureManager;
    public final void setTextureManager(TextureManagerInterface textureManager) {
        this.textureManager = textureManager;
    }

    @Override
    public DockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new DockableFrame(key, new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource("resource/img/icons/frames/textureManager.png")
        )));
        updateTitle(); // update the title

        frame.add(textureManager.build(mainFrame));

        // register action to hide/show this frame and get visible state
        actionManager.registerAction("textureManager_state-action_show", new StateActionPrototype() {
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
