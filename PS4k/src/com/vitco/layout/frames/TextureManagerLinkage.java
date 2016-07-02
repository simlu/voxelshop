package com.vitco.layout.frames;

import com.vitco.layout.content.texture.TextureManagerInterface;
import com.vitco.layout.frames.custom.CDockableFrame;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.util.misc.SaveResourceLoader;

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
    public CDockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new CDockableFrame(key,
                new SaveResourceLoader("resource/img/icons/frames/textureManager.png").asIconImage(),
                langSelector
        );
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
