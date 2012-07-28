package com.vitco.layout;

import com.jidesoft.action.DockableBar;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.swing.JideButton;
import com.vitco.util.lang.LangSelectorInterface;

import javax.swing.*;
import java.awt.*;

/**
 * Implementation of SubWindowManagerInterface
 *
 * Initializes all the different sub-windows.
 */
public class SubWindowManager implements SubWindowManagerInterface {

    /////////////////////////////////////
    // Sub-windows //
    /////////////////////////////////////

    // handles the fetching of strings for keys for localization
    protected LangSelectorInterface langSel;
    // setter method for langSel
    @Override
    public void setLangSelector(LangSelectorInterface langSel) {
        this.langSel = langSel;
    }

    // the xyz view
    @Override
    public DockableFrame prepareSideView(DockableFrame dockableFrame) {
        return dockableFrame;
    }

    // the log (console)
    @Override
    public DockableFrame prepareConsole(DockableFrame dockableFrame) {
        return dockableFrame;
    }

    // the main view
    @Override
    public DockableFrame prepareMainView(DockableFrame dockableFrame) {
        return dockableFrame;
    }

    // the time-line (animation)
    @Override
    public DockableFrame prepareTimeLine(DockableFrame dockableFrame) {
        return dockableFrame;
    }


    /////////////////////////////////////
    // Docks //
    /////////////////////////////////////

    // the main menu
    @Override
    public DockableBar prepareMainMenu(DockableBar dockableBar) {
        dockableBar.add(new JideButton(langSel.getString("file_btn")));
        dockableBar.add(new JideButton(langSel.getString("edit_btn")));
        dockableBar.add(new JideButton(langSel.getString("view_btn")));
        return dockableBar;
    }

    // the (main) tool menu
    @Override
    public DockableBar prepareToolBar(DockableBar dockableBar) {

        JideButton jideButton;

        // create the draw tool button
        jideButton = new JideButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource("resource/img/icons/tools/draw.png")
        )));
        jideButton.setToolTipText(langSel.getString("draw_tooltip"));
        dockableBar.add(jideButton);

        // create the animation tool button
        jideButton = new JideButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource("resource/img/icons/tools/animate.png")
        )));
        jideButton.setToolTipText(langSel.getString("animate_tooltip"));
        dockableBar.add(jideButton);

        return dockableBar;
    }

}
