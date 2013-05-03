package com.vitco.util.colors;

import com.vitco.res.VitcoSettings;
import com.vitco.util.ColorTools;
import com.vitco.util.colors.basics.ColorChooserPrototype;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * A panel that displays hue and saturation for a given color.
 */
public final class SatBrightPanelChooser extends ColorChooserPrototype {
    // the current position of the selected color
    private Point crossPosition = new Point(0,0);

    // to check if the currentColor has changed
    private float[] prevCurrentColor = new float[] {-1, -1, -1};
    // current color that is used to draw the panel
    private float[] currentColor = new float[] {-1, -1, -1};

    // buffer image used for redraw without hue change
    private BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    // set the internal color
    public final void setColor(float[] newCurrentColor) {
        currentColor = newCurrentColor.clone();
    }

    // constructor
    public SatBrightPanelChooser() {
        // register mouse down events
        MouseAdapter ma = new MouseAdapter() {
            private void internalColorUpdate(Point point, boolean notify) {
                synchronized (VitcoSettings.SYNCHRONIZER) {
                    point = new Point(
                            (int)Math.max(0, Math.min(getWidth(), point.getX())),
                            (int)Math.max(0, Math.min(getHeight(), point.getY()))
                    );
                    currentColor = new float[] {
                            Math.max(0, Math.min(1, currentColor[0])),
                            Math.max(0, Math.min(1, (float)((point.getX() / (double) getWidth())))),
                            Math.max(0, Math.min(1, 1-(float)((point.getY() / (double) getHeight()))))
                    };
                    if (notify) {
                        // set the main color
                        notifyListeners(currentColor);
                    }
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                synchronized (VitcoSettings.SYNCHRONIZER) {
                    internalColorUpdate(e.getPoint(), false);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                synchronized (VitcoSettings.SYNCHRONIZER) {
                    internalColorUpdate(e.getPoint(), false);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                synchronized (VitcoSettings.SYNCHRONIZER) {
                    internalColorUpdate(e.getPoint(), true);
                }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    @Override
    protected final void paintComponent(Graphics g1) {
        boolean[] same = new boolean[] {
                currentColor[0] == prevCurrentColor[0],
                currentColor[1] == prevCurrentColor[1],
                currentColor[2] == prevCurrentColor[2],
                this.getWidth() == image.getWidth() && this.getHeight() == image.getHeight()
        };
        if (!same[3] || !same[0] || !same[1] || !same[2]) {
            // calculate the position of the current color
            if (!same[3] || !same[1] || !same[2]) {
                crossPosition = new Point(
                        Math.round(currentColor[1]*getWidth()),
                        Math.round((1-currentColor[2])*getHeight())
                );
            }
            if (!same[3]) {
                // resize the image
                image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            }
            if (!same[3] || !same[0]) {
                // repaint the image
                Graphics2D g = (Graphics2D)image.getGraphics();
                g.setPaint(new GradientPaint(
                        0, 0, Color.WHITE,
                        getWidth() - 1, 0, ColorTools.hsbToColor(new float[] {currentColor[0],1,1}),
                        false)
                );
                g.fillRect(0, 0, image.getWidth(), image.getHeight());
                g.setPaint(new GradientPaint(
                        0, 0, new Color(0,0,0,0),
                        0, getHeight() - 1, new Color(0,0,0,255),
                        false)
                );
                g.fillRect(0,0,image.getWidth(),image.getHeight());
            }
            // update the previous color
            prevCurrentColor = currentColor.clone();
        }

        Graphics2D ig = (Graphics2D) g1;
        // draw image
        ig.drawImage(image, 0, 0, null);
        // Anti-alias
        ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        // draw circle (selected color)
        ig.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
        ig.setColor(ColorTools.perceivedBrightness(currentColor) > 127 ? Color.BLACK : Color.WHITE);
        ig.drawOval(crossPosition.x - 5, crossPosition.y - 5, 10, 10);

    }
}

