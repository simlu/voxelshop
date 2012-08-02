package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.util.action.ActionManagerInterface;
import com.vitco.util.lang.LangSelectorInterface;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 1:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainViewLinkage implements FrameLinkageInterface {
    private DockableFrame frame;

    private LangSelectorInterface langSelector;
    @Override
    public void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }

    private ActionManagerInterface actionManager;
    @Override
    public void setActionManager(ActionManagerInterface actionManager) {
        this.actionManager = actionManager;
    }

    @Override
    public DockableFrame buildFrame(String key) {
        frame = new DockableFrame(key, null);

        //...

        return frame;
    }

    @Override
    public void toggleHidden() {
        frame.setVisible(!frame.isVisible());
    }

    @Override
    public boolean isHidden() {
        return frame.isVisible();
    }
}
