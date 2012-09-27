package com.vitco.util.colors;

import com.jidesoft.swing.JideTabbedPane;
import com.vitco.util.ColorTools;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Advanced color chooser that uses tabs to display different
 * ways of altering the color.
 */
public class TabbedColorChooser extends ColorChooserPrototype {

    private static final Color TEXTAREA_BG_COLOR = new Color(60, 60, 60);
    private static final Color TEXTAREA_BG_COLOR_FOCUS = new Color(200, 200, 200);
    private static final Color TEXTAREA_TEXT_COLOR = new Color(255, 255, 255);
    private static final Color TEXTAREA_TEXT_COLOR_FOCUS = new Color(0, 0, 0);
    private static final Color TEXTAREA_BORDER_COLOR = new Color(50, 50, 50);
    private static final Color BG_COLOR = new Color(80, 80, 80);
    private static final Color TEXT_COLOR = new Color(255, 255, 255);
    private static final Color SLIDER_BORDER_COLOR = new Color(50, 50, 50);
    private static final Color SLIDER_KNOB_COLOR = new Color(158, 158, 158);
    private static final Color SLIDER_KNOB_OUTLINE_COLOR = new Color(42, 42, 42);

    // box that only allows numbers and has an easy way to retrieve the current one
    // also allows for notification listen (and change "onChange")
    private static class NumberBox extends JTextField {

        // make sure the value is only set when it changes
        public void setValue(int value) {
            int croppedValue = cropValue(value);
            if (getValue() != croppedValue) {
                super.setText(String.valueOf(croppedValue));
            }
        }

        // the current string used
        private String currentString = "";

        // holds the listeners
        private final ArrayList<TextChangeListener> listener = new ArrayList<NumberBox.TextChangeListener>();
        // the listener interface
        protected interface TextChangeListener {
            void onChange();
        }

        // add a listener
        public final void addTextChangeListener(TextChangeListener tcl) {
            listener.add(tcl);
        }

        // notify listeners
        private void notifyListeners() {
            for (TextChangeListener tcl : listener) {
                tcl.onChange();
            }
        }

