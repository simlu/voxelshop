package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.logic.console.ConsoleViewInterface;
import com.vitco.util.action.types.StateActionPrototype;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct the console frame
 */
public class ConsoleLinkage extends FrameLinkagePrototype {

    // var & setter
    private ConsoleViewInterface consoleView;
    public void setConsoleView(ConsoleViewInterface consoleView) {
        this.consoleView = consoleView;
    }

    @Override
    public DockableFrame buildFrame(String key) {
        // construct frame
        frame = new DockableFrame(key, new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource("resource/img/icons/frames/console.png")
        )));
        updateTitle(); // update the title

        frame.add(consoleView.buildConsole(this));

        // register action to hide/show this frame and get visible state
        actionManager.registerAction("console_state-action_show", new StateActionPrototype() {
            @Override
            public boolean getStatus() {
                return isVisible();
            }

            @Override
            public void action(ActionEvent e) {
                toggleVisible();
            }
        });

        return frame;
    }
}
