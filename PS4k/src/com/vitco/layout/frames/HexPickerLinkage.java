package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.util.action.types.StateActionPrototype;
import com.vitco.util.colors.HexColorChooser;
import com.vitco.util.colors.basics.ColorChangeListener;
import com.vitco.util.pref.PrefChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct the hex color picker frame
 */
public class HexPickerLinkage extends FrameLinkagePrototype {

    @Override
    public DockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new DockableFrame(key, new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource("resource/img/icons/frames/hexpicker.png")
        )));
        updateTitle(); // update the title

        // register hex color picker button
        final HexColorChooser hexColorChooser = new HexColorChooser();
        hexColorChooser.addColorChangeListener(new ColorChangeListener() {
            @Override
            public void colorChanged(float[] hsb) {
                preferences.storeObject("currently_used_color", hsb);
            }
        });
        // refresh the hex color chooser when the current color changes
        preferences.addPrefChangeListener("currently_used_color", new PrefChangeListener() {
            @Override
            public void onPrefChange(final Object o) {
                hexColorChooser.setColor((float[])o);
            }
        });

        frame.add(hexColorChooser);

        // register action to hide/show this frame and get visible state
        actionManager.registerAction("hexPicker_state-action_show", new StateActionPrototype() {
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
