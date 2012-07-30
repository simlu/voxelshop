package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;
import com.jidesoft.swing.JideButton;
import com.vitco.util.lang.LangSelectorInterface;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 12:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToolBarLinkage implements BarLinkageInterface {

    @Override
    public CommandBar buildBar(String key, LangSelectorInterface langSel) {
        CommandBar bar = new CommandBar(key);

        JideButton jideButton;
        // create the draw tool button
        jideButton = new JideButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource("resource/img/icons/tools/draw.png")
        )));
        jideButton.setToolTipText(langSel.getString("draw_tooltip"));
        bar.add(jideButton);

        // create the animation tool button
        jideButton = new JideButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource("resource/img/icons/tools/animate.png")
        )));
        jideButton.setToolTipText(langSel.getString("animate_tooltip"));
        bar.add(jideButton);

        return bar;
    }
}
