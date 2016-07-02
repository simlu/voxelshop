package com.vitco.layout;

import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideTabbedPane;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.misc.SaveResourceLoader;

import javax.swing.*;
import java.awt.*;

/**
 * Load the layout and customizer
 */
public class LayoutLoader {

    public static void loadLayout() {

        // load the layout
        LookAndFeelFactory.installDefaultLookAndFeel();
        LookAndFeelFactory.installJideExtension(LookAndFeelFactory.EXTENSION_STYLE_VSNET);

        // hide border from menu popups
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(VitcoSettings.SOFT_BLACK));

        // set menu item color
        UIManager.put("Menu.disabledForeground", VitcoSettings.MAIN_MENU_DISABLED_COLOR);
        UIManager.put("Menu.foreground", VitcoSettings.MAIN_MENU_ENABLED_COLOR);
        UIManager.put("Menu.selectionForeground", VitcoSettings.MAIN_MENU_ENABLED_COLOR);
        UIManager.put("Menu.background", VitcoSettings.MAIN_MENU_BACKGROUND);
        UIManager.put("Menu.disabledBackground", VitcoSettings.MAIN_MENU_BACKGROUND);

        UIManager.put("MenuBar.background", VitcoSettings.MAIN_MENU_ENABLED_COLOR);
        UIManager.put("MenuBar.disabledForeground", VitcoSettings.MAIN_MENU_DISABLED_COLOR);
        UIManager.put("MenuBar.foreground", VitcoSettings.MAIN_MENU_ENABLED_COLOR);
        UIManager.put("MenuBar.highlight", VitcoSettings.MAIN_MENU_ENABLED_COLOR);
        UIManager.put("MenuBar.selectionForeground", VitcoSettings.MAIN_MENU_ENABLED_COLOR);
        UIManager.put("MenuBar.background", VitcoSettings.MAIN_MENU_BACKGROUND);
        UIManager.put("MenuBar.disabledBackground", VitcoSettings.MAIN_MENU_BACKGROUND);

        UIManager.put("MenuItem.disabledForeground", VitcoSettings.MAIN_MENU_DISABLED_COLOR);
        UIManager.put("MenuItem.acceleratorForeground", VitcoSettings.MAIN_MENU_ENABLED_COLOR);
        UIManager.put("MenuItem.acceleratorSelectionForeground", VitcoSettings.MAIN_MENU_ENABLED_COLOR);
        UIManager.put("MenuItem.disabledForeground", VitcoSettings.MAIN_MENU_DISABLED_COLOR);
        UIManager.put("MenuItem.foreground", VitcoSettings.MAIN_MENU_ENABLED_COLOR);
        UIManager.put("MenuItem.selectionForeground", VitcoSettings.MAIN_MENU_ENABLED_COLOR);
        UIManager.put("MenuItem.background", VitcoSettings.MAIN_MENU_BACKGROUND);
        UIManager.put("MenuItem.disabledBackground", VitcoSettings.MAIN_MENU_BACKGROUND);

        UIManager.put("CheckBoxMenuItem.acceleratorForeground", VitcoSettings.MAIN_MENU_ENABLED_COLOR);
        UIManager.put("CheckBoxMenuItem.acceleratorSelectionForeground", VitcoSettings.MAIN_MENU_ENABLED_COLOR);
        UIManager.put("CheckBoxMenuItem.disabledForeground", VitcoSettings.MAIN_MENU_DISABLED_COLOR);
        UIManager.put("CheckBoxMenuItem.foreground", VitcoSettings.MAIN_MENU_ENABLED_COLOR);
        UIManager.put("CheckBoxMenuItem.selectionForeground", VitcoSettings.MAIN_MENU_ENABLED_COLOR);
        UIManager.put("CheckBoxMenuItem.background", VitcoSettings.MAIN_MENU_BACKGROUND);
        UIManager.put("CheckBoxMenuItem.disabledBackground", VitcoSettings.MAIN_MENU_BACKGROUND);
        UIManager.put("CheckBoxMenuItem.checkIcon", new SaveResourceLoader(
                "resource/img/icons/checkmark.png"
        ).asIconImage());


        // --set tabbed pane settings
        // outline color of tabs ("none")
        UIManager.put("JideTabbedPane.highlight", VitcoSettings.DEFAULT_BG_COLOR);
        // the border of the tabs
        UIManager.put("JideTabbedPane.tabInsets", new Insets(0, 3, 0, 3));
        UIManager.put("JideTabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        // the shape of the tab headers
        UIManager.put("JideTabbedPane.defaultTabShape", JideTabbedPane.SHAPE_BOX);

        // set separator color
        UIManager.put("CommandBarSeparator.foreground", VitcoSettings.DEFAULT_BORDER_COLOR);

        // set the table cell outline
        UIManager.put("Table.gridColor", VitcoSettings.DEFAULT_BORDER_COLOR);

        // prevent icon from floating (hover)
        UIManager.put("Icon.floating", false);

        // make title bar "thin"
        UIManager.put("DockableFrameTitlePane.margin", new Insets(0, 5, 0, 3));
        UIManager.put("DockableFrame.titleBorder", BorderFactory.createEmptyBorder());
        UIManager.put("DockableFrameTitlePane.buttonGap", 4);

        // set margin
        UIManager.put("JideButton.margin", new Insets(3,3,3,3));

        // set buttons alignment (required for OSX)
        UIManager.put("DockableFrameTitlePane.buttonsAlignment", SwingConstants.TRAILING);
        UIManager.put("DockableFrameTitlePane.titleAlignment", SwingConstants.LEADING);

        // set icons for title bar
        UIManager.put("DockableFrameTitlePane.hideIcon", new SaveResourceLoader(
                "resource/img/icons/frame_close_button_icon.png"
        ).asIconImage());
        UIManager.put("DockableFrameTitlePane.unfloatIcon", new SaveResourceLoader(
                "resource/img/icons/frame_maximize_button_icon.png"
        ).asIconImage());
        UIManager.put("DockableFrameTitlePane.floatIcon", new SaveResourceLoader(
                "resource/img/icons/frame_maximize_button_icon.png"
        ).asIconImage());
        UIManager.put("DockableFrameTitlePane.helpIcon", new SaveResourceLoader(
                "resource/img/icons/frame_help_button_icon.png"
        ).asIconImage());

        // allow cursor changes
        UIManager.put("DockingFramework.changeCursor", true);

        // set custom painter
        UIManager.getDefaults().put("Theme.painter", new ButtonLayoutPainter());

    }
}
