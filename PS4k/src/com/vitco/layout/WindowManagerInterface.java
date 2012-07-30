package com.vitco.layout;

import com.jidesoft.action.DockableBar;
import com.jidesoft.docking.DockableFrame;
import com.vitco.layout.bars.BarLinkageInterface;
import com.vitco.layout.frames.FrameLinkageInterface;
import com.vitco.util.lang.LangSelectorInterface;

import java.util.Map;

/**
 * Interface
 *
 * Initializes all the different sub-windows.
 */
public interface WindowManagerInterface {
    public void setLangSelector(LangSelectorInterface langSel);
    public DockableFrame prepareFrame(String key);
    public DockableBar prepareBar(String key);
    public void setBarLinkageMap(Map<String, BarLinkageInterface> map);
    public void setFrameLinkageMap(Map<String, FrameLinkageInterface> map);
    public void setLayoutFileURI(String layoutFileURI);
}
