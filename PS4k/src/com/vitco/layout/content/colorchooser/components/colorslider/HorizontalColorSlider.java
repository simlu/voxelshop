package com.vitco.layout.content.colorchooser.components.colorslider;

import com.vitco.layout.content.colorchooser.basic.Settings;
import com.vitco.layout.content.colorchooser.components.ColorSliderPrototype;
import com.vitco.layout.content.colorchooser.components.SliderUI;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * custom slider
 */
class HorizontalColorSlider extends ColorSliderPrototype {

    // the left color of the slider
    private Color leftColor = Color.WHITE;
    public final void setLeftColor(Color color) {
        leftColor = color;
    }

    // the right color of the slider
    private Color rightColor = Color.BLACK;
    public final void setRightColor(Color color) {
        rightColor = color;
    }

    private BufferedImage bgBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    private Point prevContentRect = new Point(0, 0);
    private Color lastLeftColor = Color.BLACK;
    private Color lastRightColor = Color.WHITE;

    @Override
    protected final void drawBackground(Graphics2D g, SliderUI sliderUI) {
        boolean[] change = new boolean[] {
                prevContentRect.x == sliderUI.getContentRect().width
                        && prevContentRect.y == sliderUI.getContentRect().height,
                lastLeftColor.equals(leftColor) && lastRightColor.equals(rightColor)
        };
        // only generate background on resize and when color changes
        if (!change[0] || !change[1]) {

            if (!change[1]) {
                lastLeftColor = leftColor;
                lastRightColor = rightColor;
            }

            if (!change[0]) {
                prevContentRect = new Point(sliderUI.getContentRect().width, sliderUI.getContentRect().height);
                bgBuffer = new BufferedImage(
                        sliderUI.getSlider().getWidth(),
                        sliderUI.getSlider().getHeight(),
                        BufferedImage.TYPE_INT_RGB);
            }

            int w = bgBuffer.getWidth();
            int h = bgBuffer.getHeight();

            Graphics2D ig = (Graphics2D) bgBuffer.getGraphics();

            ig.setColor(Settings.BG_COLOR);
            ig.fillRect(0, 0, w, h);

            // leave some free for the slider
            ig.setPaint(new GradientPaint(sliderUI.getXPositionForValue(0), 0, leftColor, sliderUI.getXPositionForValue(255), 0, rightColor, false));
            ig.fillRect(1, 1, w - 2, h - 11);
            ig.setColor(Settings.SLIDER_BORDER_COLOR);
            ig.drawRect(0, 0, w - 1, h - 10);
            ig.dispose();
        }

        // draw the background
        g.drawImage(bgBuffer, 0, 0, null);
    }

    // constructor
    public HorizontalColorSlider(int min, int max, int current) {
        super(JSlider.HORIZONTAL, min, max, current);
    }
}
