package com.vitco.layout;

import com.jidesoft.swing.JideMenu;
import com.jidesoft.swing.JideSplitButton;
import com.vitco.settings.VitcoSettings;

import javax.swing.*;
import java.awt.*;

/**
 * Custom painter class for a custom layout.
 *
 * Defines button behavior.
 *
 * todo: look at sample files
 * https://github.com/jidesoft/jide-oss/blob/master/src/com/jidesoft/plaf/basic/BasicPainter.java
 */
public class ButtonLayoutPainter extends CustomLayoutPainter {

    // arc size of the button corners
    private static final int BUTTON_CORNER_ARC_SIZE = 6;

    @Override
    public void paintGripper(javax.swing.JComponent c, java.awt.Graphics g, java.awt.Rectangle rect, int orientation, int state) {
        g.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
        // paint "gripper"
        g.setColor(VitcoSettings.SOFT_BLACK);
        if (orientation == HORIZONTAL) {
            for (int i = rect.y + 3; i < rect.height - 2; i++) {
                if (i%2 == 0) {
                    g.drawLine(rect.x+2, i, rect.x + 5, i);
                }
            }
        } else {
            for (int i = rect.x + 3; i < rect.width - 2; i++) {
                if (i%2 == 0) {
                    g.drawLine(i, rect.y+2, i, rect.y+5);
                }
            }
        }
    }

    @Override
    public void paintCommandBarBackground(javax.swing.JComponent c, java.awt.Graphics g, java.awt.Rectangle rect, int orientation, int state) {
        g.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
        g.setColor(VitcoSettings.DEFAULT_BORDER_COLOR);
        g.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
    }

    @Override
    public void paintButtonBackground(JComponent c, Graphics g, Rectangle rect, int orientation, int state, boolean showBorder) {
        switch (state) {
            case STATE_DEFAULT:
                if ((c instanceof JideMenu) && ((JideMenu)c).isPopupMenuVisible()) {
                    paintBackground(c, g, rect, VitcoSettings.BUTTON_BORDER_COLOR, VitcoSettings.BUTTON_BACKGROUND_SELECTED, orientation);
                } else {
                    paintBackground(c, g, rect, showBorder ? VitcoSettings.BUTTON_BORDER_COLOR : null, VitcoSettings.BUTTON_BACKGROUND_DEFAULT, orientation);
                }
                break;
            case STATE_ROLLOVER:
                paintBackground(c, g, rect, showBorder ? VitcoSettings.BUTTON_BORDER_COLOR : null, VitcoSettings.BUTTON_BACKGROUND_ROLLOVER, orientation);
                break;
            case STATE_SELECTED:
                paintBackground(c, g, rect, showBorder ? VitcoSettings.BUTTON_BORDER_COLOR : null, VitcoSettings.BUTTON_BACKGROUND_SELECTED, orientation);
                break;
            case STATE_DISABLE_SELECTED:
                paintBackground(c, g, rect, showBorder ? VitcoSettings.BUTTON_BORDER_COLOR : null, VitcoSettings.BUTTON_BACKGROUND_DISABLED_SELECTED, orientation);
                break;
            case STATE_PRESSED:
                paintBackground(c, g, rect, showBorder ? VitcoSettings.BUTTON_BORDER_COLOR : null, VitcoSettings.BUTTON_BACKGROUND_PRESSED, orientation);
                break;
            case STATE_DISABLE:
                break;
            case STATE_DISABLE_ROLLOVER:
                break;
            case STATE_INACTIVE_ROLLOVER:
                break;
        }
    }

    @Override
    protected void paintBackground(JComponent c, Graphics g, Rectangle rect, Color borderColor, Color background, int orientation) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        Color oldColor = g2.getColor();
        if (borderColor != null) {
            boolean paintDefaultBorder = true;
            Object o = c.getClientProperty("JideButton.paintDefaultBorder");
            if (o instanceof Boolean) {
                paintDefaultBorder = (Boolean) o;
            }
            if (paintDefaultBorder) {
                boolean flipped = (c instanceof JideSplitButton) &&
                        ((JideSplitButton)c).getOrientation() != JideSplitButton.HORIZONTAL;
                // draw gradient inside
                g2.setColor(background);
                g2.setPaint(new GradientPaint(
                        flipped ? rect.x -50 : 0,
                        !flipped ? rect.y -50 : 0,
                        background.brighter(),
                        flipped ? rect.width : 0,
                        !flipped ? rect.height : 0,
                        background
                ));
                g2.fillRoundRect(rect.x + (flipped?1:0) + 1,
                        rect.y + (flipped?0:1) - (flipped?1:0) + 1,
                        rect.width - (flipped?1:0) - 2,
                        rect.height - (flipped?0:1) - 2,
                        BUTTON_CORNER_ARC_SIZE, BUTTON_CORNER_ARC_SIZE);
                g2.setPaint(null);
                // compute outline rect
                rect = new Rectangle(rect);
                rect.grow(-1, -1);
                // draw outline
                g2.setColor(background.brighter());
                g2.drawRoundRect(
                        rect.x + (flipped?1:0),
                        rect.y + (flipped?0:1) - (flipped?1:0),
                        rect.width - (flipped?1:0),
                        rect.height - (flipped?0:1),
                        BUTTON_CORNER_ARC_SIZE, BUTTON_CORNER_ARC_SIZE);
                g2.setColor(borderColor);
                g2.drawRoundRect(
                        rect.x,
                        rect.y - (flipped?1:0),
                        rect.width - (flipped?1:0),
                        rect.height - (flipped?0:1),
                        BUTTON_CORNER_ARC_SIZE, BUTTON_CORNER_ARC_SIZE);
            } else {
                g2.setColor(background);
                g2.fillRoundRect(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2, BUTTON_CORNER_ARC_SIZE, BUTTON_CORNER_ARC_SIZE);
            }
        }
        else {
            g2.setColor(background);
            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, BUTTON_CORNER_ARC_SIZE, BUTTON_CORNER_ARC_SIZE);
        }
        g2.setColor(oldColor);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
    }
}
