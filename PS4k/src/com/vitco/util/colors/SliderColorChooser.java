package com.vitco.util.colors;

import com.jidesoft.swing.JideTabbedPane;
import com.vitco.util.ColorTools;
import com.vitco.util.colors.basics.ColorChooserPrototype;
import com.vitco.util.colors.basics.Settings;
import com.vitco.util.colors.basics.components.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;

/**
 * Advanced color chooser that uses tabs to display different
 * ways of altering the color.
 */
public class SliderColorChooser extends ColorChooserPrototype {

    // custom slider
    private static class HorizontalColorSlider extends ColorSliderPrototype {

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
            }

            // draw the background
            g.drawImage(bgBuffer, 0, 0, null);
        }

        // constructor
        public HorizontalColorSlider(int min, int max, int current) {
            super(JSlider.HORIZONTAL, min, max, current);
        }
    }

    // hue slider
    private static class HorizontalHueSlider extends ColorSliderPrototype {

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
            }

            // draw the background
            g.drawImage(bgBuffer, 0, 0, null);
        }

        // constructor
        public HorizontalHueSlider(int min, int max, int current) {
            super(JSlider.HORIZONTAL, min, max, current);
        }
    }

    // ======================

    // tab prototype
    private abstract class TabPrototype extends JPanel {
        private ColorSliderPrototype[] sliders;
        private NumberBox[] fields;

        protected abstract void onSliderChange(int id, ChangeEvent e);
        protected abstract void onTextFieldChange(int id, NumberBox source);
        protected abstract void refreshUI();
        protected abstract void notifyColorChange(Color color);

        private boolean hasChanged = false;
        protected void update(Color newColor, boolean externalChange, boolean publishIfChanged) {
            boolean changed = !color.equals(newColor);
            if (changed || externalChange) {
                // set the color
                color = newColor;
                if (changed) {
                    hasChanged = true;
                }
                if (externalChange) {
                    notifyColorChange(newColor);
                }
                refreshUI();
            }
            // notify the listeners
            if (hasChanged && publishIfChanged) {
                hasChanged = false;
                notifyListeners(ColorTools.colorToHSB(color));
            }
        }

        // update displayed values
        protected final void setValues(int[] values) {
            for (int i = 0; i < values.length; i++) {
                sliders[i].setValueWithoutRefresh(values[i]);
                fields[i].setValueWithoutRefresh(values[i]);
            }
        }

        protected final void init(String[] values, ColorSliderPrototype[] sliders, final NumberBox[] fields) {
            // store internal
            this.sliders = sliders;
            this.fields = fields;

            // update color when this component is shown
            addHierarchyListener(new HierarchyListener() {
                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) == HierarchyEvent.SHOWING_CHANGED) {
                        if (isShowing()) {
                            update(color, true, false);
                        }
                    }
                }
            });

            // register slider events
            for (int id = 0; id < sliders.length; id++) {
                final int finalId = id;
                sliders[id].addValueChangeListener(new ValueChangeListener() {
                    @Override
                    public void onChange(ChangeEvent e) {
                        onSliderChange(finalId, e);
                    }
                });
            }

            // register textfield events
            for (int id = 0; id < fields.length; id++) {
                final int finalId = id;
                fields[id].addTextChangeListener(new TextChangeListener() {
                    @Override
                    public void onChange() {
                        onTextFieldChange(finalId, fields[finalId]);
                    }
                });
            }

            // construct the layout
            setLayout(new GridBagLayout());
            setBackground(Settings.BG_COLOR);
            final GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(3,3,3,3);
            c.fill = GridBagConstraints.BOTH;

            // labels
            c.gridx = 0;
            c.gridy = 0;
            for (String value : values) {
                JLabel label = new JLabel(value);
                label.setForeground(Settings.TEXT_COLOR);
                add(label, c);
                c.gridy++;
            }

            // slider
            c.gridx = 1;
            c.gridy = 0;
            for (ColorSliderPrototype slider : sliders) {
                slider.setPreferredSize(new Dimension(150, 20));
                add(slider, c);
                c.gridy++;
            }

            // text fields
            c.gridx = 2;
            c.gridy = 0;
            for (NumberBox field : fields) {
                add(field, c);
                c.gridy++;
            }
        }
    }

    // the rgb chooser
    private final RGBTab rgbTab = new RGBTab();
    private class RGBTab extends TabPrototype {

        // the sliders
        private final HorizontalColorSlider rSlider = new HorizontalColorSlider(0, 255, 0);
        private final HorizontalColorSlider gSlider = new HorizontalColorSlider(0, 255, 0);
        private final HorizontalColorSlider bSlider = new HorizontalColorSlider(0, 255, 0);

        // the number boxes
        private final NumberBox rBox = new NumberBox(0, 255, 0);
        private final NumberBox gBox = new NumberBox(0, 255, 0);
        private final NumberBox bBox = new NumberBox(0, 255, 0);

        public RGBTab() {
            init(
                    new String[]{"R", "G", "B"},
                    new ColorSliderPrototype[]{rSlider, gSlider, bSlider},
                    new NumberBox[]{rBox, gBox, bBox}
            );
        }

        @Override
        protected void onSliderChange(int id, ChangeEvent e) {
            JSlider source = ((JSlider) e.getSource());
            update(new Color(
                    id == 0 ? source.getValue() : color.getRed(),
                    id == 1 ? source.getValue() : color.getGreen(),
                    id == 2 ? source.getValue() : color.getBlue()),
                    false, !source.getValueIsAdjusting());
        }

        @Override
        protected void onTextFieldChange(int id, NumberBox source) {
            update(new Color(
                    id == 0 ? source.getValue() : color.getRed(),
                    id == 1 ? source.getValue() : color.getGreen(),
                    id == 2 ? source.getValue() : color.getBlue()),
                    false, true);
        }

        @Override
        protected void refreshUI() {
            // repaint the slider
            rSlider.setLeftColor(new Color(0, color.getGreen(), color.getBlue()));
            rSlider.setRightColor(new Color(255, color.getGreen(), color.getBlue()));
            rSlider.repaint();

            gSlider.setLeftColor(new Color(color.getRed(), 0, color.getBlue()));
            gSlider.setRightColor(new Color(color.getRed(), 255, color.getBlue()));
            gSlider.repaint();

            bSlider.setLeftColor(new Color(color.getRed(), color.getGreen(), 0));
            bSlider.setRightColor(new Color(color.getRed(), color.getGreen(), 255));
            bSlider.repaint();

            // set the values
            setValues(new int[] {color.getRed(), color.getGreen(), color.getBlue()});
        }

        @Override
        protected void notifyColorChange(Color color) {
            // nothing to do here
        }
    }

    // the hsb chooser
    private final HSBTab hsbTab = new HSBTab();
    private class HSBTab extends TabPrototype {

        private final static int HUE_STEPCOUNT = 360;
        private final static int STEPCOUNT = 100;

        // the sliders
        private final SliderColorChooser.HorizontalHueSlider hSlider = new SliderColorChooser.HorizontalHueSlider(0, HUE_STEPCOUNT, 0);
        private final SliderColorChooser.HorizontalColorSlider sSlider = new SliderColorChooser.HorizontalColorSlider(0, STEPCOUNT, 0);
        private final SliderColorChooser.HorizontalColorSlider bSlider = new SliderColorChooser.HorizontalColorSlider(0, STEPCOUNT, 0);

        // the number boxes
        private final NumberBox hBox = new NumberBox(0, HUE_STEPCOUNT, 0);
        private final NumberBox sBox = new NumberBox(0, STEPCOUNT, 0);
        private final NumberBox bBox = new NumberBox(0, STEPCOUNT, 0);

        public HSBTab() {
            init(
                    new String[]{"H", "S", "B"},
                    new ColorSliderPrototype[]{hSlider, sSlider, bSlider},
                    new NumberBox[]{hBox, sBox, bBox}
            );
        }

        @Override
        protected void onSliderChange(int id, ChangeEvent e) {
            JSlider source = ((JSlider) e.getSource());
            hsb = new float[]{
                    id == 0 ? (float) source.getValue() / HUE_STEPCOUNT : hsb[0],
                    id == 1 ? (float) source.getValue() / STEPCOUNT : hsb[1],
                    id == 2 ? (float) source.getValue() / STEPCOUNT : hsb[2]};
            update(ColorTools.hsbToColor(hsb), false, !source.getValueIsAdjusting());
        }

        @Override
        protected void onTextFieldChange(int id, NumberBox source) {
            hsb = new float[]{
                    id == 0 ? (float) source.getValue() / HUE_STEPCOUNT : hsb[0],
                    id == 1 ? (float) source.getValue() / STEPCOUNT : hsb[1],
                    id == 2 ? (float) source.getValue() / STEPCOUNT : hsb[2]};
            update(ColorTools.hsbToColor(hsb), false, true);
        }

        @Override
        protected void refreshUI() {
            sSlider.setLeftColor(ColorTools.hsbToColor(new float[] {hsb[0], 0, hsb[2]}));
            sSlider.setRightColor(ColorTools.hsbToColor(new float[] {hsb[0], 1, hsb[2]}));
            sSlider.repaint();

            bSlider.setLeftColor(ColorTools.hsbToColor(new float[] {hsb[0], hsb[1], 0}));
            bSlider.setRightColor(ColorTools.hsbToColor(new float[] {hsb[0], hsb[1], 1}));
            bSlider.repaint();

            // set the values
            setValues(new int[] {
                    Math.round(hsb[0] * HUE_STEPCOUNT),
                    Math.round(hsb[1] * STEPCOUNT),
                    Math.round(hsb[2] * STEPCOUNT)
            });
        }

        // internal variable
        private float[] hsb = new float[3];

        @Override
        protected void notifyColorChange(Color color) {
            hsb = ColorTools.colorToHSB(color);
        }
    }

    private final CMYKTab cmykTab = new CMYKTab();
    private class CMYKTab extends TabPrototype {

        private final static int STEPCOUNT = 100;

        // the sliders
        private final SliderColorChooser.HorizontalColorSlider cSlider = new SliderColorChooser.HorizontalColorSlider(0, STEPCOUNT, 0);
        private final SliderColorChooser.HorizontalColorSlider mSlider = new SliderColorChooser.HorizontalColorSlider(0, STEPCOUNT, 0);
        private final SliderColorChooser.HorizontalColorSlider ySlider = new SliderColorChooser.HorizontalColorSlider(0, STEPCOUNT, 0);
        private final SliderColorChooser.HorizontalColorSlider kSlider = new SliderColorChooser.HorizontalColorSlider(0, STEPCOUNT, 0);

        // the number boxes
        private final NumberBox cBox = new NumberBox(0, STEPCOUNT, 0);
        private final NumberBox mBox = new NumberBox(0, STEPCOUNT, 0);
        private final NumberBox yBox = new NumberBox(0, STEPCOUNT, 0);
        private final NumberBox kBox = new NumberBox(0, STEPCOUNT, 0);

        public CMYKTab() {
            init(
                    new String[]{"C", "M", "Y", "K"},
                    new ColorSliderPrototype[]{cSlider, mSlider, ySlider, kSlider},
                    new NumberBox[]{cBox, mBox, yBox, kBox}
            );
        }

        @Override
        protected void onSliderChange(int id, ChangeEvent e) {
            JSlider source = ((JSlider) e.getSource());
            cmyk = new float[]{
                    id == 0 ? (float) source.getValue() / STEPCOUNT : cmyk[0],
                    id == 1 ? (float) source.getValue() / STEPCOUNT : cmyk[1],
                    id == 2 ? (float) source.getValue() / STEPCOUNT : cmyk[2],
                    id == 3 ? (float) source.getValue() / STEPCOUNT : cmyk[3]};
            update(ColorTools.cmykToColor(cmyk), false, !source.getValueIsAdjusting());
        }

        @Override
        protected void onTextFieldChange(int id, NumberBox source) {
            cmyk = new float[]{
                    id == 0 ? (float) source.getValue() / STEPCOUNT : cmyk[0],
                    id == 1 ? (float) source.getValue() / STEPCOUNT : cmyk[1],
                    id == 2 ? (float) source.getValue() / STEPCOUNT : cmyk[2],
                    id == 3 ? (float) source.getValue() / STEPCOUNT : cmyk[3]};
            update(ColorTools.cmykToColor(cmyk), false, true);
        }

        @Override
        protected void refreshUI() {
            cSlider.setLeftColor(ColorTools.cmykToColor(new float[]{0, cmyk[1], cmyk[2], cmyk[3]}));
            cSlider.setRightColor(ColorTools.cmykToColor(new float[]{1, cmyk[1], cmyk[2], cmyk[3]}));
            cSlider.repaint();

            mSlider.setLeftColor(ColorTools.cmykToColor(new float[]{cmyk[0], 0, cmyk[2], cmyk[3]}));
            mSlider.setRightColor(ColorTools.cmykToColor(new float[]{cmyk[0], 1, cmyk[2], cmyk[3]}));
            mSlider.repaint();

            ySlider.setLeftColor(ColorTools.cmykToColor(new float[]{cmyk[0], cmyk[1], 0, cmyk[3]}));
            ySlider.setRightColor(ColorTools.cmykToColor(new float[]{cmyk[0], cmyk[1], 1, cmyk[3]}));
            ySlider.repaint();

            kSlider.setLeftColor(ColorTools.cmykToColor(new float[]{cmyk[0], cmyk[1], cmyk[2], 0}));
            kSlider.setRightColor(ColorTools.cmykToColor(new float[]{cmyk[0], cmyk[1], cmyk[2], 1}));
            kSlider.repaint();

            // set the values
            setValues(new int[] {
                    Math.round(cmyk[0] * STEPCOUNT),
                    Math.round(cmyk[1] * STEPCOUNT),
                    Math.round(cmyk[2] * STEPCOUNT),
                    Math.round(cmyk[3] * STEPCOUNT)
            });
        }

        // internal variable
        private float[] cmyk = new float[4];

        @Override
        protected void notifyColorChange(Color color) {
            cmyk = ColorTools.colorToCMYK(color);
        }
    }

    // the tabbed pane
    private final JideTabbedPane tabbedPane = new JideTabbedPane(JTabbedPane.RIGHT, JideTabbedPane.SCROLL_TAB_LAYOUT);

    // ======================

    // get the active tab
    public final int getActiveTab() {
        return tabbedPane.getSelectedIndex();
    }

    // set the active tab
    public final void setActiveTab(int selectedIndex) {
        if (tabbedPane.getTabCount() > selectedIndex && selectedIndex >= 0) {
            tabbedPane.setSelectedIndex(selectedIndex);
        }
    }

    // set the color that is currently displayed
    private Color color = Color.WHITE;
    public final void setColor(float[] hsb) {
        Color color = ColorTools.hsbToColor(hsb);
        if (!this.color.equals(color)) {
            if (rgbTab.isShowing()) {
                rgbTab.update(color, true, false);
            }
            if (hsbTab.isShowing()) {
                hsbTab.update(color, true, false);
            }
            if (cmykTab.isShowing()) {
                cmykTab.update(color, true, false);
            }
            // none of the above might be visible
            this.color = color;
        }
    }

    // constructor
    public SliderColorChooser() {

        // set up the tabbed pane
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder());
        setBackground(new Color(0, 0, 0, 0));

        // add the tabs
        JScrollPane RGBscrollPane = new JScrollPane(rgbTab);
        RGBscrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        RGBscrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        tabbedPane.addTab("RGB", RGBscrollPane);

        JScrollPane HSBscrollPane = new JScrollPane(hsbTab);
        HSBscrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        HSBscrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        tabbedPane.addTab("HSB", HSBscrollPane);

        JScrollPane CMYKscrollPane = new JScrollPane(cmykTab);
        CMYKscrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        CMYKscrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        tabbedPane.addTab("CMYK", CMYKscrollPane);

        // disable focus for tabs themselves
        tabbedPane.setFocusable(false);

        tabbedPane.setTabShape(JideTabbedPane.SHAPE_ROUNDED_FLAT); // make square
        tabbedPane.setTabResizeMode(JideTabbedPane.RESIZE_MODE_FIT); // fit them all

        add(tabbedPane, BorderLayout.CENTER);
    }
}
