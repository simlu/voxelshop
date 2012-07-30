package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;
import com.vitco.util.lang.LangSelectorInterface;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 12:12 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BarLinkageInterface {
    public CommandBar buildBar(String key, LangSelectorInterface langSel);
}