        // filter to allow only numbers in textarea and notify on change
        // also remembers the current string
        private class AxisJTextFilter extends DocumentFilter {
            @Override
            public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException
            {
                StringBuilder sb = new StringBuilder();
                sb.append(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.insert(offset, text);
                if(invalidContent(sb.toString())) return;
                fb.insertString(offset, text, attr);
                currentString = sb.toString();
                notifyListeners();
            }

            @Override
            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException
            {
                StringBuilder sb = new StringBuilder();
                sb.append(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.replace(offset, offset + length, text);
                if(invalidContent(sb.toString())) return;
                fb.replace(offset, length, text, attr);
                currentString = sb.toString();
                notifyListeners();
            }

            @Override
            public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException
            {
                super.remove(fb, offset, length);
                currentString = fb.getDocument().getText(0, fb.getDocument().getLength());
                notifyListeners();
            }

            public boolean invalidContent(String text)
            {
                Pattern pattern = Pattern.compile("\\d{0,4}?");
                Matcher matcher = pattern.matcher(text);
                boolean isMatch = matcher.matches();
                return !text.equals("") && !isMatch;
            }
        }

        // link to this field
        private final NumberBox thisField = this;

        // conversion helper
        private int cropValue(int value)  {
            return Math.min(MAX,Math.max(MIN,value));
        }

        // get value (range 0-255)
        public final int getValue() {
            int result = 0;
            try {
                result = cropValue(Integer.valueOf(currentString));
            } catch (NumberFormatException ignored) {}
            return result;
        }

        private final int MIN;
        private final int MAX;

        public NumberBox(int min, int max, int current) {
            super(String.valueOf(current), 4);
            MIN = min;
            MAX = max;
            setForeground(TEXTAREA_TEXT_COLOR);
            setBackground(TEXTAREA_BG_COLOR);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(TEXTAREA_BORDER_COLOR),
                    BorderFactory.createEmptyBorder(0, 3, 0, 3)
            ));
            ((AbstractDocument)this.getDocument()).setDocumentFilter(new AxisJTextFilter());
            // handle highlight on focus
            this.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    setForeground(TEXTAREA_TEXT_COLOR_FOCUS);
                    setBackground(TEXTAREA_BG_COLOR_FOCUS);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    thisField.setText(String.valueOf(getValue()));
                    setForeground(TEXTAREA_TEXT_COLOR);
                    setBackground(TEXTAREA_BG_COLOR);
                }
            });
            // handle update on return (only visual)
            this.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == 10) {
                        thisField.setText(String.valueOf(getValue()));
                        // remove focus from this component
                        thisField.setFocusable(false);
                        thisField.setFocusable(true);
                    }
                }
            });
        }
    }

    // custom slider
    private class HorizontalColorSlider extends JSlider {

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

        // constructor
        public HorizontalColorSlider(int min, int max, int current) {
            super(JSlider.HORIZONTAL, min, max, current);
            setPreferredSize(new Dimension(150, 20));
            final BasicSliderUI sliderUI = new BasicSliderUI(this) {

                private BufferedImage bgBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
                private BufferedImage thumbBuffer = null;
                private Point prevContentRect = new Point(0, 0);
                private Color lastLeftColor = Color.BLACK;
                private Color lastRightColor = Color.WHITE;

                private final static int size = 5;

                @Override
                public void paint(Graphics g, JComponent c) {
                    super.paint(g, c);

                    // only generate background on resize and when color changes
                    if (prevContentRect.x != contentRect.width || prevContentRect.y != contentRect.height ||
                            !lastLeftColor.equals(leftColor) || !lastRightColor.equals(rightColor)) {

                        prevContentRect = new Point(contentRect.width, contentRect.height);
                        int w = slider.getWidth();
                        int h = slider.getHeight();
                        bgBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                        Graphics2D ig = (Graphics2D) bgBuffer.getGraphics();

                        ig.setColor(BG_COLOR);
                        ig.fillRect(0, 0, w, h);

                        // leave some free for the slider
                        ig.setPaint(new GradientPaint(xPositionForValue(0), 0, leftColor, xPositionForValue(255), 0, rightColor, false));
                        ig.fillRect(0, 0, w, h - 10);
                        ig.setColor(SLIDER_BORDER_COLOR);
                        ig.drawRect(0, 0, w, h - 10);
                    }

                    // draw the background
                    g.drawImage(bgBuffer, 0, 0, null);

                    if (g.getClipBounds().intersects(thumbRect)) {
                        // make sure the thumbRect covers the whole height
                        thumbRect.y = 0;
                        thumbRect.height = slider.getHeight();
                        // only create the thumb once
                        if (thumbBuffer == null) {
                            thumbBuffer = new BufferedImage(size*2 + 1, size*2 + 1, BufferedImage.TYPE_INT_ARGB);
                            Graphics2D ig = (Graphics2D) thumbBuffer.getGraphics();
                            // Anti-alias
                            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                            ig.setColor(SLIDER_KNOB_COLOR);
                            ig.fillPolygon(new int[] {1,size*2,size*2,size,1}, new int[] {size*2, size*2, size, 1, size}, 5);

                            ig.setColor(SLIDER_KNOB_OUTLINE_COLOR);
                            ig.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
                            ig.drawPolygon(new int[] {0,size*2,size*2,size,0}, new int[] {size*2, size*2, size, 0, size}, 5);
                        }
                        // draw the thumb
                        g.drawImage(thumbBuffer, xPositionForValue(slider.getValue()) - size, slider.getHeight()-size*2 - 1, null);
                    }
                }


            };
            // move the thumb to the position we pressed instantly
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    setValue(sliderUI.valueForXPosition(e.getX()));
                    repaint();
                }
            });
            // needs to go last
            setUI(sliderUI);
        }
    }

    // hue slider
    private class HorizontalHueSlider extends JSlider {

        private final int MAX;

        // constructor
        public HorizontalHueSlider(int min, int max, int current) {
            super(JSlider.HORIZONTAL, min, max, current);
            MAX = max;
            setPreferredSize(new Dimension(150, 20));
            final BasicSliderUI sliderUI = new BasicSliderUI(this) {

                private BufferedImage bgBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
                private BufferedImage thumbBuffer = null;
                private Point prevContentRect = new Point(0, 0);

                private final static int size = 5;

                @Override
                public void paint(Graphics g, JComponent c) {
                    super.paint(g, c);

                    // only generate background on resize and when color changes
                    if (prevContentRect.x != contentRect.width || prevContentRect.y != contentRect.height) {

                        prevContentRect = new Point(contentRect.width, contentRect.height);
                        int w = slider.getWidth();
                        int h = slider.getHeight();
                        bgBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                        Graphics2D ig = (Graphics2D) bgBuffer.getGraphics();

                        ig.setColor(BG_COLOR);
                        ig.fillRect(0, 0, w, h);

                        // leave some free for the slider
                        for (int x = 0; x < w; x++) {
                            ig.setColor(ColorTools.hsbToColor(new float[] {(float)valueForXPosition(x)/ MAX, 1, 1}));
                            ig.drawLine(x, 0, x, h - 10);
                        }
                        ig.setColor(SLIDER_BORDER_COLOR);
                        ig.drawRect(0, 0, w, h - 10);
                    }

                    // draw the background
                    g.drawImage(bgBuffer, 0, 0, null);

                    if (g.getClipBounds().intersects(thumbRect)) {
                        // make sure the thumbRect covers the whole height
                        thumbRect.y = 0;
                        thumbRect.height = slider.getHeight();
                        // only create the thumb once
                        if (thumbBuffer == null) {
                            thumbBuffer = new BufferedImage(size*2 + 1, size*2 + 1, BufferedImage.TYPE_INT_ARGB);
                            Graphics2D ig = (Graphics2D) thumbBuffer.getGraphics();
                            // Anti-alias
                            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                            ig.setColor(SLIDER_KNOB_COLOR);
                            ig.fillPolygon(new int[] {1,size*2,size*2,size,1}, new int[] {size*2, size*2, size, 1, size}, 5);

                            ig.setColor(SLIDER_KNOB_OUTLINE_COLOR);
                            ig.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
                            ig.drawPolygon(new int[] {0,size*2,size*2,size,0}, new int[] {size*2, size*2, size, 0, size}, 5);
                        }
                        // draw the thumb
                        g.drawImage(thumbBuffer, xPositionForValue(slider.getValue()) - size, slider.getHeight()-size*2 - 1, null);
                    }
                }


            };
            // move the thumb to the position we pressed instantly
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    setValue(sliderUI.valueForXPosition(e.getX()));
                    repaint();
                }
            });
            // needs to go last
            setUI(sliderUI);
        }
    }

    // set the color that is currently displayed
    private Color color = Color.WHITE;
    public final void setColor(float[] hsb) {
            Color color = ColorTools.hsbToColor(hsb);
            if (!this.color.equals(color)) {
                if (rgbTab.isShowing()) {
                    rgbTab.updateRGB(color, true);
                }
                if (hsbTab.isShowing()) {
                    hsbTab.updateHSB(ColorTools.colorToHSB(color), true);
                }
            }
    }

    // the rgb chooser
    RGBTab rgbTab = new RGBTab();
    private class RGBTab extends JPanel {

        // the sliders
        private final TabbedColorChooser.HorizontalColorSlider rSlider = new TabbedColorChooser.HorizontalColorSlider(0, 255, 0);
        private final TabbedColorChooser.HorizontalColorSlider gSlider = new TabbedColorChooser.HorizontalColorSlider(0, 255, 0);
        private final TabbedColorChooser.HorizontalColorSlider bSlider = new TabbedColorChooser.HorizontalColorSlider(0, 255, 0);

        // the number boxes
        private final NumberBox rBox = new NumberBox(0, 255, 0);
        private final NumberBox gBox = new NumberBox(0, 255, 0);
        private final NumberBox bBox = new NumberBox(0, 255, 0);

        public final void updateRGB(Color newColor, boolean forceRepaint) {
            if (!color.equals(newColor) || forceRepaint) {
                // debug
//                System.out.println(newColor.getRed() + "vs" + color.getRed() +
//                    " & " + newColor.getGreen() + "vs" + color.getGreen() +
//                    " & " + newColor.getBlue() + "vs" + color.getBlue() );
//                System.out.println("RGB refresh");

                // set the color
                color = newColor;

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
                rBox.setValue(color.getRed());
                gBox.setValue(color.getGreen());
                bBox.setValue(color.getBlue());
                rSlider.setValue(color.getRed());
                gSlider.setValue(color.getGreen());
                bSlider.setValue(color.getBlue());
            }
        }

        public RGBTab() {

            super(new GridBagLayout());

            // set up the RGBChooser
            setBackground(BG_COLOR);
            final GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(3,3,3,3);
            // align only when showing
            addHierarchyListener(new HierarchyListener() {
                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) == HierarchyEvent.SHOWING_CHANGED) {
                        if (isShowing()) {
                            updateRGB(color, true);
                        }
                    }
                }
            });

            // the labels
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            JLabel label = new JLabel("R");
            label.setForeground(TEXT_COLOR);
            add(label, c);
            c.gridy = 1;
            label = new JLabel("G");
            label.setForeground(TEXT_COLOR);
            add(label, c);
            c.gridy = 2;
            label = new JLabel("B");
            label.setForeground(TEXT_COLOR);
            add(label, c);

            // slider events
            rSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    updateRGB(new Color(rSlider.getValue(), color.getGreen(), color.getBlue()), false);
                }
            });
            gSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    updateRGB(new Color(color.getRed(), gSlider.getValue(), color.getBlue()), false);
                }
            });
            bSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    updateRGB(new Color(color.getRed(), color.getGreen(), bSlider.getValue()), false);
                }
            });
            // add
            c.gridx = 1;
            c.gridy = 0;
            add(rSlider, c);
            c.gridy = 1;
            add(gSlider, c);
            c.gridy = 2;
            add(bSlider, c);

            // the edit fields events
            rBox.addTextChangeListener(new NumberBox.TextChangeListener() {
                @Override
                public void onChange() {
                    updateRGB(new Color(rBox.getValue(), color.getGreen(), color.getBlue()), false);
                }
            });
            gBox.addTextChangeListener(new NumberBox.TextChangeListener() {
                @Override
                public void onChange() {
                    updateRGB(new Color(color.getRed(), gBox.getValue(), color.getBlue()), false);
                }
            });
            bBox.addTextChangeListener(new NumberBox.TextChangeListener() {
                @Override
                public void onChange() {
                    updateRGB(new Color(color.getRed(), color.getGreen(), bBox.getValue()), false);
                }
            });
            // add
            c.gridx = 2;
            c.gridy = 0;
            add(rBox, c);
            c.gridy = 1;
            add(gBox, c);
            c.gridy = 2;
            add(bBox, c);
        }

    }

    // the hsb chooser
    HSBTab hsbTab = new HSBTab();
    private class HSBTab extends JPanel {

        private final static int HUE_STEPCOUNT = 360;
        private final static int STEPCOUNT = 100;

        // the sliders
        private final TabbedColorChooser.HorizontalHueSlider hSlider = new TabbedColorChooser.HorizontalHueSlider(0, HUE_STEPCOUNT, 0);
        private final TabbedColorChooser.HorizontalColorSlider sSlider = new TabbedColorChooser.HorizontalColorSlider(0, STEPCOUNT, 0);
        private final TabbedColorChooser.HorizontalColorSlider bSlider = new TabbedColorChooser.HorizontalColorSlider(0, STEPCOUNT, 0);

        // the number boxes
        private final NumberBox hBox = new NumberBox(0, HUE_STEPCOUNT, 0);
        private final NumberBox sBox = new NumberBox(0, STEPCOUNT, 0);
        private final NumberBox bBox = new NumberBox(0, STEPCOUNT, 0);

        private float[] oldRoundHSB = new float[3];
        private float[] newRoundHSB = new float[3];

        public final void updateHSB(float[] newHsb, boolean forceRepaint) {

            // make values smooth to prevent redraw
            newRoundHSB[0] = (float)Math.round(newHsb[0]*HUE_STEPCOUNT)/HUE_STEPCOUNT;
            newRoundHSB[1] = (float)Math.round(newHsb[1]*STEPCOUNT)/STEPCOUNT;
            newRoundHSB[2] = (float)Math.round(newHsb[2]*STEPCOUNT)/STEPCOUNT;

            if ((oldRoundHSB[0] != newRoundHSB[0] || oldRoundHSB[1] != newRoundHSB[1] || oldRoundHSB[2] != newRoundHSB[2]) || forceRepaint) {
                // debug
//                System.out.println(oldRoundHSB[0] + "vs" + newRoundHSB[0] +
//                    " & " + oldRoundHSB[1] + "vs" + newRoundHSB[1] +
//                    " & " + oldRoundHSB[2] + "vs" + newRoundHSB[2] );
//                System.out.println("HSB refresh");

                // set the color
                color = ColorTools.hsbToColor(newHsb);
                oldRoundHSB = newRoundHSB.clone();

                sSlider.setLeftColor(ColorTools.hsbToColor(new float[] {oldRoundHSB[0], 0, oldRoundHSB[2]}));
                sSlider.setRightColor(ColorTools.hsbToColor(new float[] {oldRoundHSB[0], 1, oldRoundHSB[2]}));
                sSlider.repaint();

                bSlider.setLeftColor(ColorTools.hsbToColor(new float[] {oldRoundHSB[0], oldRoundHSB[1], 0}));
                bSlider.setRightColor(ColorTools.hsbToColor(new float[] {oldRoundHSB[0], oldRoundHSB[1], 1}));
                bSlider.repaint();

                // set the values
                hBox.setValue(Math.round(oldRoundHSB[0]*HUE_STEPCOUNT));
                sBox.setValue(Math.round(oldRoundHSB[1]*STEPCOUNT));
                bBox.setValue(Math.round(oldRoundHSB[2]*STEPCOUNT));
                hSlider.setValue(Math.round(oldRoundHSB[0]*HUE_STEPCOUNT));
                sSlider.setValue(Math.round(oldRoundHSB[1]*STEPCOUNT));
                bSlider.setValue(Math.round(oldRoundHSB[2]*STEPCOUNT));
            }
        }

        public HSBTab() {

            super(new GridBagLayout());

            // set up the HSBChooser
            setBackground(BG_COLOR);
            final GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(3,3,3,3);
            // align only when showing
            addHierarchyListener(new HierarchyListener() {
                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) == HierarchyEvent.SHOWING_CHANGED) {
                        if (isShowing()) {
                            updateHSB(ColorTools.colorToHSB(color), true);
                        }
                    }
                }
            });

            // the labels
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            JLabel label = new JLabel("H");
            label.setForeground(TEXT_COLOR);
            add(label, c);
            c.gridy = 1;
            label = new JLabel("S");
            label.setForeground(TEXT_COLOR);
            add(label, c);
            c.gridy = 2;
            label = new JLabel("B");
            label.setForeground(TEXT_COLOR);
            add(label, c);

            // slider events
            hSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    updateHSB(new float[] {(float)hSlider.getValue()/HUE_STEPCOUNT, oldRoundHSB[1], oldRoundHSB[2]}, false);
                }
            });
            sSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    updateHSB(new float[] {oldRoundHSB[0], (float)sSlider.getValue()/STEPCOUNT, oldRoundHSB[2]}, false);
                }
            });
            bSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    updateHSB(new float[] {oldRoundHSB[0], oldRoundHSB[1],(float)bSlider.getValue()/STEPCOUNT}, false);
                }
            });
            // add
            c.gridx = 1;
            c.gridy = 0;
            add(hSlider, c);
            c.gridy = 1;
            add(sSlider, c);
            c.gridy = 2;
            add(bSlider, c);

            // the edit fields events
            hBox.addTextChangeListener(new NumberBox.TextChangeListener() {
                @Override
                public void onChange() {
                    updateHSB(new float[] {(float)hBox.getValue()/HUE_STEPCOUNT, oldRoundHSB[1], oldRoundHSB[2]}, false);
                }
            });
            sBox.addTextChangeListener(new NumberBox.TextChangeListener() {
                @Override
                public void onChange() {
                    updateHSB(new float[] {oldRoundHSB[0], (float)sBox.getValue()/STEPCOUNT, oldRoundHSB[2]}, false);
                }
            });
            bBox.addTextChangeListener(new NumberBox.TextChangeListener() {
                @Override
                public void onChange() {
                    updateHSB(new float[] {oldRoundHSB[0], oldRoundHSB[1], (float)bBox.getValue()/STEPCOUNT}, false);
                }
            });
            // add
            c.gridx = 2;
            c.gridy = 0;
            add(hBox, c);
            c.gridy = 1;
            add(sBox, c);
            c.gridy = 2;
            add(bBox, c);

