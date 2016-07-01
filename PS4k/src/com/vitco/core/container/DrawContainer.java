package com.vitco.core.container;

import com.threed.jpct.*;
import com.vitco.core.data.container.ExtendedVector;
import com.vitco.manager.async.AsyncAction;
import com.vitco.settings.DynamicSettings;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.graphic.G2DUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Defines the final draw container that draws the rendering of animation and voxels.
 */
public abstract class DrawContainer extends AbstractDrawContainer {

    // initialize this container
    public final void init() {

        final DrawContainer container = this;

        // add a border to our view
        container.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, VitcoSettings.DEFAULT_BORDER_COLOR));

        // register size change of container and change buffer size accordingly
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (container.getWidth() > 0 && container.getHeight() > 0) {
                    asyncActionManager.addAsyncAction(new AsyncAction() {
                        @Override
                        public void performAction() {
                            cleanup();
                            buffer = null; // so the gc can collect before creation if necessary
                            buffer = new HackedFrameBuffer(container.getWidth(),
                                    // increase size by one to prevent black frame
                                    container.getHeight() + 1, DynamicSettings.SAMPLING_MODE);
                            container.notifyAboutResize(container.getWidth(), container.getHeight());
                            container.doNotSkipNextWorldRender();
                            forceRepaint();
                        }
                    });
                }
            }
        });
    }

    // set resolution of this container (refresh buffer)
    public final void refreshBuffer() {
        asyncActionManager.addAsyncAction(new AsyncAction() {
            @Override
            public void performAction() {
                int w = buffer.getWidth(), h = buffer.getHeight();
                buffer = null; // so the gc can collect before creation if necessary
                buffer = new HackedFrameBuffer(w, h, DynamicSettings.SAMPLING_MODE);
            }
        });
    }

    // cleanup this container
    public final void cleanup() {
        buffer.disableRenderer(IRenderer.RENDERER_OPENGL);
        buffer.dispose();
    }

    // define some basic functions
    protected abstract void forceRepaint();
    protected abstract boolean updateGhostOverlay();
    protected abstract SimpleVector[][] getGhostOverlay();
    protected abstract void refreshVoxels(boolean b);

    // ---------------------------

    // wrapper
    private void drawFadingLine(Graphics2D ig, SimpleVector[] vectors, int i, int j, Color color, float distance, float[] zRange) {
        float range1 = (vectors[i].z-zRange[0])/distance;
        float range2 = (vectors[j].z-zRange[0])/distance;

        ig.setPaint(new GradientPaint(
                Math.round(vectors[i].x), Math.round(vectors[i].y),
                new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(55 + range1*150)),
                Math.round(vectors[j].x), Math.round(vectors[j].y),
                new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(55 + range2*150)),
                false));

        ig.drawLine(Math.round(vectors[i].x), Math.round(vectors[i].y),
                Math.round(vectors[j].x), Math.round(vectors[j].y));
    }

    // wrapper
    private void drawLine(Graphics2D g, SimpleVector start, SimpleVector stop, Color color) {
        final Graphics2D ig = (Graphics2D)g.create();
        ig.setColor(color);
        ig.drawLine(Math.round(start.x), Math.round(start.y),
                Math.round(stop.x), Math.round(stop.y));
        ig.dispose();
    }

    private static final Stroke drawingStroke1 =
            new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, null, 0);
    private static final Stroke drawingStroke2 =
            new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0);

    // wrapper
    private void drawDashLine(Graphics2D g, SimpleVector start, SimpleVector stop, Color color1, Color color2) {
        final Graphics2D ig = (Graphics2D)g.create();
        ig.setStroke(drawingStroke1);
        ig.setColor(color1);
        ig.drawLine(Math.round(start.x), Math.round(start.y),
                Math.round(stop.x), Math.round(stop.y));

        ig.setStroke(drawingStroke2);
        ig.setColor(color2);
        ig.drawLine(Math.round(start.x), Math.round(start.y),
                Math.round(stop.x), Math.round(stop.y));
        ig.dispose();
    }

    // helper
    private boolean drawBoxOutline(float[] center, float[] range,
                                Color color1, Color color2,
                                Graphics2D ig, boolean useFading) {
        // true if the bounding box was drawn successfully
        boolean valid = true;
        // true if the text should be drawn
        boolean drawText = false;

        // how the bounding box is drawn depends on the view
        switch (side) {
            // draw any boxes for the side views as simple 2D rectangles in the zero layer
            case 0:case 1:case 2: {
                SimpleVector[] vectors;
                if (side == 0) {
                    vectors = new SimpleVector[]{
                            new SimpleVector(center[0] + range[0], center[1] + range[1], 0),
                            new SimpleVector(center[0] + range[0], center[1] - range[1], 0),
                            new SimpleVector(center[0] - range[0], center[1] - range[1], 0),
                            new SimpleVector(center[0] - range[0], center[1] + range[1], 0)
                    };
                } else if (side == 1) {
                    vectors = new SimpleVector[]{
                            new SimpleVector(center[0] + range[0], 0, center[2] - range[2]),
                            new SimpleVector(center[0] + range[0], 0, center[2] + range[2]),
                            new SimpleVector(center[0] - range[0], 0, center[2] + range[2]),
                            new SimpleVector(center[0] - range[0], 0, center[2] - range[2])
                    };
                } else {
                    vectors = new SimpleVector[]{
                            new SimpleVector(0, center[1] + range[1], center[2] + range[2]),
                            new SimpleVector(0, center[1] + range[1], center[2] - range[2]),
                            new SimpleVector(0, center[1] - range[1], center[2] - range[2]),
                            new SimpleVector(0, center[1] - range[1], center[2] + range[2])
                    };
                }

                for (int i = 0; i < vectors.length; i++) {
                    // scale and convert the points
                    vectors[i].scalarMul(VitcoSettings.VOXEL_SIZE);
                    vectors[i] = convert3D2D(vectors[i]);
                    // check that valid
                    if (vectors[i] == null) {
                        valid = false;
                    }
                }
                if (valid) {
                    // draw the cube
                    ig.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
                    if (useFading) {
                        drawLine(ig, vectors[0], vectors[1], color1);
                        drawLine(ig, vectors[1], vectors[2], color1);
                        drawLine(ig, vectors[2], vectors[3], color1);
                        drawLine(ig, vectors[3], vectors[0], color1);
                    } else {
                        drawDashLine(ig, vectors[0], vectors[1], color1, color2);
                        drawDashLine(ig, vectors[1], vectors[2], color1, color2);
                        drawDashLine(ig, vectors[2], vectors[3], color1, color2);
                        drawDashLine(ig, vectors[3], vectors[0], color1, color2);
                        drawText = true;
                    }
                }
            }
            break;
            // draw any boxes in 3D
            default: {
                // define the points of the voxel
                SimpleVector[] vectors = new SimpleVector[] {
                        new SimpleVector(center[0] + range[0], center[1] + range[1], center[2] + range[2]),
                        new SimpleVector(center[0] + range[0], center[1] + range[1], center[2] - range[2]),
                        new SimpleVector(center[0] + range[0], center[1] - range[1], center[2] - range[2]),
                        new SimpleVector(center[0] + range[0], center[1] - range[1], center[2] + range[2]),
                        new SimpleVector(center[0] - range[0], center[1] + range[1], center[2] + range[2]),
                        new SimpleVector(center[0] - range[0], center[1] + range[1], center[2] - range[2]),
                        new SimpleVector(center[0] - range[0], center[1] - range[1], center[2] - range[2]),
                        new SimpleVector(center[0] - range[0], center[1] - range[1], center[2] + range[2])
                };
                for (int i = 0; i < vectors.length; i++) {
                    // scale and convert the points
                    vectors[i].scalarMul(VitcoSettings.VOXEL_SIZE);
                    vectors[i] = convert3D2D(vectors[i]);
                    // check that valid
                    if (vectors[i] == null) {
                        valid = false;
                    }
                }

                if (valid) {
                    // calculate the z range
                    float[] zRange = new float[] {vectors[0].z, vectors[0].z}; // min and max z value
                    for (int i = 1; i < 8; i ++) {
                        zRange[0] = Math.min(vectors[i].z, zRange[0]);
                        zRange[1] = Math.max(vectors[i].z, zRange[1]);
                    }
                    float distance = zRange[1] - zRange[0];

                    // draw the cube
                    ig.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
                    if (useFading) {
                        for (int i = 0; i < 4; i++) {
                            drawFadingLine(ig, vectors, i, (i + 1) % 4, color1, distance, zRange);
                            drawFadingLine(ig, vectors, i + 4, (i + 1) % 4 + 4, color1, distance, zRange);
                            drawFadingLine(ig, vectors, i, i + 4, color1, distance, zRange);
                        }
                    } else {
                        for (int i = 0; i < 4; i++) {
                            drawDashLine(ig, vectors[i], vectors[(i + 1) % 4], color1, color2);
                            drawDashLine(ig, vectors[i + 4], vectors[(i + 1) % 4 + 4], color1, color2);
                            drawDashLine(ig, vectors[i], vectors[i + 4], color1, color2);
                        }

                        drawText = true;
                    }
                }
            }
            break;
        }

        if (drawText) {
            // draw size text
            ig = (Graphics2D) ig.create();
            String str1 = String.valueOf(((int) (range[2] * 2)));
            String str2 = String.valueOf(((int) (range[1] * 2)));
            String str3 = String.valueOf(((int) (range[0] * 2)));

            ig.setFont(ig.getFont().deriveFont(18f).deriveFont(Font.BOLD));

            float len1 = (float) ig.getFontMetrics().getStringBounds(str1, ig).getWidth();
            float len2 = (float) ig.getFontMetrics().getStringBounds(str2 + ", " + str1, ig).getWidth();
            float len3 = (float) ig.getFontMetrics().getStringBounds(str3 + ", " + str2 + ", " + str1, ig).getWidth();

            ig.setColor(Color.BLACK);
            ig.drawString(str1, this.getWidth() - len1 - 14 - 1, 28 - 1);
            ig.drawString(str1, this.getWidth() - len1 - 14 + 1, 28 - 1);
            ig.drawString(str1, this.getWidth() - len1 - 14 - 1, 28 + 1);
            ig.drawString(str1, this.getWidth() - len1 - 14 + 1, 28 + 1);
            ig.drawString(str2, this.getWidth() - len2 - 14 - 1, 28 - 1);
            ig.drawString(str2, this.getWidth() - len2 - 14 + 1, 28 - 1);
            ig.drawString(str2, this.getWidth() - len2 - 14 - 1, 28 + 1);
            ig.drawString(str2, this.getWidth() - len2 - 14 + 1, 28 + 1);
            ig.drawString(str3, this.getWidth() - len3 - 14 - 1, 28 - 1);
            ig.drawString(str3, this.getWidth() - len3 - 14 + 1, 28 - 1);
            ig.drawString(str3, this.getWidth() - len3 - 14 - 1, 28 + 1);
            ig.drawString(str3, this.getWidth() - len3 - 14 + 1, 28 + 1);
            ig.setColor(VitcoSettings.ANIMATION_AXIS_COLOR_Z);
            ig.drawString(str1, this.getWidth() - len1 - 14, 28);
            ig.setColor(VitcoSettings.ANIMATION_AXIS_COLOR_Y);
            ig.drawString(str2, this.getWidth() - len2 - 14, 28);
            ig.setColor(VitcoSettings.ANIMATION_AXIS_COLOR_X);
            ig.drawString(str3, this.getWidth() - len3 - 14, 28);
            ig.dispose();
        }

        return valid;
    }

    // draw overlay for voxels
    private void drawVoxelOverlay(Graphics2D ig) {
        // Anti-alias
        ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // draw the select rect
        if (selectedRect != null) {
            ig.setColor(Color.WHITE);
            ig.drawRect(selectedRect.x, selectedRect.y, selectedRect.width, selectedRect.height);
        }

        for (int[][] outlinedRect : outlineBoxed) {
            float[] center = new float[]{
                    (outlinedRect[0][0] + outlinedRect[1][0]) / 2f,
                    (outlinedRect[0][1] + outlinedRect[1][1]) / 2f,
                    (outlinedRect[0][2] + outlinedRect[1][2]) / 2f
            };
            float[] range = new float[]{
                    Math.abs(outlinedRect[0][0] - outlinedRect[1][0]) / 2f + 0.5f,
                    Math.abs(outlinedRect[0][1] - outlinedRect[1][1]) / 2f + 0.5f,
                    Math.abs(outlinedRect[0][2] - outlinedRect[1][2]) / 2f + 0.5f
            };
            drawBoxOutline(center, range, new Color(outlinedRect[2][0]), new Color(outlinedRect[2][1]), ig, false);
        }

        // draw the preview voxel
        int[] voxel = highlighted;
        // draw selected voxel (ghost / preview voxel)
        if (voxel != null) {
            Color previewColor = VitcoSettings.VOXEL_PREVIEW_LINE_COLOR;
//                // try to find the voxel color
//                List<Voxel> list = voxelPositions.search(new float[] {voxel[0], voxel[1], voxel[2]}, ZEROS);
//                if (list.size() > 0) {
//                    Voxel voxelObject3D = list.get(0);
//                    if (ColorTools.perceivedBrightness(voxelObject3D.getColor()) < 130) {
//                        previewColor = VitcoSettings.VOXEL_PREVIEW_LINE_COLOR_BRIGHT;
//                    }
//                }

            boolean valid = drawBoxOutline(highlightedFloat, defaultVoxel, previewColor, null, ig, true);
            if (valid) {
                // draw the highlighted side (grid)
                if (previewPlane != -1) {
                    // calculate center and some variables
                    int RANGE = 4;
                    float shift = (previewPlane%2 == 0 ? 0.5f : -0.5f);
                    int plane = previewPlane / 2;
                    // the "grid" is set to the zero plane for the side views
                    SimpleVector center = new SimpleVector(
                            side == 2 ? 0 : voxel[0] + (plane == 2 ? shift : 0),
                            side == 1 ? 0 : voxel[1] + (plane == 1 ? shift : 0),
                            side == 0 ? 0 : voxel[2] + (plane == 0 ? shift : 0)
                    );

                    // calculate the points
                    SimpleVector[] points = new SimpleVector[(RANGE*2 - 1) * 4];
                    float[] range = new float[]{-RANGE, RANGE + 1};
                    int c = 0;
                    for (int i = -RANGE + 1; i < RANGE; i++) {
                        for (float j : range) {
                            points[c] = center.calcAdd(new SimpleVector(
                                    (plane != 2 ? i - 0.5 : 0),
                                    (plane != 1 ? j - 0.5 : 0),
                                    (plane == 1 ? j - 0.5 : (plane == 2 ? i - 0.5 : 0))
                            ));
                            points[c].scalarMul(VitcoSettings.VOXEL_SIZE);
                            points[c] = convert3D2D(points[c]);
                            points[c+1] = center.calcAdd(new SimpleVector(
                                    (plane != 2 ? j - 0.5 : 0),
                                    (plane != 1 ? i - 0.5 : 0),
                                    (plane == 1 ? i - 0.5 : (plane == 2 ? j - 0.5 : 0))
                            ));
                            points[c+1].scalarMul(VitcoSettings.VOXEL_SIZE);
                            points[c+1] = convert3D2D(points[c+1]);
                            if (points[c] == null || points[c+1] == null) {
                                valid = false;
                            }
                            c+=2;
                        }
                    }

                    if (valid) {
                        // draw the lines
                        float halfLen = points.length/((float)8);
                        Color transColor = new Color(previewColor.getRed(),
                                previewColor.getGreen(),
                                previewColor.getBlue(),
                                0);
                        for (int i = 0, len = points.length/4; i < len; i++) {

                            float alpha = (halfLen-Math.abs(i-halfLen))/halfLen;
                            Color visColor = new Color(
                                    previewColor.getRed(),
                                    previewColor.getGreen(),
                                    previewColor.getBlue(),
                                    Math.min(255,Math.max(0,Math.round(100*alpha))));

                            ig.setPaint(new GradientPaint(
                                    Math.round(points[i*4].x), Math.round(points[i*4].y),
                                    transColor,
                                    Math.round((points[i*4].x + points[i*4 + 2].x)/2), Math.round((points[i*4].y + points[i*4 + 2].y)/2),
                                    visColor,
                                    true));
                            ig.drawLine(Math.round(points[i*4].x), Math.round(points[i*4].y),
                                    Math.round(points[i*4 + 2].x), Math.round(points[i*4 + 2].y));

                            ig.setPaint(new GradientPaint(
                                    Math.round(points[i*4 + 1].x), Math.round(points[i*4 + 1].y),
                                    transColor,
                                    Math.round((points[i*4 + 1].x + points[i*4 + 3].x)/2), Math.round((points[i*4 + 1].y + points[i*4 + 3].y)/2),
                                    visColor,
                                    true));
                            ig.drawLine(Math.round(points[i*4 + 1].x), Math.round(points[i*4 + 1].y),
                                    Math.round(points[i*4 + 3].x), Math.round(points[i*4 + 3].y));
                        }
                    }
                }
            }
        }
    }

    // draw dynamic overlay on top of the openGL
    private void drawAnimationOverlay(Graphics2D ig) {
        // Anti-alias
        ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // store lines and points
        ArrayList<ExtendedVector[]> objects = new ArrayList<ExtendedVector[]>();
        // add lines
        for (ExtendedVector[] line : data.getLines()) {
            // line points are set to zero planes for side views
            line = line.clone();
            line[0] = new ExtendedVector(line[0]);
            line[1] = new ExtendedVector(line[1]);
            if (side == 2) {
                line[0].x = 0;
                line[1].x = 0;
            }
            if (side == 1) {
                line[0].y = 0;
                line[1].y = 0;
            }
            if (side == 0) {
                line[0].z = 0;
                line[1].z = 0;
            }
            ExtendedVector point2da = convertExt3D2D(line[0]);
            ExtendedVector point2db = convertExt3D2D(line[1]);
            if (point2da != null && point2db != null) {
                ExtendedVector mid = new ExtendedVector(point2da.calcAdd(point2db), 0);
                mid.scalarMul(DynamicSettings.SAMPLING_MODE_DIVIDEND);
                objects.add(new ExtendedVector[] {point2da, point2db, mid});
            }
        }
        // add preview line
        ExtendedVector[] preview_line = data.getPreviewLine();
        boolean connected = preview_line != null && data.areConnected(preview_line[0].id, preview_line[1].id);
        if (preview_line != null && !connected) {
            // line points are set to zero planes for side views
            preview_line = preview_line.clone();
            preview_line[0] = new ExtendedVector(preview_line[0]);
            preview_line[1] = new ExtendedVector(preview_line[1]);
            if (side == 2) {
                preview_line[0].x = 0;
                preview_line[1].x = 0;
            }
            if (side == 1) {
                preview_line[0].y = 0;
                preview_line[1].y = 0;
            }
            if (side == 0) {
                preview_line[0].z = 0;
                preview_line[1].z = 0;
            }
            ExtendedVector point2da = convertExt3D2D(preview_line[0]);
            ExtendedVector point2db = convertExt3D2D(preview_line[1]);
            if (point2da != null && point2db != null) {
                ExtendedVector mid = new ExtendedVector(point2da.calcAdd(point2db), 0);
                mid.scalarMul(DynamicSettings.SAMPLING_MODE_DIVIDEND);
                objects.add(new ExtendedVector[] {point2da, point2db, mid});
            }
        }
        // add points
        for (ExtendedVector point : data.getPoints()) {
            // points are set to zero planes for side views
            point = new ExtendedVector(point);
            if (side == 2) {
                point.x = 0;
            }
            if (side == 1) {
                point.y = 0;
            }
            if (side == 0) {
                point.z = 0;
            }
            ExtendedVector point2d = convertExt3D2D(point);
            if (point2d != null) {
                objects.add(new ExtendedVector[] {point2d});
            }
        }
        // sort the data
        Collections.sort(objects, new Comparator<ExtendedVector[]>() {
            @Override
            public int compare(ExtendedVector[] o1, ExtendedVector[] o2) {
                if (o1.length == 3 && o2.length == 3) { // two lines
                    return (int) Math.signum(o1[2].z - o2[2].z);
                } else if (o1.length == 1 && o2.length == 1) { // two points
                    return (int) Math.signum(o1[0].z - o2[0].z);
                } else if (o1.length == 1 && o2.length == 3) { // point and line
                    float diff = o1[0].z - o2[2].z;
                    return Math.abs(diff) < 0.00000001 ? 1 : (int)Math.signum(diff); // point to front if equal
                } else if (o1.length == 3 && o2.length == 1) { // line and point
                    float diff = o1[2].z - o2[0].z;
                    return Math.abs(diff) < 0.00000001 ? -1 : (int)Math.signum(diff); // point to front if equal
                }
                return 0;
            }
        });

        // draw all points and lines
        int selected_point = data.getSelectedPoint();
        int highlighted_point = data.getHighlightedPoint();

        for (ExtendedVector[] object : objects) {
            if (object.length == 1) {
                if (object[0].id == selected_point) { // selected
                    G2DUtil.drawPoint(object[0], ig,
                            VitcoSettings.ANIMATION_DOT_SEL_INNER_COLOR,
                            VitcoSettings.ANIMATION_DOT_SEL_OUTER_COLOR,
                            VitcoSettings.ANIMATION_CIRCLE_RADIUS,
                            VitcoSettings.ANIMATION_CIRCLE_BORDER_SIZE);
                } else if (object[0].id == highlighted_point) { // highlighted
                    G2DUtil.drawPoint(object[0], ig,
                            VitcoSettings.ANIMATION_DOT_HL_INNER_COLOR,
                            VitcoSettings.ANIMATION_DOT_HL_OUTER_COLOR,
                            VitcoSettings.ANIMATION_CIRCLE_RADIUS,
                            VitcoSettings.ANIMATION_CIRCLE_BORDER_SIZE);
                } else { // default
                    G2DUtil.drawPoint(object[0], ig,
                            VitcoSettings.ANIMATION_DOT_INNER_COLOR,
                            VitcoSettings.ANIMATION_DOT_OUTER_COLOR,
                            VitcoSettings.ANIMATION_CIRCLE_RADIUS,
                            VitcoSettings.ANIMATION_CIRCLE_BORDER_SIZE);
                }
            } else {
                if (preview_line != null &&
                        ((object[0].id == preview_line[0].id && object[1].id == preview_line[1].id)
                                || (object[0].id == preview_line[1].id && object[1].id == preview_line[0].id))) {
                    G2DUtil.drawLine(object[0], object[1], ig,
                            connected ? VitcoSettings.ANIMATION_LINE_PREVIEW_REMOVE_COLOR : VitcoSettings.ANIMATION_LINE_PREVIEW_ADD_COLOR,
                            VitcoSettings.ANIMATION_LINE_OUTER_COLOR,
                            VitcoSettings.ANIMATION_LINE_SIZE);
                } else {
                    G2DUtil.drawLine(object[0], object[1], ig,
                            VitcoSettings.ANIMATION_LINE_INNER_COLOR, VitcoSettings.ANIMATION_LINE_OUTER_COLOR,
                            VitcoSettings.ANIMATION_LINE_SIZE);
                }
            }
        }

    }

    // wrapper
    private void drawAxeHalf(SimpleVector unitVector, boolean invert, Graphics2D ig,
                             Color innerColor, Color outerColor, float size,
                             int offsetX, int offsetY
    ) {
        G2DUtil.drawLine(
                new SimpleVector((invert?-1:1)*unitVector.x*15 + offsetX, (invert?-1:1)*unitVector.y*15 + offsetY, 0),
                new SimpleVector((invert?-1:1)*unitVector.x*3 + offsetX, (invert?-1:1)*unitVector.y*3 + offsetY, 0),
                ig,
                innerColor,
                outerColor,
                size
        );
    }

    // called for content that only changes when the opengl content changes
    private void drawLinkedOverlay(final Graphics2D ig) {
        // Anti-alias
        ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // draw the bounding box
        if (drawBoundingBox) {
            drawBoundingBox(ig, side);
        }

        int offsetX = 25;
        int offsetY = 25;

//        // this moves the axis (red blue green) to the corner of the bounding box (main view)
//            if (side == -1) {
//                // need to convert this vector to static
//                SimpleVector pos =
//                        convert3D2D(new SimpleVector(
//                                (DynamicSettings.VOXEL_PLANE_RANGE_X - 0.5) * VitcoSettings.VOXEL_SIZE, VitcoSettings.HALF_VOXEL_SIZE,
//                                (DynamicSettings.VOXEL_PLANE_RANGE_Z - 0.5) * VitcoSettings.VOXEL_SIZE));
//                if (pos != null) {
//                    offsetX = Math.round(pos.x);
//                    offsetY = Math.round(pos.y);
//                }
//            }

        // draw axis into corner (top left)
        Matrix matrix = camera.getBack();
        ExtendedVector[] vec = new ExtendedVector[]{
                new ExtendedVector(1, 0, 0, 0),
                new ExtendedVector(0, 1, 0, 1),
                new ExtendedVector(0, 0, 1, 2)
        };
        for (ExtendedVector v : vec) {
            v.matMul(matrix);
        }
        Arrays.sort(vec, new Comparator<ExtendedVector>() {
            @Override
            public int compare(ExtendedVector o1, ExtendedVector o2) {
                return (int) Math.signum(o1.z - o2.z);
            }
        });
        for (ExtendedVector v : vec) {
            drawAxeHalf(v, true, ig,
                    (v.id == 0
                            ? VitcoSettings.ANIMATION_AXIS_COLOR_X
                            : (v.id == 1
                            ? VitcoSettings.ANIMATION_AXIS_COLOR_Y
                            : VitcoSettings.ANIMATION_AXIS_COLOR_Z)),
                    VitcoSettings.ANIMATION_AXIS_OUTER_COLOR, VitcoSettings.ANIMATION_AXIS_LINE_SIZE,
                    offsetX, offsetY
            );
        }

        // draw center cross
        if (drawBoundingBox || side != -1) {
            ig.setColor(VitcoSettings.ANIMATION_CENTER_CROSS_COLOR);
            ig.setStroke(new BasicStroke(1.0f));
            SimpleVector center = convert3D2D(new SimpleVector(0,0,0));
            if (center != null) {
                ig.drawLine(Math.round(center.x - 5), Math.round(center.y), Math.round(center.x + 5), Math.round(center.y));
                ig.drawLine(Math.round(center.x), Math.round(center.y - 5), Math.round(center.x), Math.round(center.y + 5));
            }
        }
    }

    // draw so that we can also see the outline of the
    // box in the front (where the textured grid is hidden)
    private void drawBoundingBox(Graphics2D gr, int side) {

        // Anti-alias
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        switch (side) {
            // draw the bounding box for the side views as a simple 2D rectangle in the zero layer
            case 0: case 1: case 2: {
                SimpleVector[] vectors;
                if (side == 0) {
                    vectors = new SimpleVector[] {
                            new SimpleVector(DynamicSettings.VOXEL_PLANE_RANGE_X, 0.5f, 0),
                            new SimpleVector(DynamicSettings.VOXEL_PLANE_RANGE_X, -DynamicSettings.VOXEL_PLANE_SIZE_Y +0.5f, 0),
                            new SimpleVector(-DynamicSettings.VOXEL_PLANE_RANGE_X, -DynamicSettings.VOXEL_PLANE_SIZE_Y +0.5f, 0),
                            new SimpleVector(-DynamicSettings.VOXEL_PLANE_RANGE_X, 0.5f, 0)
                    };
                } else if (side == 1) {
                    vectors = new SimpleVector[] {
                            new SimpleVector(DynamicSettings.VOXEL_PLANE_RANGE_X, 0, -DynamicSettings.VOXEL_PLANE_RANGE_Z),
                            new SimpleVector(-DynamicSettings.VOXEL_PLANE_RANGE_X, 0, -DynamicSettings.VOXEL_PLANE_RANGE_Z),
                            new SimpleVector(-DynamicSettings.VOXEL_PLANE_RANGE_X, 0, DynamicSettings.VOXEL_PLANE_RANGE_Z),
                            new SimpleVector(DynamicSettings.VOXEL_PLANE_RANGE_X, 0, DynamicSettings.VOXEL_PLANE_RANGE_Z)
                    };
                } else {
                    vectors = new SimpleVector[] {
                            new SimpleVector(0, -DynamicSettings.VOXEL_PLANE_SIZE_Y +0.5f, -DynamicSettings.VOXEL_PLANE_RANGE_Z),
                            new SimpleVector(0, 0.5f, -DynamicSettings.VOXEL_PLANE_RANGE_Z),
                            new SimpleVector(0, 0.5f, DynamicSettings.VOXEL_PLANE_RANGE_Z),
                            new SimpleVector(0, -DynamicSettings.VOXEL_PLANE_SIZE_Y +0.5f, DynamicSettings.VOXEL_PLANE_RANGE_Z)
                    };
                }

                boolean valid = true;
                for (int i = 0; i < vectors.length; i++) {
                    // scale and convert the points
                    if (DynamicSettings.VOXEL_PLANE_SIZE_X%2 == 0) {
                        // necessary if the center voxel is not the true center
                        vectors[i].x -= 0.5f;
                    }
                    if (DynamicSettings.VOXEL_PLANE_SIZE_Z%2 == 0) {
                        // necessary if the center voxel is not the true center
                        vectors[i].z -= 0.5f;
                    }
                    vectors[i].scalarMul(VitcoSettings.VOXEL_SIZE);
                    vectors[i] = convert3D2D(vectors[i]);
                    // check that valid
                    if (vectors[i] == null) {
                        valid = false;
                    }
                }
                if (valid) {
                    // draw the rect
                    gr.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
                    drawLine(gr, vectors[0], vectors[1], VitcoSettings.BOUNDING_BOX_COLOR);
                    drawLine(gr, vectors[1], vectors[2], VitcoSettings.BOUNDING_BOX_COLOR);
                    drawLine(gr, vectors[2], vectors[3], VitcoSettings.BOUNDING_BOX_COLOR);
                    drawLine(gr, vectors[3], vectors[0], VitcoSettings.BOUNDING_BOX_COLOR);
                }
            }
            break;
            // draw the bounding box in 3D view
            default: {
                // get an instance that we can modify
                SimpleVector[] vectors = new SimpleVector[14];

                boolean valid = true;
                for (int i = 0; i < vectors.length; i++) {
                    // scale and convert the points
                    vectors[i] = getVectorsStatic(i);
                    vectors[i].y += 0.5f / DynamicSettings.VOXEL_PLANE_SIZE_Y;
                    vectors[i].x *= DynamicSettings.VOXEL_PLANE_WORLD_SIZE_X;
                    vectors[i].y *= DynamicSettings.VOXEL_PLANE_WORLD_SIZE_Y;
                    vectors[i].z *= DynamicSettings.VOXEL_PLANE_WORLD_SIZE_Z;
                    if (DynamicSettings.VOXEL_PLANE_SIZE_X%2 == 0) {
                        // necessary if the center voxel is not the true center
                        vectors[i].x -= 0.5f * VitcoSettings.VOXEL_SIZE;
                    }
                    if (DynamicSettings.VOXEL_PLANE_SIZE_Z%2 == 0) {
                        // necessary if the center voxel is not the true center
                        vectors[i].z -= 0.5f * VitcoSettings.VOXEL_SIZE;
                    }
                    vectors[i] = convert3D2D(vectors[i]);
                    // check that valid
                    if (vectors[i] == null) {
                        valid = false;
                    }
                }

                if (valid) {
                    // calculate the z range
                    float[] zRange = new float[]{vectors[0].z, vectors[0].z}; // min and max z value
                    for (int i = 1; i < 8; i++) {
                        zRange[0] = Math.min(vectors[i].z, zRange[0]);
                        zRange[1] = Math.max(vectors[i].z, zRange[1]);
                    }
                    float distance = zRange[1] - zRange[0];

                    // draw the cube
                    gr.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
                    for (int i = 0; i < 4; i++) {
                        drawFadingLine(gr, vectors, i, (i + 1) % 4, VitcoSettings.BOUNDING_BOX_COLOR, distance, zRange);
                        drawFadingLine(gr, vectors, i + 4, (i + 1) % 4 + 4, VitcoSettings.BOUNDING_BOX_COLOR, distance, zRange);
                        drawFadingLine(gr, vectors, i, i + 4, VitcoSettings.BOUNDING_BOX_COLOR, distance, zRange);
                    }

                    // draw center dots for bounding box
                    gr.setColor(Color.WHITE);
                    gr.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
                    for (int i = 0; i < 6; i++) {
                        gr.drawLine(Math.round(vectors[8+i].x), Math.round(vectors[8+i].y), Math.round(vectors[8+i].x), Math.round(vectors[8+i].y));
                    }
                }
            }
            break;
        }
    }

    // ---------------------------
    
    // constructor
    public DrawContainer(int side) {
        super(side);
        // initialize the drawing buffer
        notifyAboutResize(100, 100);

    }

    // handle the resize of this container and
    // update all variables accordingly
    public void notifyAboutResize(int width, int height) {
        toDraw = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics gr = toDraw.getGraphics();
        gr.setColor(bgColor);
        gr.fillRect(0, 0, width, height);
        gr.dispose();
        // the overlay
        overlayBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // draw the lines
        overlayBufferGraphics = (Graphics2D)overlayBuffer.getGraphics();
        // Anti-alias
        overlayBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        // bg color to clear
        overlayBufferGraphics.setBackground(new Color(0,0,0,0));
        // set color
        overlayBufferGraphics.setColor(VitcoSettings.GHOST_VOXEL_OVERLAY_LINE_COLOR);
        overlayBufferGraphics.setStroke(
                new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,
                        0, new float[]{4,5}, 2));
        hasResized = true;
    }

    // enable shader for this container
    private boolean enableShade = false;
    public void enableShader(boolean state) {
        enableShade = state;
    }

    // expose the z buffer of this container
    public final int[] getZBuffer() {
        return buffer.getZBuffer();
    }

    // exposes the pixel array
    public final int[] getPixels() {
        return buffer.getPixels();
    }

    // get the image currently rendered in high quality
    public final BufferedImage getImage() {
        Config.useFramebufferWithAlpha = true;
        HackedFrameBuffer fb = new HackedFrameBuffer(getWidth()*2, getHeight()*2, FrameBuffer.SAMPLINGMODE_NORMAL);
        Config.useFramebufferWithAlpha = false;
        fb.clear(new Color(0, 0, 0, 0));
        world.renderScene(fb);
        world.draw(fb);
        fb.update();

        int w = fb.getWidth() * 2;
        int[] zBuffer = fb.getZBuffer(); //requires hacked framebuffer
        int[] pixels = fb.getPixels();

        // fix t-junction anomalies
        for (int c = w + 1; c < zBuffer.length - w - 1; c++) {

            int x = zBuffer[c] + Integer.MAX_VALUE;
            int x5 = zBuffer[c-w] + Integer.MAX_VALUE;
            int x3 = zBuffer[c+w] + Integer.MAX_VALUE;
            int x1 = zBuffer[c-1] + Integer.MAX_VALUE;
            int x7 = zBuffer[c+1] + Integer.MAX_VALUE;
            int x2 = zBuffer[c-w - 1] + Integer.MAX_VALUE;
            int x8 = zBuffer[c-w + 1] + Integer.MAX_VALUE;
            int x0 = zBuffer[c+w - 1] + Integer.MAX_VALUE;
            int x6 = zBuffer[c+w + 1] + Integer.MAX_VALUE;

            if (Math.abs(x1 - x7) < 100000 && Math.abs(x1 - x) > 100000) {
                pixels[c] = pixels[c-1];
            } else if (Math.abs(x5 - x3) < 100000 && Math.abs(x5 - x) > 100000) {
                pixels[c] = pixels[c-w];
            } else if (Math.abs(x0 - x8) < 100000 && Math.abs(x0 - x) > 100000) {
                pixels[c] = pixels[c+w-1];
            } else if (Math.abs(x2 - x6) < 100000 && Math.abs(x2 - x) > 100000) {
                pixels[c] = pixels[c-w-1];
            }
        }

        BufferedImage largeResult = new BufferedImage(fb.getWidth(), fb.getHeight(), BufferedImage.TYPE_INT_ARGB);
        fb.display(largeResult.getGraphics());

        // resize
        BufferedImage result = new BufferedImage(largeResult.getWidth()/2, largeResult.getHeight()/2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(largeResult, 0, 0, largeResult.getWidth()/2, largeResult.getHeight()/2, null);
        g2d.dispose();

        return result;
    }

    // get the image currently rendered in high quality
    public final BufferedImage getDepthImage() {
        HackedFrameBuffer fb = new HackedFrameBuffer(getWidth(), getHeight(), FrameBuffer.SAMPLINGMODE_OGSS);
        fb.clear();
        world.renderScene(fb);
        world.draw(fb);
        fb.update();
        BufferedImage largeResult = new BufferedImage(fb.getWidth()*2, fb.getHeight()*2, BufferedImage.TYPE_INT_ARGB);

        int w = fb.getWidth() * 2;
        int[] zBuffer = fb.getZBuffer(); //requires hacked framebuffer

        // compute mean
        int count = 0;
        long mean = 0;
        for (int aZBuffer : zBuffer) {
            if (aZBuffer != -2147483647) {
                mean += aZBuffer;
                count++;
            }
        }
        mean /= count;

        // compute std deviation
        long sum = 0;
        for (int aZBuffer : zBuffer) {
            if (aZBuffer != -2147483647) {
                sum += Math.pow(aZBuffer - mean, 2);
            }
        }
        double stdDev = Math.sqrt(sum/(double)count);

        // compute min and max for non outliers
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int aZBuffer : zBuffer) {
            if (Math.abs(aZBuffer - mean) < 4*stdDev) {
                min = Math.min(min, aZBuffer);
                max = Math.max(max, aZBuffer);
            }
        }
        int range = max - min;

        // fix t-junction anomalies
        for (int c = w + 1; c < zBuffer.length - w - 1; c++) {

            int x = zBuffer[c] + Integer.MAX_VALUE;
            int x5 = zBuffer[c-w] + Integer.MAX_VALUE;
            int x3 = zBuffer[c+w] + Integer.MAX_VALUE;
            int x1 = zBuffer[c-1] + Integer.MAX_VALUE;
            int x7 = zBuffer[c+1] + Integer.MAX_VALUE;
            int x2 = zBuffer[c-w - 1] + Integer.MAX_VALUE;
            int x8 = zBuffer[c-w + 1] + Integer.MAX_VALUE;
            int x0 = zBuffer[c+w - 1] + Integer.MAX_VALUE;
            int x6 = zBuffer[c+w + 1] + Integer.MAX_VALUE;

            if (Math.abs(x1 - x7) < 100000 && Math.abs(x1 - x) > 100000) {
                zBuffer[c] = zBuffer[c-1];
            } else if (Math.abs(x5 - x3) < 100000 && Math.abs(x5 - x) > 100000) {
                zBuffer[c] = zBuffer[c-w];
            } else if (Math.abs(x0 - x8) < 100000 && Math.abs(x0 - x) > 100000) {
                zBuffer[c] = zBuffer[c+w-1];
            } else if (Math.abs(x2 - x6) < 100000 && Math.abs(x2 - x) > 100000) {
                zBuffer[c] = zBuffer[c-w-1];
            }
        }

        // compute values
        for (int c = 0; c < zBuffer.length; c++) {
            if (zBuffer[c] != -2147483647) {
                int val = (int) Math.min(255,Math.max(0,
                        ((zBuffer[c] - min) ) / (range/255f)
                ));
                //if (c%w < largeResult.getWidth() && c/w < largeResult.getHeight())
                largeResult.setRGB((c % w), (c / w), new Color(val, val, val, 255).getRGB());
            }
        }

        // resize
        BufferedImage result = new BufferedImage(largeResult.getWidth()/2, largeResult.getHeight()/2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(largeResult, 0, 0, largeResult.getWidth()/2, largeResult.getHeight()/2, null);
        g2d.dispose();

        return result;
    }

    // draw shader
    private void drawShader() {
        // draw depth outline (software "shader")
        // idea: http://coding-experiments.blogspot.de/2010/06/edge-detection.html
        int w = buffer.getWidth() * DynamicSettings.SAMPLING_MODE_MULTIPLICAND;
        int factor = w * DynamicSettings.SAMPLING_MODE_MULTIPLICAND * DynamicSettings.SAMPLING_MODE_MULTIPLICAND;
        int[] zBuffer = buffer.getZBuffer(); //requires hacked framebuffer
        @SuppressWarnings("MismatchedReadAndWriteOfArray")
        int[] pixels = buffer.getPixels();
        for (int c = w*2 + 2; c < zBuffer.length - w*2 - 2; c++) {

            int x = zBuffer[c] + Integer.MAX_VALUE;
            if (x != 0) {
                int x5 = zBuffer[c-w] + Integer.MAX_VALUE;
                int x3 = zBuffer[c+w] + Integer.MAX_VALUE;
                int x1 = zBuffer[c-1] + Integer.MAX_VALUE;
                int x7 = zBuffer[c+1] + Integer.MAX_VALUE;
                int x2 = zBuffer[c-w - 1] + Integer.MAX_VALUE;
                int x8 = zBuffer[c-w + 1] + Integer.MAX_VALUE;
                int x0 = zBuffer[c+w - 1] + Integer.MAX_VALUE;
                int x6 = zBuffer[c+w + 1] + Integer.MAX_VALUE;

                // move one more outwards
                int x5t = zBuffer[c-2*w] + Integer.MAX_VALUE;
                int x3t = zBuffer[c+2*w] + Integer.MAX_VALUE;
                int x1t = zBuffer[c-2] + Integer.MAX_VALUE;
                int x7t = zBuffer[c+2] + Integer.MAX_VALUE;
                int x2t = zBuffer[c-2*w - 2] + Integer.MAX_VALUE;
                int x8t = zBuffer[c-2*w + 2] + Integer.MAX_VALUE;
                int x0t = zBuffer[c+2*w - 2] + Integer.MAX_VALUE;
                int x6t = zBuffer[c+2*w + 2] + Integer.MAX_VALUE;

                int p1 = Math.abs(x1 - x7)/10;
                int p2 = Math.abs(x5 - x3)/10;
                int p3 = Math.abs(x0 - x8)/10;
                int p4 = Math.abs(x2 - x6)/10;
                int val = (Math.abs(x7 - x7t) < p1 && Math.abs(x1 - x1t) < p1 ? 1 : 0) +
                        (Math.abs(x5 - x5t) < p2 && Math.abs(x3 - x3t) < p2 ? 1 : 0) +
                        (Math.abs(x0 - x0t) < p3 && Math.abs(x8 - x8t) < p3 ? 1 : 0) +
                        (Math.abs(x2 - x2t) < p4 && Math.abs(x6 - x6t) < p4 ? 1 : 0);

                if (val == 2 || val == 3) {
                    pixels[(c/factor)*w + (c/DynamicSettings.SAMPLING_MODE_MULTIPLICAND)%w] = 0;
                    c += DynamicSettings.SAMPLING_MODE_MULTIPLICAND -1;
                } else {

                    int xP = x + 100;
                    int xM = x - 100;

                    int s = ((x1t > xP && x7 > xP) || (x1t < xM && x7t < xM) ? 1 : 0) +
                            ((x5t > xP && x3 > xP) || (x5t < xM && x3t < xM) ? 1 : 0) +
                            ((x2t > xP && x6 > xP) || (x2t < xM && x6t < xM) ? 1 : 0) +
                            ((x0t > xP && x8 > xP) || (x0t < xM && x8t < xM) ? 1 : 0);

                    if (s == 2 || s == 3) {
                        pixels[(c/factor)*w + (c/DynamicSettings.SAMPLING_MODE_MULTIPLICAND)%w] = 0;
                        c += DynamicSettings.SAMPLING_MODE_MULTIPLICAND -1;
                    }
                }
            }
        }
    }

    // render the content of this container
    public final void render() {
        if (skipNextWorldRender && !doNotSkipNextWorldRender) {
            skipNextWorldRender = false;
        } else {
            if (doNotSkipNextWorldRender) {
                doNotSkipNextWorldRender = false;
                skipNextWorldRender = false;
            }
            buffer.clear(bgColor);
            if (drawWorld && world != null) {
                refreshVoxels(false);
                world.renderScene(buffer);
                if (useWireFrame) {
                    world.drawWireframe(buffer, VitcoSettings.WIREFRAME_COLOR);
                } else {
                    world.draw(buffer);
                    if (drawSelectedVoxels) { // only draw selected voxels if enables
                        selectedVoxelsWorld.drawAsShiftedWireframe(buffer,
                                VitcoSettings.SELECTED_VOXEL_WIREFRAME_COLOR,
                                VitcoSettings.SELECTED_VOXEL_WIREFRAME_COLOR_SHIFTED);
                    }
                }
            }
            buffer.update();
            if (drawOverlay && camera != null) { // overlay part 1
                drawLinkedOverlay((Graphics2D) buffer.getGraphics()); // refreshes with OpenGL
            }
            // draw the shader if enabled
            if (enableShade) {
                drawShader();
            }
        }
        Graphics2D gr = (Graphics2D) toDraw.getGraphics();

        buffer.display(gr);

        // draw the under/overlay (voxels in parallel planes)
        if (drawGhostOverlay) {
            drawGhostOverlay(gr, cameraChanged, hasResized);
        }
        if (drawOverlay && drawAnimationOverlay && data != null) { // overlay part 2
            drawAnimationOverlay(gr); // refreshes with animation data
        }
        if (drawOverlay && drawVoxelOverlay) {
            drawVoxelOverlay(gr);
        }
        gr.dispose();

//        // debug
//        if (Main.isDebugMode()) {
//            gr.drawString(String.valueOf(world.getVisibilityList().getSize()), buffer.getWidth() - 40, 20);
//        }

        cameraChanged = false; // camera is current for this redraw
        hasResized = false; // no resize pending

    }

    // handle the redrawing of this component
    // Note1: this MUSTN'T have any call to synchronized
    // Note2: this method should be super fast to execute,
    // otherwise we might have many pending repaint events
    // in the queue!
    @Override
    protected final void paintComponent(Graphics g1) {
        if (toDraw != null) {
            g1.drawImage(toDraw, 0, 0, null);
        } else {
            g1.setColor(bgColor);
            g1.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
        this.setRepainting(false);
        if (needRepainting) {
            needRepainting = false;
            this.resetSkipRenderFlags();
            if (skipNextWorldRenderBuffer) {
                this.skipNextWorldRender();
            }
            if (doNotSkipNextWorldRenderBuffer) {
                this.doNotSkipNextWorldRender();
            }
            forceRepaint();
        }
    }

    // draw some ghosting lines (the voxel outline)
    private void drawGhostOverlay(Graphics2D g1, boolean cameraChanged, boolean hasResized) {
        boolean updated = updateGhostOverlay();
        if (updated || cameraChanged || hasResized) {
            // clear the previous drawings
            overlayBufferGraphics.clearRect(0,0,overlayBuffer.getWidth(),overlayBuffer.getHeight());
            // draw
            for (SimpleVector[] line : getGhostOverlay()) {
                SimpleVector p1 = convert3D2D(line[0]);
                SimpleVector p2 = convert3D2D(line[1]);
                overlayBufferGraphics.drawLine(Math.round(p1.x), Math.round(p1.y), Math.round(p2.x), Math.round(p2.y));
            }
        }
        g1.drawImage(overlayBuffer, 0, 0, null);
    }

}
