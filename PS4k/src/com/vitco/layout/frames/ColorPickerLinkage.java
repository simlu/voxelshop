package com.vitco.layout.frames;

import com.vitco.layout.content.colorchooser.HSBPanelSliderChooser;
import com.vitco.layout.content.colorchooser.basic.ColorChangeListener;
import com.vitco.layout.frames.custom.CDockableFrame;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.manager.error.ErrorHandlerInterface;
import com.vitco.manager.pref.PrefChangeListener;
import com.vitco.util.misc.ColorTools;
import com.vitco.util.misc.SaveResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct personal frame (upload, etc)
 */
public class ColorPickerLinkage extends FrameLinkagePrototype {

    // var & setter
    private ErrorHandlerInterface errorHandler;
    @Autowired
    public final void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    final HSBPanelSliderChooser pcc = new HSBPanelSliderChooser();

    @Override
    public CDockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new CDockableFrame(key,
                new SaveResourceLoader("resource/img/icons/frames/colorpicker.png").asIconImage(),
                langSelector
        );
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
