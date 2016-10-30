package com.vitco.layout.frames.custom;

import com.jidesoft.docking.DockableFrame;
import com.vitco.manager.lang.LangSelectorInterface;
import com.vitco.settings.VitcoSettings;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


class TitlePane extends JPanel {

    private final FrameGenericJideButton help;
    public final void setHelpAction(final AbstractAction action) {
        help.addActionListener(action);
    }

    JLabel title = new JLabel();
    public void setTitle(String title) {
        this.title.setText(title);
    }

    private final FrameGenericJideButton floating;
    public final void displayFloatButton(boolean flag) {
        floating.setVisible(flag);
    }

    public TitlePane(final DockableFrame frame, LangSelectorInterface langSelector) {
        // define layout
        this.setLayout(new BorderLayout());
        this.setBackground(VitcoSettings.DEFAULT_BG_COLOR);
        // define and set title layout
        title.setForeground(VitcoSettings.SOFT_WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        this.add(title, BorderLayout.WEST);
        // define and set button panel layout
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttons.setOpaque(false);
        this.add(buttons, BorderLayout.EAST);
        // define buttons layout
        help = new FrameGenericJideButton("DockableFrameTitlePane.helpIcon", langSelector.getString("help_frame_button_tooltip"));
        buttons.add(help);
        floating = new FrameGenericJideButton("DockableFrameTitlePane.floatIcon", langSelector.getString("toggle_floating_frame_button_tooltip"));
        buttons.add(floating);
        FrameGenericJideButton hide = new FrameGenericJideButton("DockableFrameTitlePane.hideIcon", langSelector.getString("close_frame_button_tooltip"));
        buttons.add(hide);
        // define actions
        floating.addActionListener(frame.getFloatingAction());
        hide.addActionListener(frame.getCloseAction());
    }

}


public class CDockableFrame extends DockableFrame {

    /* This implements a hacky custom title bar to ensure they look the same on all OS, especially OSX */

    public void setTitle(String title) {
        super.setTitle(title);
        titlePane.setTitle(title);
    }

    public final void setHelpAction(final AbstractAction action) {
        titlePane.setHelpAction(action);
    }

    @Override
    public boolean isShowTitleBar() {
        // always hide the real title bar
        return false;
    }

    @Override
    public void setShowTitleBar(boolean flag) {
        // we overwrite the setter
        titlePane.setVisible(flag);
    }

    /* Real getter */
    public boolean isShowTitleBarReal() {
        return titlePane.isVisible();
    }

    @Override
    public boolean isDraggingTarget(MouseEvent e) {
        // allows us to drag the frame with the fake title bar
        return titlePane.contains(e.getPoint());
    }

    private final TitlePane titlePane;
    private static final Border highlightedBorder = BorderFactory.createLineBorder(VitcoSettings.DEFAULT_BORDER_COLOR_HIGHLIGHTED);
    private static final Border defaultBorder = BorderFactory.createLineBorder(VitcoSettings.DEFAULT_BORDER_COLOR);
    private static final Border emptyBorder = BorderFactory.createEmptyBorder();

    @Override
    public void setBorder(Border ignored) {
        if (activeFrameHighlighted) {
            super.setBorder(this.isActive() ? highlightedBorder : defaultBorder);
        } else {
            super.setBorder(emptyBorder);
        }
    }

    // whether to highlight active windows or not
    private static boolean activeFrameHighlighted = true;
    public static void setActiveFrameHighlighted(boolean value) {
        if (activeFrameHighlighted != value) {
            activeFrameHighlighted = value;
        }
    }
    public static boolean isActiveFrameHighlighted() {
        return activeFrameHighlighted;
    }

    @Override
    public void setFloatable(boolean ignored) {
        super.setFloatable(framesFloatable);
        titlePane.displayFloatButton(framesFloatable);
    }

    private static boolean framesFloatable = false;
    public static void setFramesFloatable(boolean value) {
        if (framesFloatable != value) {
            framesFloatable = value;
        }
    }
    public static boolean isFramesFloatable() {
        return framesFloatable;
    }

    public CDockableFrame(String key, ImageIcon imageIcon, LangSelectorInterface langSelector) {
        super(key, imageIcon);

        // listen to changes that might affect the border of this frame
        this.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("active") || evt.getPropertyName().equals("docked") || evt.getPropertyName().equals("floated")) {
                    setBorder(null);
                }
            }
        });

        titlePane = new TitlePane(this, langSelector);
        // this assumes we don't have anything in the north of any frame (!)
        this.add(titlePane, BorderLayout.NORTH);
    }

}
