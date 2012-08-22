package com.vitco.frames.engine.sideview;

import com.jidesoft.action.CommandMenuBar;
import com.vitco.frames.engine.data.animationdata.AnimationDataInterface;
import com.vitco.frames.engine.data.listener.DataChangeListener;
import com.vitco.res.VitcoSettings;
import com.vitco.util.action.ActionManagerInterface;
import com.vitco.util.lang.LangSelectorInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Abstract class that provides core functionality of this panel.
 *
 * E.g. zoom, shift, initialization
 */
public abstract class VPanelPrototype extends JPanel {

    // Animation Mode Vars
    protected float[][][] points;
    protected float[][][][] lines;
    protected int highlighted_point = -1;
    protected int selected_point = -1;
    protected float[][][] preview_line = null;
    protected transient MouseAdapter animationAdapter;
    protected abstract void animationDraw(Graphics g1);

    // Voxel Mode Vars
    // todo voxel mode


    // the perspective that we mainview (e.g. (0, 1) ~ (x, y))
    protected final int[] PERS;

    // the current mode (Animation vs Voxel)
    protected Boolean animationMode = true;

    // the size of this panel (dynamic)
    protected int SIZEX = 200;
    protected int SIZEY = 200;

    // the shift of the current view (dynamic)
    protected final double[] SHIFT_FOR_CENTER = new double[2];
    protected final double[] SHIFT_BY_USER = new double[2];

    // the current zoom
    protected double ZOOM = 1;

    // the data object this panel uses
    protected final AnimationDataInterface animationData;

    // the language select
    protected final LangSelectorInterface langSelector;

    // internal - converts (1,2,3) to different perspectives, e.g. (x,y), (x,z), (y,z)
    private int[] getPerspective(int side) {
        int[] result = new int[2];
        switch (side) {
            case 0:
                result[0] = 0;
                result[1] = 1;
                break;
            case 1:
                result[0] = 0;
                result[1] = 2;
                break;
            case 2:
                result[0] = 2;
                result[1] = 1;
                break;
        }
        return result;
    }

    // internal - shift point (x, y, z) (stored) to correct viewing position (panel) as (x, y)
    protected int[] convert3D2D(float[] point) {
        int[] result = new int[2];
        result[0] = (int) Math.round(point[PERS[0]] * ZOOM + SHIFT_FOR_CENTER[0] - SHIFT_BY_USER[0]);
        result[1] = (int) Math.round(point[PERS[1]] * ZOOM + SHIFT_FOR_CENTER[1] - SHIFT_BY_USER[1]);
        return result;
    }

    // internal - shift (x, y) (e.g. panel) to stored position (x, y, z) - fill missing coordinate
    protected int[] convert2D3D(int[] point2D, int fillValue) {
        int[] point = new int[]{
                (int) Math.round((point2D[0] - SHIFT_FOR_CENTER[0] + SHIFT_BY_USER[0]) / ZOOM),
                (int) Math.round((point2D[1] - SHIFT_FOR_CENTER[1] + SHIFT_BY_USER[1]) / ZOOM)
        };
        int[] result = new int[3];
        result[0] = (PERS[0] == 0 ? point[0] : (PERS[1] == 0 ? point[1] : fillValue));
        result[1] = (PERS[0] == 1 ? point[0] : (PERS[1] == 1 ? point[1] : fillValue));
        result[2] = (PERS[0] == 2 ? point[0] : (PERS[1] == 2 ? point[1] : fillValue));
        return result;
    }

    // internal - draw a point (takes (x,y,z))
    protected void drawPoint(float[] point, Graphics2D ig, Color innerColor, Color outerColor) {
        int[] shiftPoint = convert3D2D(point);
        ig.setColor(innerColor);
        ig.fillOval(shiftPoint[0] - VitcoSettings.ANIMATION_CIRCLE_RADIUS,
               shiftPoint[1] - VitcoSettings.ANIMATION_CIRCLE_RADIUS,
                VitcoSettings.ANIMATION_CIRCLE_RADIUS *2,
                VitcoSettings.ANIMATION_CIRCLE_RADIUS *2);
        ig.setColor(outerColor);
        ig.drawOval(Math.round(shiftPoint[0] - VitcoSettings.ANIMATION_CIRCLE_RADIUS),
                Math.round(shiftPoint[1] - VitcoSettings.ANIMATION_CIRCLE_RADIUS),
                VitcoSettings.ANIMATION_CIRCLE_RADIUS * 2,
                VitcoSettings.ANIMATION_CIRCLE_RADIUS * 2);
    }

