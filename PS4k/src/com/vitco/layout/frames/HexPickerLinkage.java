package com.vitco.layout.frames;

import com.vitco.layout.content.colorchooser.HexColorChooser;
import com.vitco.layout.content.colorchooser.basic.ColorChangeListener;
import com.vitco.layout.frames.custom.CDockableFrame;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.manager.pref.PrefChangeListener;
import com.vitco.util.misc.SaveResourceLoader;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct the hex color picker frame
 */
public class HexPickerLinkage extends FrameLinkagePrototype {

    @Override
    public CDockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new CDockableFrame(key,
                new SaveResourceLoader("resource/img/icons/frames/hexpicker.png").asIconImage(),
                langSelector
        );
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
