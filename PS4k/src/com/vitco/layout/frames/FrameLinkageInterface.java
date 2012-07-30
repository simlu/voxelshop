package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.util.lang.LangSelectorInterface;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
public interface FrameLinkageInterface {
    public DockableFrame buildFrame(String key, LangSelectorInterface langSel);
}