    // internal - draw a line (takes (x,y,z), (x,y,z))
    protected void drawLine(float[] p1, float[] p2, Graphics2D ig) {
        // shift perspective
        int[][] curLine = new int[2][];
        curLine[0] = convert3D2D(p1);
        curLine[1] = convert3D2D(p2);
        ig.drawLine(curLine[0][0],curLine[0][1],curLine[1][0],curLine[1][1]);
    }

    // draw the axis
    protected void drawAxis(Graphics2D ig) {

        // mainview the outline (sides)
        ig.setColor(VitcoSettings.ANIMATION_AXIS_OUTER_COLOR);
        ig.setStroke(new BasicStroke(
                VitcoSettings.ANIMATION_AXIS_LINE_SIZE * (float) 1.2,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_BEVEL));
        ig.drawLine(10, 10,
                10, SIZEY - 10 - Math.round(VitcoSettings.ANIMATION_AXIS_LINE_SIZE)
        );
        ig.drawLine(10 + Math.round(VitcoSettings.ANIMATION_AXIS_LINE_SIZE), SIZEY - 10,
                SIZEX - 10, SIZEY - 10
        );

        // mainview the inner (sides)
        GradientPaint gradient = new GradientPaint(10,10,
                (PERS[1] == 0
                        ? VitcoSettings.ANIMATION_AXIS_COLOR_X
                        : (PERS[1] == 1
                        ? VitcoSettings.ANIMATION_AXIS_COLOR_Y
                        : VitcoSettings.ANIMATION_AXIS_COLOR_Z)),
                SIZEX - 10, SIZEY - 10,
                VitcoSettings.ANIMATION_AXIS_CENTER_COLOR,
                false);
        ig.setPaint(gradient);
        ig.setStroke(new BasicStroke(
                VitcoSettings.ANIMATION_AXIS_LINE_SIZE,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));

        ig.drawLine(10, 10,
                10, SIZEY - 10 - Math.round(VitcoSettings.ANIMATION_AXIS_LINE_SIZE)
        );
        gradient = new GradientPaint(10,10,
                VitcoSettings.ANIMATION_AXIS_CENTER_COLOR,
                SIZEX - 10, SIZEY - 10,
                (PERS[0] == 0
                        ? VitcoSettings.ANIMATION_AXIS_COLOR_X
                        : (PERS[0] == 1
                        ? VitcoSettings.ANIMATION_AXIS_COLOR_Y
                        : VitcoSettings.ANIMATION_AXIS_COLOR_Z)),
                false);
        ig.setPaint(gradient);
        ig.drawLine(10 + Math.round(VitcoSettings.ANIMATION_AXIS_LINE_SIZE), SIZEY - 10,
                SIZEX - 10, SIZEY - 10
        );
    }

    // internal - draw the center cross
    protected void drawCenterCross(Graphics2D ig) {
        // draw center cross
        ig.setColor(Color.BLACK);
        ig.setStroke(new BasicStroke(1.0f));
        int[] center = convert3D2D(new float[] {0, 0, 0});
        ig.drawLine(center[0] - 5, center[1], center[0] + 5, center[1]);
        ig.drawLine(center[0], center[1] - 5, center[0], center[1] + 5);
    }

    // internal - zoom in
    protected void zoomIn(double amount) {
        double tmp = Math.min(VitcoSettings.SIDE_VIEW_MAX_ZOOM, ZOOM + amount);
        if (tmp != ZOOM) {
            SHIFT_BY_USER[0] = SHIFT_BY_USER[0] + SHIFT_BY_USER[0] * amount;
            SHIFT_BY_USER[1] = SHIFT_BY_USER[1] + SHIFT_BY_USER[1] * amount;
            ZOOM = tmp;
        }
    }

