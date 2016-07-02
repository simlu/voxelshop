package com.vitco.layout.frames.custom;

import com.jidesoft.swing.JideButton;

import javax.swing.*;
import java.awt.*;

public class FrameGenericJideButton extends JideButton {

    public void setIcon(String icon) {
        super.setIcon((Icon) UIManager.get(icon));
    }

    public FrameGenericJideButton(String icon, String tooltip) {
        super();
        this.setIcon(icon);
        this.setToolTipText(icon);
        this.setMargin(new Insets(5, 10, 5, 10));
        this.setContentAreaFilled(true);
        this.setFocusable(false);
        this.setToolTipText(tooltip);
    }
}
