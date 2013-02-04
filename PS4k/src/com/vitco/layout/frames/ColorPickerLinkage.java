package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.util.ColorTools;
import com.vitco.util.action.types.StateActionPrototype;
import com.vitco.util.colors.HSBPanelSliderChooser;
import com.vitco.util.colors.basics.ColorChangeListener;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.pref.PrefChangeListener;
import com.vitco.util.pref.PreferencesInterface;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct personal frame (upload, etc)
 */
public class ColorPickerLinkage extends FrameLinkagePrototype {

    // var & setter
    protected PreferencesInterface preferences;
    @Autowired(required=true)
    public final void setPreferences(PreferencesInterface preferences) {
        this.preferences = preferences;
    }

    // var & setter
    private ErrorHandlerInterface errorHandler;
    @Autowired
    public final void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    final HSBPanelSliderChooser pcc = new HSBPanelSliderChooser();

    @Override
    public DockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new DockableFrame(key, new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource("resource/img/icons/frames/colorpicker.png")
        )));
        updateTitle(); // update the title

        // ===========
        // todo move to global class (for global events not clearly linked)
        // initialize the robot (for global color picker)
        Robot tmp = null;
        try {
            tmp = new Robot();
        } catch (AWTException e) {
            errorHandler.handle(e);
        }
        final Robot robot = tmp;
        // register global color picker action
        actionManager.registerAction("pick_color_under_mouse_as_current_color", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (robot != null) {
                    Point mousePosition = MouseInfo.getPointerInfo().getLocation();
                    preferences.storeObject("currently_used_color",
                            ColorTools.colorToHSB(robot.getPixelColor(mousePosition.x, mousePosition.y)));
                }
            }
        });
        // ===========

        // register the color chooser
        frame.add(pcc);
        preferences.addPrefChangeListener("currently_used_color", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                pcc.setColor((float[])o);
            }
        });
        pcc.addColorChangeListener(new ColorChangeListener() {
            @Override
            public void colorChanged(float[] hsb) {
                preferences.storeObject("currently_used_color", hsb);
            }
        });

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
