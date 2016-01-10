package com.vitco.layout.content.colorchooser;

import com.jidesoft.swing.JideTabbedPane;
import com.vitco.layout.content.JCustomScrollPane;
import com.vitco.layout.content.colorchooser.basic.ColorChangeListener;
import com.vitco.layout.content.colorchooser.basic.ColorChooserPrototype;
import com.vitco.layout.content.colorchooser.components.colorslider.CMYKTab;
import com.vitco.layout.content.colorchooser.components.colorslider.HSBTab;
import com.vitco.layout.content.colorchooser.components.colorslider.RGBTab;
import com.vitco.util.misc.ColorTools;

import javax.swing.*;
import java.awt.*;

/**
 * Advanced color chooser that uses tabs to display different
 * ways of altering the color.
 */
public class SliderColorChooser extends ColorChooserPrototype {

    // the rgb chooser
    private final RGBTab rgbTab = new RGBTab();

    // the hsb chooser
    private final HSBTab hsbTab = new HSBTab();

    // the cmyk chooser
    private final CMYKTab cmykTab = new CMYKTab();

    // the tabbed pane
    private final JTabbedPane tabbedPane = new JideTabbedPane(JTabbedPane.RIGHT, JideTabbedPane.SCROLL_TAB_LAYOUT);

    // ======================

    // get the active tab
    public final int getActiveTab() {
        return tabbedPane.getSelectedIndex();
    }

    // set the active tab
    public final void setActiveTab(int selectedIndex) {
        if (tabbedPane.getTabCount() > selectedIndex && selectedIndex >= 0) {
            tabbedPane.setSelectedIndex(selectedIndex);
        }
    }

    // set the color that is currently displayed
    private Color color = Color.WHITE;
    public final void setColor(float[] hsb) {
        Color color = ColorTools.hsbToColor(hsb);
        rgbTab.setColor(color);
        hsbTab.setColor(color);
        cmykTab.setColor(color);
        if (!this.color.equals(color)) {
            // none of the above might be visible
            this.color = color;
        }
    }

    // constructor
    public SliderColorChooser() {

        // set up the tabbed pane
        setLayout(new BorderLayout());

        ColorChangeListener ccl = new ColorChangeListener() {
            @Override
            public void colorChanged(float[] hsb) {
                color = ColorTools.hsbToColor(hsb);
                notifyListeners(hsb);
            }
        };
        rgbTab.addColorChangeListener(ccl);
        hsbTab.addColorChangeListener(ccl);
        cmykTab.addColorChangeListener(ccl);

        // add the tabs
        JCustomScrollPane RGBscrollPane = new JCustomScrollPane(rgbTab);
        RGBscrollPane.setHorizontalScrollBarPolicy(JCustomScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        RGBscrollPane.setVerticalScrollBarPolicy(JCustomScrollPane.VERTICAL_SCROLLBAR_NEVER);
        tabbedPane.addTab("RGB", RGBscrollPane);

        JCustomScrollPane HSBscrollPane = new JCustomScrollPane(hsbTab);
        HSBscrollPane.setHorizontalScrollBarPolicy(JCustomScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        HSBscrollPane.setVerticalScrollBarPolicy(JCustomScrollPane.VERTICAL_SCROLLBAR_NEVER);
        tabbedPane.addTab("HSB", HSBscrollPane);

        JCustomScrollPane CMYKscrollPane = new JCustomScrollPane(cmykTab);
        CMYKscrollPane.setHorizontalScrollBarPolicy(JCustomScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        CMYKscrollPane.setVerticalScrollBarPolicy(JCustomScrollPane.VERTICAL_SCROLLBAR_NEVER);
        tabbedPane.addTab("CMYK", CMYKscrollPane);

        // disable focus for tabs themselves
        tabbedPane.setFocusable(false);

        //tabbedPane.setTabShape(JideTabbedPane.SHAPE_ROUNDED_FLAT); // make square
        //tabbedPane.setTabResizeMode(JideTabbedPane.RESIZE_MODE_FIT); // fit them all

        // set tooltips
        for (int i = 0; i < tabbedPane.getTabCount(); i ++) {
            tabbedPane.setToolTipTextAt(i, tabbedPane.getTitleAt(i));
        }

        add(tabbedPane, BorderLayout.CENTER);
    }
}
