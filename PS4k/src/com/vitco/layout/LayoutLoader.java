package com.vitco.layout;

import com.jidesoft.plaf.LookAndFeelFactory;

import javax.swing.*;

/**
 * Load the layout and customizer
 */
public class LayoutLoader {
    public static void loadLayout() {
        // load the layout
        LookAndFeelFactory.installDefaultLookAndFeel();
        LookAndFeelFactory.installJideExtension(LookAndFeelFactory.EXTENSION_STYLE_VSNET);

        // set custom painter
        UIManager.getDefaults().put("Theme.painter", new ButtonLayoutPainter());

    }
}
