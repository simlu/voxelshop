package com.vitco.util.colors;

import com.vitco.util.ColorTools;
import com.vitco.util.colors.basics.*;
import com.vitco.util.colors.basics.components.ColorSliderPrototype;
import com.vitco.util.colors.basics.components.SliderUI;
import com.vitco.util.colors.basics.components.ValueChangeListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A color chooser that uses a 2D plane to pick saturation and brightness and a slider
 * to pick the hue. Dynamic in size (uses fast repaint).
 */
public class HSBPanelSliderChooser extends ColorChooserPrototype {

    // how fine the slider is
    private static final int SLIDER_STEPS = 1000;

    // hue slider
    private static class VerticalHueSlider extends ColorSliderPrototype {

        private Point prevContentRect = new Point(0, 0);
        private BufferedImage bgBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        @Override
        protected void drawBackground(Graphics2D g, SliderUI sliderUI) {
            // only generate background on resize and when color changes
            if (prevContentRect.x != sliderUI.getContentRect().width || prevContentRect.y != sliderUI.getContentRect().height) {

                prevContentRect = new Point(sliderUI.getContentRect().width, sliderUI.getContentRect().height);
                int w = sliderUI.getSlider().getWidth();
                int h = sliderUI.getSlider().getHeight();
                bgBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D ig = (Graphics2D) bgBuffer.getGraphics();

                // leave some free for the slider
                for (int y = 0; y < h; y++) {
                    ig.setColor(ColorTools.hsbToColor(new float[] {
                            (float)sliderUI.getValueForYPosition(y)/ sliderUI.getSlider().getMaximum(), 1, 1}));
                    ig.drawLine(0, y, w - 1, y);
                }
            }

            // draw the background
            g.drawImage(bgBuffer, 0, 0, null);
        }

        // constructor
        public VerticalHueSlider(int min, int max, int current) {
            super(JSlider.VERTICAL, min, max, current);
        }
    }

    // the instances
    private final SatBrightPanelChooser colorPanel = new SatBrightPanelChooser();
    private final VerticalHueSlider hueSlider = new VerticalHueSlider(0, SLIDER_STEPS, 0);

    public HSBPanelSliderChooser() {
        setLayout(new BorderLayout());

        // register hue slider event
        hueSlider.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void onChange(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                if (!source.getValueIsAdjusting()) {
                    hsb = new float[] {(float)source.getValue()/SLIDER_STEPS, hsb[1], hsb[2]};
                    colorPanel.setColor(hsb);
                    colorPanel.repaint();
                    notifyListeners(hsb);
                }
            }
        });

        // register panel change event
        colorPanel.addColorChangeListener(new ColorChangeListener() {
            @Override
            public void colorChanged(float[] hsb) {
                notifyListeners(hsb);
            }
        });

        // set hue slider layout
        final int sliderWidth = 25;
        hueSlider.setPreferredSize(new Dimension(sliderWidth, hueSlider.getPreferredSize().height));
        // generate the custom thumb
        {
            final BufferedImage thumbBuffer = new BufferedImage(sliderWidth - 1, 11, BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig = (Graphics2D)thumbBuffer.getGraphics();
            // Anti-alias
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            ig.setColor(Color.BLACK);
            ig.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
            ig.drawRect(1, 1, sliderWidth - 3, 8);
            ig.setColor(Color.WHITE);
            ig.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
            ig.drawRect(1, 1, sliderWidth - 3, 8);

            hueSlider.setThumb(thumbBuffer);
        }

        // set the borders
        colorPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Settings.BORDER_COLOR));
        hueSlider.setBorder(BorderFactory.createLineBorder(Settings.BORDER_COLOR));

        // add to layout
        add(colorPanel, BorderLayout.CENTER);
        add(hueSlider, BorderLayout.EAST);
    }

    // the current color of this chooser
    private float[] hsb = new float[3];

    // set the current color
    public void setColor(float[] hsb) {
        if (this.hsb[0] != hsb[0] || this.hsb[1] != hsb[1] || this.hsb[2] != hsb[2]) {
            this.hsb = hsb.clone();
            // update the panel
            colorPanel.setColor(hsb);
            colorPanel.repaint();
            // set the value for the slider
            hueSlider.setValueWithoutRefresh(Math.round(hsb[0]*SLIDER_STEPS));
        }
    }
}
