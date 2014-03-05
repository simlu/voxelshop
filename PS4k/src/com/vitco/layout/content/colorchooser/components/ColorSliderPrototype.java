package com.vitco.layout.content.colorchooser.components;

import com.vitco.layout.content.colorchooser.basic.Settings;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * A custom color slider prototype that allows for custom thumbs (cursor) and background generation.
 */
public abstract class ColorSliderPrototype extends JSlider {

    // holds the listeners
    private final ArrayList<ValueChangeListener> listener = new ArrayList<ValueChangeListener>();

    // add a listener
    public final void addValueChangeListener(ValueChangeListener vcl) {
        listener.add(vcl);
    }

    // notify listeners
    private boolean blockNotify = false;

    private void notifyListeners() {
        if (!blockNotify) {
            for (ValueChangeListener vcl : listener) {
                vcl.onChange(this.changeEvent);
            }
        }
    }

    // this will NOT trigger the listeners to be notified
    public final void setValueWithoutRefresh(int value) {
        if (getValue() != value) {
            blockNotify = true;
            setValue(value);
            blockNotify = false;
        }
    }

    // allow to set a specific fixed height
    private Integer height = null;
    public void setHeight(int height) {
        this.height = height;
    }

    // return the set height (setHeight(...))
    @Override
    public int getHeight() {
        if (height == null) {
            return super.getHeight();
        } else {
            return height;
        }
    }

    // draw the background
    protected abstract void drawBackground(Graphics2D g, SliderUI sliderUI);

    // thumb image that is drawn
    private BufferedImage thumbBuffer;
    // setter for the thumb image
    public final void setThumb(BufferedImage thumb) {
        thumbBuffer = thumb;
    }

    // constructor
    public ColorSliderPrototype(final int orientation, int min, int max, int current) {
        super(orientation, min, max, current);

        // notification
        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                notifyListeners();
            }
        });

        // ================
        // create default thumb
        {
            final int size = 5;
            thumbBuffer = new BufferedImage(size * 2 + 1, size * 2 + 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig = (Graphics2D) thumbBuffer.getGraphics();
            // Anti-alias
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            if (orientation != JSlider.HORIZONTAL) {
                //ig.rotate(-Math.PI/2, size, size);
                ig.setColor(Color.BLACK);
                ig.fillOval(0, 0, thumbBuffer.getWidth() - 1, thumbBuffer.getHeight() - 1);
                ig.setColor(Color.WHITE);
                ig.drawOval(0,0,thumbBuffer.getWidth()-1,thumbBuffer.getHeight()-1);
            } else {
                ig.setPaint(new GradientPaint(0, 1, Settings.SLIDER_KNOB_COLOR_TOP,
                        0, size * 2, Settings.SLIDER_KNOB_COLOR_BOTTOM, false));
                ig.fillPolygon(new int[]{1, size * 2, size * 2, size, 1}, new int[]{size * 2, size * 2, size, 1, size}, 5);

                ig.setColor(Settings.SLIDER_KNOB_OUTLINE_COLOR);
                ig.setStroke(new BasicStroke(0.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
                ig.drawPolygon(new int[]{0, size * 2, size * 2, size, 0}, new int[]{size * 2, size * 2, size, 0, size}, 5);
            }
            ig.dispose();
        }
        // ===============

        // create ui
        final SliderUI sliderUI = new SliderUI(this) {
            @Override
            public void paint(Graphics g, JComponent c) {
                super.paint(g, c);

                // draw the background
                drawBackground((Graphics2D) g, this);

                // draw the thumb
                if (g.getClipBounds().intersects(thumbRect)) {
                    if (orientation == JSlider.HORIZONTAL) {
                        // make sure the thumbRect covers the whole height
                        thumbRect.y = 0;
                        thumbRect.height = slider.getHeight();
                        // draw the thumb
                        g.drawImage(thumbBuffer,
                                xPositionForValue(slider.getValue()) - thumbBuffer.getWidth() / 2,
                                slider.getHeight() - thumbBuffer.getHeight() - 1, null
                        );
                    } else {
                        // make sure the thumbRect covers the whole width
                        thumbRect.x = 0;
                        thumbRect.width = slider.getWidth();
                        // draw the thumb
                        g.drawImage(thumbBuffer,
                                slider.getWidth() - thumbBuffer.getWidth() - 1,
                                yPositionForValue(slider.getValue()) - thumbBuffer.getHeight() / 2, null
                        );
                    }
                }
            }
        };

        // move the thumb to the position we pressed (instantly)
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (orientation == JSlider.HORIZONTAL) {
                    setValue(sliderUI.valueForXPosition(e.getX()));
                } else {
                    setValue(sliderUI.valueForYPosition(e.getY()));
                }
                repaint();
            }
        });
        // register - needs to go last
        setUI(sliderUI);
    }
}
