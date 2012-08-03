package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.actions.StateActionInterface;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 1:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimeLineLinkage extends FrameLinkagePrototype {
    @Override
    public DockableFrame buildFrame(String key) {
        frame = new DockableFrame(key, null);

        frame.addPropertyChangeListener("title", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String title = langSelector.getString("time_line_btn");
                if (evt.getNewValue() != title) {
                    frame.setTitle(title);
                    frame.setTabTitle(title);
                    frame.setSideTitle(title);
                }
            }
        });

        actionManager.registerAction("time-line_state-action_show", new StateActionInterface() {
            @Override
            public boolean getStatus() {
                return isVisible();
            }

            @Override
            public void performAction() {
                toggleVisible();
            }
        });

        return frame;
    }
}
