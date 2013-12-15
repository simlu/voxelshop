package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.layout.content.layer.LayerViewInterface;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.util.misc.SaveResourceLoader;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * displays layers and defines layer interaction
 */
public class LayerLinkage extends FrameLinkagePrototype {

    // var & setter
    private LayerViewInterface layerView;
    public final void setLayerView(LayerViewInterface layerView) {
        this.layerView = layerView;
    }

    @Override
    public DockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new DockableFrame(key,
                new SaveResourceLoader("resource/img/icons/frames/layer.png").asIconImage()
        );
        updateTitle(); // update the title

        frame.add(layerView.build());

        // register action to hide/show this frame and get visible state
        actionManager.registerAction("layer_state-action_show", new StateActionPrototype() {
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
