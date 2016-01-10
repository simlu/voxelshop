package com.vitco.util.components.button;

import com.jidesoft.swing.JideLabel;
import com.vitco.settings.VitcoSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Designed to be displayed inside of frames.
 */
public abstract class FrameButton extends JideLabel {

    private final FrameButton thisButton = this;

    public abstract void onClick();

    public FrameButton() {
        this.setEnabled(true);
        this.setOpaque(true);
        this.setHorizontalAlignment(SwingConstants.CENTER);
        this.setVerticalAlignment(SwingConstants.CENTER);
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (thisButton.isEnabled()) {
                    onClick();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                thisButton.setBackground(thisButton.isEnabled() ? VitcoSettings.BUTTON_ENABLED_OVER : VitcoSettings.TEXTURE_WINDOW_BG_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                thisButton.setBackground(thisButton.isEnabled() ? VitcoSettings.BUTTON_ENABLED : VitcoSettings.TEXTURE_WINDOW_BG_COLOR);
            }
        });
    }

    @Override
    public void setEnabled(boolean flag) {
        super.setEnabled(flag);
        this.setBackground(flag ? VitcoSettings.BUTTON_ENABLED : VitcoSettings.TEXTURE_WINDOW_BG_COLOR);
        this.setForeground(flag ? Color.LIGHT_GRAY : VitcoSettings.TEXTURE_WINDOW_BG_COLOR);
    }
}
