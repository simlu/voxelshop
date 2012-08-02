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
    // set the language selector that provides translations (text)
    public void setLangSelector(LangSelectorInterface langSel);
    // prepare a frame (key is the key of the frame)
    public DockableFrame prepareFrame(String key);
    // prepare a bar (key is the key of the frame)
    public DockableBar prepareBar(String key);
    // set the key <-> bar class linkage map (defined in config.xml)
    public void setBarLinkageMap(Map<String, BarLinkageInterface> map);
    // set the key <-> frame class linkage map (defined in config.xml)
    public void setFrameLinkageMap(Map<String, FrameLinkageInterface> map);
    // set the uri of the file that deals with the layout
    public void setLayoutFile(String filename);
}
