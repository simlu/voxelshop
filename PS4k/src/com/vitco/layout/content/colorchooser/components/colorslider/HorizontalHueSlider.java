package com.vitco.layout.content.colorchooser.components.colorslider;

import com.vitco.layout.content.colorchooser.basic.Settings;
import com.vitco.layout.content.colorchooser.components.ColorSliderPrototype;
import com.vitco.layout.content.colorchooser.components.SliderUI;
import com.vitco.util.misc.ColorTools;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * hue slider
 */
class HorizontalHueSlider extends ColorSliderPrototype {

    private Point prevContentRect = new Point(0, 0);
    private BufferedImage bgBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    @Override
    protected void drawBackground(Graphics2D g, SliderUI sliderUI) {
        // only generate background on resize and when color changes
        if (prevContentRect.x != sliderUI.getContentRect().width
                || prevContentRect.y != sliderUI.getContentRect().height) {

            prevContentRect = new Point(sliderUI.getContentRect().width, sliderUI.getContentRect().height);
            int w = sliderUI.getSlider().getWidth();
            int h = sliderUI.getSlider().getHeight();
            bgBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D ig = (Graphics2D) bgBuffer.getGraphics();

            ig.setColor(Settings.BG_COLOR);
            ig.fillRect(0, 0, w, h);

            // leave some free for the slider
            for (int x = 0; x < w - 1; x++) {
                ig.setColor(ColorTools.hsbToColor(new float[] {(float)sliderUI.getValueForXPosition(x)/ sliderUI.getSlider().getMaximum(), 1, 1}));
                ig.drawLine(x, 1, x, h - 11);
            }
            ig.setColor(Settings.SLIDER_BORDER_COLOR);
            ig.drawRect(0, 0, w - 1, h - 10);
            ig.dispose();
        }

        // draw the background
        g.drawImage(bgBuffer, 0, 0, null);
    }

    // constructor
    public HorizontalHueSlider(int min, int max, int current) {
        super(JSlider.HORIZONTAL, min, max, current);
    }
}
