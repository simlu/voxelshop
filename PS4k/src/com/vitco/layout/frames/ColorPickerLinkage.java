package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.logic.colorpicker.ColorPickerViewInterface;
import com.vitco.util.action.types.StateActionPrototype;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct personal frame (upload, etc)
 */
public class ColorPickerLinkage extends FrameLinkagePrototype {

    // var & setter
    private ColorPickerViewInterface colorPickerView;
    public void setColorPickerView(ColorPickerViewInterface colorPickerView) {
        this.colorPickerView = colorPickerView;
    }

    @Override
    public DockableFrame buildFrame(String key) {
        // construct frame
        frame = new DockableFrame(key, new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource("resource/img/icons/frames/colorpicker.png")
        )));
        updateTitle(); // update the title

        frame.add(colorPickerView.build());

        // register action to hide/show this frame and get visible state
        actionManager.registerAction("colorpicker_state-action_show", new StateActionPrototype() {
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
