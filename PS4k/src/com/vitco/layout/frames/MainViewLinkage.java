package com.vitco.layout.frames;

import com.vitco.layout.content.mainview.MainViewInterface;
import com.vitco.layout.content.menu.MainMenuLogic;
import com.vitco.layout.frames.custom.CDockableFrame;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.util.misc.SaveResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * construct the main view
 */
public class MainViewLinkage extends FrameLinkagePrototype {

    // var & setter
    private MainViewInterface mainView;
    public final void setMainView(MainViewInterface mainView) {
        this.mainView = mainView;
    }

    // var & setter
    protected MainMenuLogic menuLogic;
    @Autowired
    public final void setMenuLogic(MainMenuLogic menuLogic) {
        this.menuLogic = menuLogic;
    }

    @Override
    public CDockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new CDockableFrame(key,
                new SaveResourceLoader("resource/img/icons/frames/mainview.png").asIconImage(),
                langSelector
        );

        // ensure the title is updated correctly
        menuLogic.addSaveLocationListener(new ActionListener() {
            private void setTitle(String newTitle) {
                frame.setTitle(newTitle);
                frame.setTabTitle(newTitle);
                frame.setSideTitle(newTitle);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = e.getActionCommand();
                String newTitle = langSelector.getString(frame.getName() + "_caption") + " - ";
                newTitle += fileName != null ? fileName : "NEW MODEL";
                setTitle(newTitle);
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                menuLogic.setSaveLocation(null);
            }
        });

        frame.add(mainView.build());

        // register action to hide/show this frame and get visible state
        actionManager.registerAction("mainview_state-action_show", new StateActionPrototype() {
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
