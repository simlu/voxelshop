package com.vitco.layout.frames;

import com.vitco.layout.content.colorchooser.SliderColorChooser;
import com.vitco.layout.content.colorchooser.basic.ColorChangeListener;
import com.vitco.layout.frames.custom.CDockableFrame;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.manager.pref.PrefChangeListener;
import com.vitco.util.misc.SaveResourceLoader;

import javax.annotation.PreDestroy;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct personal frame (upload, etc)
 */
public class ColorSliderLinkage extends FrameLinkagePrototype {


    // the color chooser
    final SliderColorChooser scc = new SliderColorChooser();

    @PreDestroy
    public final void savePref() {
        preferences.storeInteger("colorslider_active-tab", scc.getActiveTab());
    }

    @Override
    public CDockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new CDockableFrame(key,
                new SaveResourceLoader("resource/img/icons/frames/colorslider.png").asIconImage(),
                langSelector
        );
        updateTitle(); // update the title

        // add the chooser
        frame.add(scc);
        // load active tab
        if (preferences.contains("colorslider_active-tab")) {
            scc.setActiveTab(preferences.loadInteger("colorslider_active-tab"));
        }
        // register the color chooser events
        preferences.addPrefChangeListener("currently_used_color", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                scc.setColor((float[])o);
            }
        });
        scc.addColorChangeListener(new ColorChangeListener() {
            @Override
            public void colorChanged(float[] hsb) {
                preferences.storeObject("currently_used_color", hsb);
            }
        });

        // register action to hide/show this frame and get visible state
        actionManager.registerAction("colorslider_state-action_show", new StateActionPrototype() {
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
