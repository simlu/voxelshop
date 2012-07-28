package com.vitco.layout;

import com.jidesoft.action.DockableBar;
import com.jidesoft.docking.DockableFrame;
import com.vitco.util.lang.LangSelectorInterface;

/**
 * Interface
 *
 * Initializes all the different sub-windows.
 */
public interface SubWindowManagerInterface {
    public void setLangSelector(LangSelectorInterface langSel);
    public DockableFrame prepareSideView(DockableFrame dockableFrame);
    public DockableFrame prepareConsole(DockableFrame dockableFrame);
    public DockableFrame prepareMainView(DockableFrame dockableFrame);
    public DockableFrame prepareTimeLine(DockableFrame dockableFrame);
    public DockableBar prepareMainMenu(DockableBar dockableBar);
    public DockableBar prepareToolBar(DockableBar dockableBar);
}
