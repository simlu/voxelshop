package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;
import com.jidesoft.swing.JideButton;
import com.vitco.util.lang.LangSelectorInterface;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 12:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainMenuLinkage implements BarLinkageInterface {

    @Override
    public CommandBar buildBar(String key, LangSelectorInterface langSel) {
        CommandBar bar = new CommandBar(key);
        bar.add(new JideButton(langSel.getString("file_btn")));
        bar.add(new JideButton(langSel.getString("edit_btn")));
        bar.add(new JideButton(langSel.getString("view_btn")));
        return bar;
    }
}
