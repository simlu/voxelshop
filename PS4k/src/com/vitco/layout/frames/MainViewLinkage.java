package com.vitco.layout.frames;

import com.vitco.layout.content.mainview.MainViewInterface;
import com.vitco.layout.frames.custom.CDockableFrame;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.util.misc.SaveResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * construct the main view
 */
public class MainViewLinkage extends FrameLinkagePrototype {

    // var & setter
    private MainViewInterface mainView;
    public final void setMainView(MainViewInterface mainView) {
        this.mainView = mainView;
    }

    @Override
    public CDockableFrame buildFrame(String key, Frame mainFrame) {
        // construct frame
        frame = new CDockableFrame(key,
                new SaveResourceLoader("resource/img/icons/frames/mainview.png").asIconImage(),
                langSelector
        );
        updateTitle(); // update the title

        udateTitleWithFileName("NEW MODEL");

        frame.add(mainView.build(this));

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

    public void udateTitleWithFileName(final String fileName)
    {
        SwingUtilities.invokeLater(new RunnableWithParam(fileName) {
            public void run() {
                String title = langSelector.getString(frame.getName() + "_caption") + " - " + fileName;
                frame.setTitle(title);
                frame.setTabTitle(title);
                frame.setSideTitle(title);
            }
        });
    }

    class RunnableWithParam implements Runnable{
        private String strParam;
        public RunnableWithParam(String p){ strParam = p; }
        public void run(){}
    }
}
