package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.util.ColorTools;
import com.vitco.util.action.types.StateActionPrototype;
import com.vitco.util.colors.ColorChangeListener;
import com.vitco.util.colors.TabbedColorChooser;
import com.vitco.util.pref.PrefChangeListener;
import com.vitco.util.pref.PreferencesInterface;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct personal frame (upload, etc)
 */
public class ColorSliderLinkage extends FrameLinkagePrototype {

    // var & setter
    protected PreferencesInterface preferences;
    @Autowired(required=true)
    public final void setPreferences(PreferencesInterface preferences) {
        this.preferences = preferences;
    }

    // the color chooser
    final TabbedColorChooser scc = new TabbedColorChooser();

    @PreDestroy
    public final void savePref() {
        preferences.storeInteger("colorslider_active-tab", scc.getActiveTab());
    }

    @Override
    public DockableFrame buildFrame(String key) {
        // construct frame
        frame = new DockableFrame(key, new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource("resource/img/icons/frames/colorslider.png")
        )));
        updateTitle(); // update the title

        // the color chooser events
        scc.addColorChangeListener(new ColorChangeListener() {
            @Override
            public void colorChanged(Color color) {
                preferences.storeObject("currently_used_color", ColorTools.colorToHSB(color));
            }
        });
        preferences.addPrefChangeListener("currently_used_color", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                scc.setColor((float[])o);
            }
        });
        // load active tab
        if (preferences.contains("colorslider_active-tab")) {
            scc.setActiveTab(preferences.loadInteger("colorslider_active-tab"));
        }
        frame.add(scc);

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
