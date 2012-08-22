package com.vitco.frames.engine.sideview;

import com.jidesoft.action.CommandMenuBar;
import com.vitco.util.GraphicTools;
import com.vitco.frames.engine.data.animationdata.AnimationDataInterface;
import com.vitco.res.VitcoSettings;
import com.vitco.util.action.ActionManagerInterface;
import com.vitco.util.action.ChangeListener;
import com.vitco.util.action.types.StateActionPrototype;
import com.vitco.util.lang.LangSelectorInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Handles one perspective of the Voxel Space, this implements the actual GUI behavior
 * (VPanelPrototype implements the core functionality)
 */
public class VPanel extends VPanelPrototype {

    // holds data that only needs recomputing on resize
    BufferedImage panelBackgroundBuffer = new BufferedImage(1,1,BufferedImage.TYPE_4BYTE_ABGR);

    // ===========================
    // Animation Mode ============
    // ===========================
    // mouse adapter
    private transient final MouseAdapter animationAdapter = new MouseAdapter() {
        private int dragPoint = -1; // the point that is dragged
        private long wasDragged = -1; // -1 if not dragged or the time in ms of first drag event

        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragPoint != -1) { // there is a point dragged
                if (wasDragged == -1) { // remember that this point was dragged
                    wasDragged = System.currentTimeMillis();
                }
                animationData.setPreviewLine(-1, -1); // reset the preview line
                // move the point to the correct position
                int[] realPoint = convert2D3D(new int[]{e.getX(), e.getY()}, 0);
                float[] tmp = animationData.getPoint(dragPoint)[0]; // need this for the missing dimension
                animationData.movePoint(dragPoint,
                        (PERS[0] == 0 || PERS[1] == 0 ? realPoint[0] : tmp[0]),
                        (PERS[0] == 1 || PERS[1] == 1 ? realPoint[1] : tmp[1]),
                        (PERS[0] == 2 || PERS[1] == 2 ? realPoint[2] : tmp[2])
                );
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int[] realPoint = convert2D3D(new int[]{e.getX(), e.getY()}, 0);
            // find if there is a point nearby
            int tmp = animationData.getNearPoint(
                    realPoint[0], realPoint[1], realPoint[2],
                    new float[] {
                            (PERS[0] == 0 || PERS[1] == 0
                                    ? (float)(VitcoSettings.ANIMATION_CIRCLE_RADIUS/ZOOM) : Integer.MAX_VALUE),
                            (PERS[0] == 1 || PERS[1] == 1
                                    ? (float)(VitcoSettings.ANIMATION_CIRCLE_RADIUS/ZOOM) : Integer.MAX_VALUE),
                            (PERS[0] == 2 || PERS[1] == 2
                                    ? (float)(VitcoSettings.ANIMATION_CIRCLE_RADIUS/ZOOM) : Integer.MAX_VALUE)
                    });
            animationData.highlightPoint(tmp); // highlight that point

            // set the preview line iff highlighted and selected point exist and are different
            if (selected_point != -1 && highlighted_point != selected_point && highlighted_point != -1) {
                animationData.setPreviewLine(selected_point, highlighted_point);
            } else {
                animationData.setPreviewLine(-1, -1);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == 1) { // left mb
                dragPoint = -1; // stop dragging
                if (highlighted_point != -1) { // there is a highlighted point
                    // if it was not at all or only for a short time dragged
                    if (wasDragged == -1 || (System.currentTimeMillis() - wasDragged < 50) ) {
                        if (selected_point == highlighted_point) { // click on selected point
                            animationData.selectPoint(-1); // deselect
                        } else {
                            if (selected_point == -1) { // click on new point
                                animationData.selectPoint(highlighted_point); // select
                            } else {
                                // click on different point -> connect/disconnect line
                                if (animationData.areConnected(selected_point, highlighted_point)) {
                                    animationData.disconnect(selected_point, highlighted_point);
                                    //animationData.selectPoint(-1); // unselect after disconnect
                                } else {
                                    animationData.connect(selected_point, highlighted_point);
                                    //animationData.selectPoint(highlighted_point); // select after connect
                                }
                                animationData.selectPoint(-1); // unselect
                                // reset "highlighting"
                                animationData.setPreviewLine(-1, -1);
                            }
                        }
                    } else {
                        animationData.selectPoint(-1);
                    }
                }
            }
            mouseMoved(e);
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            switch (e.getButton()) {
                case 3: // if right mouse - popup (add?)
                    if (selected_point == -1 && highlighted_point == -1) {
                        // no select or highlight -> show add menu
                        JPopupMenu popup = new JPopupMenu();
                        JMenuItem add = new JMenuItem(langSelector.getString("add_point"));
                        add.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent evt) {
                                // add a point
                                int[] point = convert2D3D(new int[]{e.getX(), e.getY()}, 0);
                                int added = animationData.addPoint(point[0], point[1], point[2]);
                                if (selected_point != -1) { // connect if possible
                                    animationData.connect(added, selected_point);
                                }
                                animationData.selectPoint(added); // and select
                            }
                        });
                        popup.add(add);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    } else if (highlighted_point != -1) {
                        // highlighted point -> ask to remove
                        JPopupMenu popup = new JPopupMenu();
                        JMenuItem remove = new JMenuItem(langSelector.getString("remove_point"));
                        remove.addActionListener(new ActionListener() {
                            private final int tmp_point = highlighted_point;
                            @Override
                            public void actionPerformed(ActionEvent evt) {
                                // add a point
                                animationData.removePoint(tmp_point);
                                animationData.selectPoint(-1);
                            }
                        });
                        popup.add(remove);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    } else {
                        // right click on background -> deselect
                        animationData.selectPoint(-1);
                    }
                    break;
                case 1: // if left mouse
                    if (highlighted_point != -1) { // highlighted -> select point
                        wasDragged = -1;
                        dragPoint = highlighted_point;
                    } else if (e.getClickCount() == 2) {
                        // not highlighted and double-click -> add a point
                        int[] point = convert2D3D(new int[]{e.getX(), e.getY()}, 0);
                        int added = animationData.addPoint(point[0], point[1], point[2]);
                        if (selected_point != -1) { // connect if possible
                            animationData.connect(added, selected_point);
                        }
                        animationData.selectPoint(added); // and select
                    }
                    break;
            }
        }
    };

    // internal - panel drawing
    @Override
    protected void animationDraw(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;
        BufferedImage image;
        Graphics2D ig;
        if (panelBackgroundBuffer.getWidth() == SIZEX + 1 &&
                panelBackgroundBuffer.getHeight() == SIZEY + 1) { // use buffer
            image = GraphicTools.deepCopy(panelBackgroundBuffer);
            ig = image.createGraphics();

            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // anti-aliasing
                    RenderingHints.VALUE_ANTIALIAS_ON);

        } else { // resize - refresh buffer
            image = new BufferedImage(SIZEX + 1, SIZEY + 1, BufferedImage.TYPE_INT_ARGB);
            ig = image.createGraphics();
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // anti-aliasing
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // draw bg
            ig.setColor(VitcoSettings.ANIMATION_BG_COLOR);
            ig.fillRect(0, 0, SIZEX, SIZEY);
            // draw border
            ig.setColor(VitcoSettings.DEFAULT_BORDER_COLOR);
            ig.drawRect(0, 0, SIZEX, SIZEY);

            // draw the axis and center cross
            drawAxis(ig);

            // save to buffer
            panelBackgroundBuffer = GraphicTools.deepCopy(image);
        }

        // draw the cross as (0,0,0)
        drawCenterCross(ig);

        // sideview the lines
        ig.setColor(VitcoSettings.ANIMATION_LINE_INNER_COLOR);
        ig.setStroke(new BasicStroke(VitcoSettings.ANIMATION_LINE_SIZE));
        for (float[][][] line : lines) {
            drawLine(line[0][0], line[1][0], ig);
        }

        // sideview preview line
        if (preview_line != null) {
            if (!animationData.areConnected((int)preview_line[0][1][0], (int)preview_line[1][1][0]) ) {
                ig.setColor(VitcoSettings.ANIMATION_LINE_PREVIEW_ADD_COLOR);
            } else {
                ig.setColor(VitcoSettings.ANIMATION_LINE_PREVIEW_REMOVE_COLOR);
            }
            drawLine(preview_line[0][0], preview_line[1][0], ig);
        }

        // draw points
        ig.setStroke(new BasicStroke(VitcoSettings.ANIMATION_CIRCLE_BORDER_SIZE)); // line size
        for (float[][] point : points) {
            if (point[1][0] == selected_point) { // selected
                drawPoint(point[0], ig,
                        VitcoSettings.ANIMATION_DOT_SEL_INNER_COLOR,
                        VitcoSettings.ANIMATION_DOT_SEL_OUTER_COLOR);
            } else if (point[1][0] == highlighted_point) { // highlighted
                drawPoint(point[0], ig,
                        VitcoSettings.ANIMATION_DOT_HL_INNER_COLOR,
                        VitcoSettings.ANIMATION_DOT_HL_OUTER_COLOR);
            } else { // default
                drawPoint(point[0], ig,
                        VitcoSettings.ANIMATION_DOT_INNER_COLOR,
                        VitcoSettings.ANIMATION_DOT_OUTER_COLOR);
            }
        }

        // draw the image
        g.drawImage(image, 0, 0, this);
    }
    // ===========================
    // END : Animation Mode ======
    // ===========================

    // setter for mode
    protected void setAnimationMode(boolean b) {
        animationMode = b;
        // register proper mouse events
        if (animationMode) {
            this.addMouseMotionListener(animationAdapter);
            this.addMouseListener(animationAdapter);
        } else {
            this.removeMouseListener(animationAdapter);
            this.removeMouseListener(animationAdapter);
        }
        repaint();
    }

    public VPanel(final AnimationDataInterface animationData, final ActionManagerInterface actionManager, LangSelectorInterface langSelector, int side, final JPanel parent, final CommandMenuBar menu) {
        super(animationData, actionManager, langSelector, side, parent, menu);

        // register the mode listener (when action ready)
        actionManager.performWhenActionIsReady("toggle_animation_mode", new Runnable() {
            @Override
            public void run() {
                ((StateActionPrototype) actionManager.getAction("toggle_animation_mode"))
                        .addChangeListener(new ChangeListener() {
                            @Override
                            public void actionFired(boolean b) {
                                setAnimationMode(b);
                            }
                        });
            }
        });

        // register zoom (mouse wheel)
        this.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() == -1) {
                    zoomIn(VitcoSettings.SIDE_VIEW_FINE_ZOOM_SPEED);
                } else {
                    zoomOut(VitcoSettings.SIDE_VIEW_FINE_ZOOM_SPEED);
                }
                animationAdapter.mouseMoved(e);
                repaint();
            }
        });

    }
}
