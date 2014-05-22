package com.vitco.layout;

import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideSplitButton;

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

    private static final Color BUTTON_BACKGROUND_DEFAULT = new Color(83, 83, 83);
    private static final Color BUTTON_BACKGROUND_ROLLOVER = new Color(98, 98, 98);
    private static final Color BUTTON_BACKGROUND_SELECTED = new Color(55, 55, 55);
    private static final Color BUTTON_BACKGROUND_DISABLED_SELECTED = new Color(83, 83, 83);
    private static final Color BUTTON_BACKGROUND_PRESSED = new Color(83, 83, 83);

    private static final Color BUTTON_BORDER_COLOR = new Color(39, 39, 39);

    private static final int BUTTON_CORNER_ARC_SIZE = 4;

    @Override
    public void paintButtonBackground(JComponent c, Graphics g, Rectangle rect, int orientation, int state, boolean showBorder) {
        switch (state) {
            case STATE_DEFAULT:
                paintBackground(c, g, rect, showBorder ? BUTTON_BORDER_COLOR : null, BUTTON_BACKGROUND_DEFAULT, orientation);
                break;
            case STATE_ROLLOVER:
                paintBackground(c, g, rect, showBorder ? BUTTON_BORDER_COLOR : null, BUTTON_BACKGROUND_ROLLOVER, orientation);
                break;
            case STATE_SELECTED:
                paintBackground(c, g, rect, showBorder ? BUTTON_BORDER_COLOR : null, BUTTON_BACKGROUND_SELECTED, orientation);
                break;
            case STATE_DISABLE_SELECTED:
                paintBackground(c, g, rect, showBorder ? BUTTON_BORDER_COLOR : null, BUTTON_BACKGROUND_DISABLED_SELECTED, orientation);
                break;
            case STATE_PRESSED:
                paintBackground(c, g, rect, showBorder ? BUTTON_BORDER_COLOR : null, BUTTON_BACKGROUND_PRESSED, orientation);
                break;
        }
    }

    @Override
    protected void paintBackground(JComponent c, Graphics g, Rectangle rect, Color borderColor, Color background, int orientation) {
        Color oldColor = g.getColor();
        if (borderColor != null) {
            boolean paintDefaultBorder = true;
            Object o = c.getClientProperty("JideButton.paintDefaultBorder");
            if (o instanceof Boolean) {
                paintDefaultBorder = (Boolean) o;
            }
            if (paintDefaultBorder) {
                // draw gradient inside
                g.setColor(background);
                ((Graphics2D)g).setPaint(new GradientPaint(
                        (c instanceof JideSplitButton) ? rect.x -10 : 0,
                        !(c instanceof JideSplitButton) ? rect.y -10 : 0,
                        background.brighter(),
                        (c instanceof JideSplitButton) ? rect.width+10 : 0,
                        !(c instanceof JideSplitButton) ? rect.height+10 : 0,
                        background
                ));
                g.fillRoundRect(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2, BUTTON_CORNER_ARC_SIZE, BUTTON_CORNER_ARC_SIZE);
                ((Graphics2D)g).setPaint(null);
                // draw outline
                rect = new Rectangle(rect);
                rect.grow(-1, -1);
                g.setColor(borderColor);
                Object position = c.getClientProperty(JideButton.CLIENT_PROPERTY_SEGMENT_POSITION);
                if (position == null || JideButton.SEGMENT_POSITION_ONLY.equals(position)) {
                    g.drawRoundRect(rect.x, rect.y, rect.width - 1, rect.height - 1, BUTTON_CORNER_ARC_SIZE, BUTTON_CORNER_ARC_SIZE);
                }
                else if (JideButton.SEGMENT_POSITION_FIRST.equals(position)) {
                    if (orientation == SwingConstants.HORIZONTAL) {
                        g.drawRoundRect(rect.x, rect.y, rect.width, rect.height - 1, BUTTON_CORNER_ARC_SIZE, BUTTON_CORNER_ARC_SIZE);
                    }
                    else {
                        g.drawRoundRect(rect.x, rect.y, rect.width - 1, rect.height, BUTTON_CORNER_ARC_SIZE, BUTTON_CORNER_ARC_SIZE);
                    }
                }
                else if (JideButton.SEGMENT_POSITION_MIDDLE.equals(position)) {
                    if (orientation == SwingConstants.HORIZONTAL) {
                        g.drawRoundRect(rect.x, rect.y, rect.width, rect.height - 1, BUTTON_CORNER_ARC_SIZE, BUTTON_CORNER_ARC_SIZE);
                    }
                    else {
                        g.drawRoundRect(rect.x, rect.y, rect.width - 1, rect.height, BUTTON_CORNER_ARC_SIZE, BUTTON_CORNER_ARC_SIZE);
                    }
                }
                else if (JideButton.SEGMENT_POSITION_LAST.equals(position)) {
                    if (orientation == SwingConstants.HORIZONTAL) {
                        g.drawRoundRect(rect.x, rect.y, rect.width - 1, rect.height - 1, BUTTON_CORNER_ARC_SIZE, BUTTON_CORNER_ARC_SIZE);
                    }
                    else {
                        g.drawRoundRect(rect.x, rect.y, rect.width - 1, rect.height - 1, BUTTON_CORNER_ARC_SIZE, BUTTON_CORNER_ARC_SIZE);
                    }
                }
            } else {
                g.setColor(background);
                g.fillRoundRect(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2, BUTTON_CORNER_ARC_SIZE, BUTTON_CORNER_ARC_SIZE);
            }
        }
        else {
            g.setColor(background);
            g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, BUTTON_CORNER_ARC_SIZE, BUTTON_CORNER_ARC_SIZE);
        }
        g.setColor(oldColor);
    }
}
