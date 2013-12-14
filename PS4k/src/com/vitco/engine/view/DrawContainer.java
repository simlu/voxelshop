package com.vitco.engine.view;

import com.threed.jpct.FrameBuffer;
import com.threed.jpct.IRenderer;
import com.threed.jpct.Matrix;
import com.threed.jpct.SimpleVector;
import com.vitco.async.AsyncAction;
import com.vitco.engine.data.container.ExtendedVector;
import com.vitco.res.VitcoSettings;
import com.vitco.util.G2DUtil;

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
        container.setBorder(BorderFactory.createLineBorder(VitcoSettings.DEFAULT_BORDER_COLOR));

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
                            buffer = new FrameBuffer(container.getWidth(), container.getHeight(), VitcoSettings.SAMPLING_MODE);
                            container.notifyAboutResize(container.getWidth(), container.getHeight());
                            container.doNotSkipNextWorldRender();
                            forceRepaint();
                        }
                    });
                }
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
    }

    // helper
    private boolean drawBoxOutline(float[] center, float[] range,
                                Color color1, Color color2,
                                Graphics2D ig, boolean useFading) {
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
        boolean valid = true;
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
            }
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
                    SimpleVector center = new SimpleVector(
                            voxel[0] + (plane == 2 ? shift : 0),
                            voxel[1] + (plane == 1 ? shift : 0),
                            voxel[2] + (plane == 0 ? shift : 0)
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
            ExtendedVector point2da = convertExt3D2D(line[0]);
            ExtendedVector point2db = convertExt3D2D(line[1]);
            if (point2da != null && point2db != null) {
                ExtendedVector mid = new ExtendedVector(point2da.calcAdd(point2db), 0);
                mid.scalarMul(VitcoSettings.SAMPLING_MODE_DIVIDEND);
                objects.add(new ExtendedVector[] {point2da, point2db, mid});
            }
        }
        // add preview line
        ExtendedVector[] preview_line = data.getPreviewLine();
        boolean connected = preview_line != null && data.areConnected(preview_line[0].id, preview_line[1].id);
        if (preview_line != null && !connected) {
            ExtendedVector point2da = convertExt3D2D(preview_line[0]);
            ExtendedVector point2db = convertExt3D2D(preview_line[1]);
            if (point2da != null && point2db != null) {
                ExtendedVector mid = new ExtendedVector(point2da.calcAdd(point2db), 0);
                mid.scalarMul(VitcoSettings.SAMPLING_MODE_DIVIDEND);
                objects.add(new ExtendedVector[] {point2da, point2db, mid});
            }
        }
        // add points
        for (ExtendedVector point : data.getPoints()) {
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
                    return (int) Math.signum(o1[0].z - o2[2].z);
                } else if (o1.length == 3 && o2.length == 1) { // line and point
                    return (int) Math.signum(o1[2].z - o2[0].z);
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
    private void drawAxeHalf(SimpleVector unitVector, boolean invert, Graphics2D ig, Color innerColor, Color outerColor, float size) {
        G2DUtil.drawLine(
                new SimpleVector(Math.round((invert?-1:1)*unitVector.x*15 + 25), Math.round((invert?-1:1)*unitVector.y*15 + 25), 0),
                new SimpleVector(Math.round((invert?-1:1)*unitVector.x*3 + 25), Math.round((invert?-1:1)*unitVector.y*3 + 25), 0),
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

        // draw axis into corner (top left)
        Matrix matrix = camera.getBack();
        ExtendedVector[] vec = new ExtendedVector[]{
                new ExtendedVector(1,0,0,0),
                new ExtendedVector(0,1,0,1),
                new ExtendedVector(0,0,1,2)
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
                    VitcoSettings.ANIMATION_AXIS_OUTER_COLOR, VitcoSettings.ANIMATION_AXIS_LINE_SIZE);
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
        // draw the bounding box
        if (drawBoundingBox && side == -1) {
            drawBoundingBox(ig);
        }
    }

    // draw so that we can also see the outline of the
    // box in the front (where the textured grid is hidden)
    private void drawBoundingBox(Graphics2D gr) {

        // Anti-alias
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // get an instance that we can modify
        SimpleVector[] vectors = new SimpleVector[8];

        boolean valid = true;
        for (int i = 0; i < vectors.length; i++) {
            // scale and convert the points
            vectors[i] = getVectorsStatic(i);
            vectors[i].scalarMul(VitcoSettings.VOXEL_GROUND_PLANE_SIZE);
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
            gr.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
            for (int i = 0; i < 4; i++) {
                drawFadingLine(gr, vectors, i, (i + 1) % 4, VitcoSettings.BOUNDING_BOX_COLOR, distance, zRange);
                drawFadingLine(gr, vectors, i + 4, (i + 1) % 4 + 4, VitcoSettings.BOUNDING_BOX_COLOR, distance, zRange);
                drawFadingLine(gr, vectors, i, i + 4, VitcoSettings.BOUNDING_BOX_COLOR, distance, zRange);
            }
        }
    }

    // ---------------------------
    
    // constructor
    public DrawContainer(int side) {
        super(side);
        // initialize the drawing buffer
        notifyAboutResize(100, 100);

//        profiler = new Profiler();
//        profiler.createProfile("clearBg", "buffer.clear([COLOR]);");
//        profiler.createProfile("renderScene", "world.renderScene(buffer);");
//        profiler.createProfile("drawWireframe", "world.drawWireframe(buffer, [COLOR]);");
//        profiler.createProfile("drawBuffer", "world.draw(buffer);");
//        profiler.createProfile("drawAsShiftedWireframe", "selectedVoxelsWorld.drawAsShiftedWireframe(buffer, [COLOR], [COLOR]);");
//        profiler.createProfile("bufferUpdate", "buffer.update();");
//        profiler.createProfile("drawLinkedOverlay", "drawLinkedOverlay((Graphics2D) buffer.getGraphics());");
//        profiler.createProfile("bufferDisplay", "buffer.display(gr);");
//
//        profiler.createProfile("drawGhostOverlay", "drawGhostOverlay(gr, cameraChanged, hasResized);");
//        profiler.createProfile("drawAnimationOverlay", "drawAnimationOverlay(gr);");
//        profiler.createProfile("drawVoxelOverlay", "drawVoxelOverlay(gr);");

    }

    //private final Profiler profiler;

    // handle the resize of this container and
    // update all variables accordingly
    public void notifyAboutResize(int width, int height) {
        toDraw = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics gr = toDraw.getGraphics();
        gr.setColor(bgColor);
        gr.fillRect(0, 0, width, height);
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

    // render the content of this container
    public final void render() {
        if (skipNextWorldRender && !doNotSkipNextWorldRender) {
            skipNextWorldRender = false;
        } else {
            if (doNotSkipNextWorldRender) {
                doNotSkipNextWorldRender = false;
                skipNextWorldRender = false;
            }
//            profiler.activateProfile("clearBg");
            buffer.clear(bgColor);
//            profiler.deactivateProfile("clearBg");
            if (drawWorld) {
                refreshVoxels(false);
//                profiler.activateProfile("renderScene");
                world.renderScene(buffer);
//                profiler.deactivateProfile("renderScene");
                if (useWireFrame) {
//                    profiler.activateProfile("drawWireframe");
                    world.drawWireframe(buffer, VitcoSettings.WIREFRAME_COLOR);
//                    profiler.deactivateProfile("drawWireframe");
                } else {
//                    profiler.activateProfile("drawBuffer");
                    world.draw(buffer);
//                    profiler.deactivateProfile("drawBuffer");
                    if (drawSelectedVoxels) { // only draw selected voxels if enables
//                        profiler.activateProfile("drawAsShiftedWireframe");
                        selectedVoxelsWorld.drawAsShiftedWireframe(buffer,
                                VitcoSettings.SELECTED_VOXEL_WIREFRAME_COLOR,
                                VitcoSettings.SELECTED_VOXEL_WIREFRAME_COLOR_SHIFTED);
//                        profiler.deactivateProfile("drawAsShiftedWireframe");
                    }
                }
            }
//            profiler.activateProfile("bufferUpdate");
            buffer.update();
//            profiler.deactivateProfile("bufferUpdate");
            if (drawOverlay) { // overlay part 1
//                profiler.activateProfile("drawLinkedOverlay");
                drawLinkedOverlay((Graphics2D) buffer.getGraphics()); // refreshes with OpenGL
//                profiler.deactivateProfile("drawLinkedOverlay");
            }
        }
        Graphics2D gr = (Graphics2D) toDraw.getGraphics();

//        profiler.activateProfile("bufferDisplay");
        buffer.display(gr);
//        profiler.deactivateProfile("bufferDisplay");

        // draw the under/overlay (voxels in parallel planes)
        if (drawGhostOverlay) {
//            profiler.activateProfile("drawGhostOverlay");
            drawGhostOverlay(gr, cameraChanged, hasResized);
//            profiler.deactivateProfile("drawGhostOverlay");
        }
        if (drawOverlay && drawAnimationOverlay) { // overlay part 2
//            profiler.activateProfile("drawAnimationOverlay");
            drawAnimationOverlay(gr); // refreshes with animation data
//            profiler.deactivateProfile("drawAnimationOverlay");
        }
        if (drawOverlay && drawVoxelOverlay) {
//            profiler.activateProfile("drawVoxelOverlay");
            drawVoxelOverlay(gr);
//            profiler.deactivateProfile("drawVoxelOverlay");
        }

//        // debug
//        if (Main.isDebugMode()) {
//            gr.drawString(String.valueOf(world.getVisibilityList().getSize()), buffer.getWidth() - 40, 20);
//        }

        cameraChanged = false; // camera is current for this redraw
        hasResized = false; // no resize pending

//        profiler.print();
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