    // internal - zoom out
    protected void zoomOut(double amount) {
        double tmp = Math.max(VitcoSettings.SIDE_VIEW_MIN_ZOOM, ZOOM - amount);
        if (tmp != ZOOM) {
            SHIFT_BY_USER[0] = SHIFT_BY_USER[0] - SHIFT_BY_USER[0] * amount;
            SHIFT_BY_USER[1] = SHIFT_BY_USER[1] - SHIFT_BY_USER[1] * amount;
            ZOOM = tmp;
        }
    }

    // constructor
    public VPanelPrototype(final AnimationDataInterface animationData,
                           final ActionManagerInterface actionManager,
                           LangSelectorInterface langSelector,
                           int side,
                           final JPanel parent,
                           final CommandMenuBar menu) {
        super(true); // "true" makes this double buffered
        this.animationData = animationData;
        this.points = animationData.getPoints();
        this.lines = animationData.getLines();
        this.langSelector = langSelector;
        this.PERS = getPerspective(side);

        // register size change of parent
        parent.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SIZEX = parent.getWidth() - 1;
                SIZEY = parent.getHeight() - menu.getHeight() - 1;
                SHIFT_FOR_CENTER[0] = SIZEX/(double)2;
                SHIFT_FOR_CENTER[1] = SIZEY/(double)2;
                repaint();
            }
        });

        // register zoom button events
        actionManager.registerAction("sideview_zoom_in_tb" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomIn(VitcoSettings.SIDE_VIEW_COARSE_ZOOM_SPEED);
                repaint();
            }
        });
        actionManager.registerAction("sideview_zoom_out_tb" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomOut(VitcoSettings.SIDE_VIEW_COARSE_ZOOM_SPEED);
                repaint();
            }
        });

        // register shifting
        MouseAdapter shiftingMouseAdapter = new MouseAdapter() {
            private Point mouse_down_point = null;
            private double[] SHIFT_BY_USER_hist;

            @Override
            public void mousePressed(MouseEvent e) {
                if (highlighted_point == -1 && e.getButton() == 1) {
                    mouse_down_point = e.getPoint();
                    SHIFT_BY_USER_hist = SHIFT_BY_USER.clone();
                } else {
                    mouse_down_point = null;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouse_down_point = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouse_down_point != null) {
                    SHIFT_BY_USER[0] = SHIFT_BY_USER_hist[0] + (mouse_down_point.getX() - e.getX());
                    SHIFT_BY_USER[1] = SHIFT_BY_USER_hist[1] + (mouse_down_point.getY() - e.getY());
                    repaint();
                }
            }
        };
        this.addMouseMotionListener(shiftingMouseAdapter);
        this.addMouseListener(shiftingMouseAdapter);

        // register repaint on data change
        animationData.addDataChangeListener(
                new DataChangeListener() {
                    // todo only recompute BufferedImage if the corresponding data actually changed!
                    @Override
                    public void onAnimationDataChanged() {
                        points = animationData.getPoints();
                        lines = animationData.getLines();
                        repaint();
                    }

                    @Override
                    public void onAnimationSelectionChanged() {
                        // get updated selection data
                        highlighted_point = animationData.getHighlightedPoint();
                        selected_point = animationData.getSelectedPoint();
                        preview_line = animationData.getPreviewLine();
                        repaint();
                    }

                    @Override
                    public void onFrameDataChanged() {

                    }

                    @Override
                    public void onVoxelDataChanged() {

                    }
                }
        );

        // register the reset view action
        actionManager.registerAction("sideview_reset_view" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ZOOM = 1;
                SHIFT_BY_USER[0] = 0;
                SHIFT_BY_USER[1] = 0;
                repaint();
            }
        });
    }

    // setter for mode
    protected abstract void setAnimationMode(boolean b);

    // the preferred size for this panel
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(SIZEX, SIZEY);
    }

    // handle the redrawing of this component
    @Override
    protected void paintComponent(Graphics g1) {
        if (animationMode) {
            animationDraw(g1);
        } else {
            // todo voxelDraw
        }
    }
}