//            // the labels
//            c.fill = GridBagConstraints.BOTH;
//            c.gridx = 3;
//            c.gridy = 0;
//            label = new JLabel("°");
//            label.setForeground(TEXT_COLOR);
//            add(label, c);
//            c.gridy = 1;
//            label = new JLabel("%");
//            label.setForeground(TEXT_COLOR);
//            add(label, c);
//            c.gridy = 2;
//            label = new JLabel("%");
//            label.setForeground(TEXT_COLOR);
//            add(label, c);
        }

    }

    // the tabbed pane
    JideTabbedPane tabbedPane = new JideTabbedPane(JTabbedPane.RIGHT, JideTabbedPane.SCROLL_TAB_LAYOUT);

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

    // constructor
    public TabbedColorChooser() {

        // set up the tabbed pane
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder());
        setBackground(new Color(0, 0, 0, 0));

        // add the tab
        tabbedPane.addTab("RGB", new JScrollPane(rgbTab));

        tabbedPane.addTab("HSB", new JScrollPane(hsbTab));

        tabbedPane.setTabShape(JideTabbedPane.SHAPE_ROUNDED_FLAT); // make square
        tabbedPane.setTabResizeMode(JideTabbedPane.RESIZE_MODE_FIT);

        add(tabbedPane, BorderLayout.CENTER);
    }
}
