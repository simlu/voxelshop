package com.vitco.engine;

import com.threed.jpct.*;
import com.vitco.async.AsyncAction;
import com.vitco.async.AsyncActionManager;
import com.vitco.engine.data.Data;
import com.vitco.engine.data.container.ExtendedVector;
import com.vitco.engine.data.container.Voxel;
import com.vitco.logic.ViewPrototype;
import com.vitco.res.VitcoSettings;
import com.vitco.util.G2DUtil;
import com.vitco.util.SwingAsyncHelper;
import com.vitco.util.pref.PrefChangeListener;
import com.vitco.util.thread.ThreadManagerInterface;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Rendering functionality of this World (data + overlay)
 *
 * Can switch each of them on/off. Defines the basic objects: data, world, buffer, camera.
 */
public abstract class EngineViewPrototype extends ViewPrototype {

    protected AsyncActionManager asyncActionManager;
    @Autowired
    public final void setAsyncActionManager(AsyncActionManager asyncActionManager) {
        this.asyncActionManager = asyncActionManager;
    }

    // var & setter
    protected Data data;
    @Autowired
    public final void setData(Data data) {
        this.data = data;
    }

    protected ThreadManagerInterface threadManager;
    // set the action handler
    @Autowired
    public final void setThreadManager(ThreadManagerInterface threadManager) {
        this.threadManager = threadManager;
    }

    // the world-required objects
    protected final CWorld world;
    protected final CWorld selectedVoxelsWorld;
    protected FrameBuffer buffer = new FrameBuffer(100, 100, VitcoSettings.SAMPLING_MODE);
    protected final CCamera camera;

    // conversion
    protected final SimpleVector convert2D3D(int x, int y, SimpleVector referencePoint) {
        SimpleVector result = Interact2D.reproject2D3DWS(camera, buffer,
                (int)Math.round(x*VitcoSettings.SAMPLING_MODE_MULTIPLICAND),
                (int)Math.round(y*VitcoSettings.SAMPLING_MODE_MULTIPLICAND)).normalize();
        result.scalarMul(camera.getPosition().distance(referencePoint));
        result.add(camera.getPosition());
        return result;
    }

    // conversion
    protected final SimpleVector convert3D2D(SimpleVector point) {
        SimpleVector result = Interact2D.project3D2D(camera, buffer, point);
        if (result != null) {
            result.scalarMul(VitcoSettings.SAMPLING_MODE_DIVIDEND);
        }
        return result;
    }

    // conversion
    protected final ExtendedVector convertExt3D2D(ExtendedVector point) {
        ExtendedVector result = null;
        SimpleVector point2d = Interact2D.project3D2D(camera, buffer, point);
        if (point2d != null) {
            point2d.scalarMul(VitcoSettings.SAMPLING_MODE_DIVIDEND);
            result = new ExtendedVector(point2d, point.id);
        }
        return result;
    }

    // ==============================
    // updating of world with voxels
    // ==============================


    // overlay ghost voxels
    protected abstract SimpleVector[][] getGhostOverlay();
    // true if overlay has changed since last call
    protected abstract boolean updateGhostOverlay();

    private boolean drawGhostOverlay = false;
    public final void setDrawGhostOverlay(boolean b) {
        drawGhostOverlay = b;
    }

    // voxel data getter to be defined
    protected abstract Voxel[] getVoxels();

    // voxel data getter to be defined (for changed voxels since last call)
    protected abstract Voxel[][] getChangedVoxels();

    // changed selected voxels since last call
    protected abstract Voxel[][] getChangedSelectedVoxels();

    // helper - make sure the voxel objects in the world are up to date
    // and also trigger refresh for redraws
    private void updateWorldWithVoxels() {
        // only retrieve the changed voxels
        Voxel[][] changed = getChangedVoxels();
        if (changed[0] == null) { // rebuild
            world.clear();
        } else {
            // remove individual voxel
            for (Voxel remove : changed[0]) {
                world.clearPosition(remove.getPosAsInt());
            }
        }
        for (Voxel added : changed[1]) {
            world.updateVoxel(added);
        }
        asyncActionManager.addAsyncAction(new AsyncAction("asyncWorld" + side) {
            @Override
            public void performAction() {
                container.doNotSkipNextWorldRender();
                forceRepaint();
                if (!world.refreshWorld()) {
                    asyncActionManager.addAsyncAction(this);
                }
            }
        });

        // only retrieve the changed voxels
        changed = getChangedSelectedVoxels();
        if (changed[0] == null) { // rebuild
            selectedVoxelsWorld.clear();
        } else {
            // remove individual voxel
            for (Voxel remove : changed[0]) {
                selectedVoxelsWorld.clearPosition(remove.getPosAsInt());
            }
        }
        for (Voxel added : changed[1]) {
            selectedVoxelsWorld.updateVoxel(added);
        }
        asyncActionManager.addAsyncAction(new AsyncAction("asyncSelWorld" + side) {
            @Override
            public void performAction() {
                container.doNotSkipNextWorldRender();
                forceRepaint();
                if (!selectedVoxelsWorld.refreshWorld()) {
                    asyncActionManager.addAsyncAction(this);
                }
            }
        });
    }

    // true iff the world does not need to be updated with voxels
    private boolean worldVoxelCurrent = false;
    // force update of world before next draw
    protected final void invalidateVoxels() {
        if (localMouseDown) { // instant update needed for interaction
            refreshVoxels(true);
        } else {
            worldVoxelCurrent = false;
        }
    }

    // force true: make sure the voxels are valid "right now", no matter what
    private void refreshVoxels(boolean force) {
        if (!worldVoxelCurrent || force) {
            worldVoxelCurrent = true;
            updateWorldWithVoxels();
        }
    }

    // ==============================
    // END: updating of world with voxels
    // ==============================

    // set wireframe mode
    private boolean useWireFrame = false;
    protected final void useWireFrame(boolean b) {
        useWireFrame = b;
    }

    // the container that we draw on
    protected final MPanel container = new MPanel();

    protected final class MPanel extends JPanel {

        // bg color of this panel
        private Color bgColor = VitcoSettings.ANIMATION_BG_COLOR;
        // the current preview plane
        private int previewPlane = 0;

        // true iff camera has changed since last draw call
        private boolean cameraChanged = true;
        // true iff container has resized since last draw call
        private boolean hasResized = true;

        // initialize
        public void init() {
            // register bg color change
            preferences.addPrefChangeListener("engine_view_bg_color", new PrefChangeListener() {
                @Override
                public void onPrefChange(Object newValue) {
                    bgColor = (Color)newValue;
                    forceRepaint();
                }
            });
            // register preview plane change
            preferences.addPrefChangeListener("engine_view_voxel_preview_plane", new PrefChangeListener() {
                @Override
                public void onPrefChange(Object newValue) {
                    previewPlane = (Integer)newValue;
                }
            });
            // register camera change listener
            camera.addCameraChangeListener(new CameraChangeListener() {
                @Override
                public void onCameraChange() {
                    cameraChanged = true;
                }
            });
        }

        // this draws jpct engine content if enabled
        private boolean drawWorld = true;
        public final void setDrawWorld(boolean b) {
            drawWorld = b;
        }

        // this enables/disables drawing of selected voxels
        private boolean drawSelectedVoxels = true;
        public final void setDrawSelectedVoxels(boolean b) {
            drawSelectedVoxels = b;
        }

        // this enabled/disables all overlay
        private boolean drawOverlay = true;
        public final void setDrawOverlay(boolean b) {
            drawOverlay = b;
        }

        // enabled/ disables animation overlay
        private boolean drawAnimationOverlay = true;
        public final void setDrawAnimationOverlay(boolean b) {
            drawAnimationOverlay = b;
        }

        // enabled/ disables animation overlay
        private boolean drawVoxelOverlay = true;
        public final void setDrawVoxelOverlay(boolean b) {
            drawVoxelOverlay = b;
        }

        // for the next refresh do not update the world (OpenGL render)
        private boolean skipNextWorldRender = false;
        public final void skipNextWorldRender() {
            skipNextWorldRender = true;
        }
        // prevent skipping of next world view
        private boolean doNotSkipNextWorldRender = false;
        public final void doNotSkipNextWorldRender() {
            doNotSkipNextWorldRender = true;
        }

        // to set the preview rect
        private Rectangle previewRect = null;
        public final void setPreviewRect(Rectangle rect) {
            previewRect = rect;
        }

        // wrapper
        private void drawCubeLine(Graphics2D ig, SimpleVector[] vectors, int i, int j, Color color, float distance, float[] zRange) {
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

        // draw overlay for voxels
        private void drawVoxelOverlay(Graphics2D ig) {
            // Anti-alias
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // draw the select rect
            if (previewRect != null) {
                ig.setColor(Color.WHITE);
                ig.drawRect(previewRect.x, previewRect.y, previewRect.width, previewRect.height);
            }

            // draw the preview voxel
            int[] voxel = data.getHighlightedVoxel();
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
                // define the points of the voxel
                SimpleVector[] vectors = new SimpleVector[] {
                        new SimpleVector(voxel[0] + 0.5, voxel[1] + 0.5, voxel[2] + 0.5),
                        new SimpleVector(voxel[0] + 0.5, voxel[1] + 0.5, voxel[2] - 0.5),
                        new SimpleVector(voxel[0] + 0.5, voxel[1] - 0.5, voxel[2] - 0.5),
                        new SimpleVector(voxel[0] + 0.5, voxel[1] - 0.5, voxel[2] + 0.5),
                        new SimpleVector(voxel[0] - 0.5, voxel[1] + 0.5, voxel[2] + 0.5),
                        new SimpleVector(voxel[0] - 0.5, voxel[1] + 0.5, voxel[2] - 0.5),
                        new SimpleVector(voxel[0] - 0.5, voxel[1] - 0.5, voxel[2] - 0.5),
                        new SimpleVector(voxel[0] - 0.5, voxel[1] - 0.5, voxel[2] + 0.5)
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
                    for (int i = 0; i < 4; i++) {
                        drawCubeLine(ig, vectors, i, (i + 1) % 4, previewColor, distance, zRange);
                        drawCubeLine(ig, vectors, i + 4, (i + 1) % 4 + 4, previewColor, distance, zRange);
                        drawCubeLine(ig, vectors, i, i + 4, previewColor, distance, zRange);
                    }

                    // draw the highlighted side
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
                        return (int)Math.signum(o1[2].z - o2[2].z);
                    } else if (o1.length == 1 && o2.length == 1) { // two points
                        return (int)Math.signum(o1[0].z - o2[0].z);
                    } else if (o1.length == 1 && o2.length == 3) { // point and line
                        return (int)Math.signum(o1[0].z - o2[2].z);
                    } else if (o1.length == 3 && o2.length == 1) { // line and point
                        return (int)Math.signum(o1[2].z - o2[0].z);
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
                    return (int)Math.signum(o1.z - o2.z);
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
            ig.setColor(VitcoSettings.ANIMATION_CENTER_CROSS_COLOR);
            ig.setStroke(new BasicStroke(1.0f));
            SimpleVector center = convert3D2D(new SimpleVector(0,0,0));
            if (center != null) {
                ig.drawLine(Math.round(center.x - 5), Math.round(center.y), Math.round(center.x + 5), Math.round(center.y));
                ig.drawLine(Math.round(center.x), Math.round(center.y - 5), Math.round(center.x), Math.round(center.y + 5));
            }
        }

        private BufferedImage toDraw = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        private BufferedImage overlayBuffer = new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB);
        private Graphics2D overlayBufferGraphics = (Graphics2D) overlayBuffer.getGraphics();
        public void notifyAboutResize(int width, int height) {
            toDraw = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics gr = toDraw.getGraphics();
            gr.setColor(bgColor);
            gr.drawRect(0, 0, width, height);
            // the overlay
            overlayBuffer = new BufferedImage(buffer.getOutputWidth(), buffer.getOutputHeight(), BufferedImage.TYPE_INT_ARGB);
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

        protected final void render() {
            if (skipNextWorldRender && !doNotSkipNextWorldRender) {
                skipNextWorldRender = false;
            } else {
                if (doNotSkipNextWorldRender) {
                    doNotSkipNextWorldRender = false;
                    skipNextWorldRender = false;
                }
                buffer.clear(bgColor);
                if (drawWorld) {
                    refreshVoxels(false);
                    world.renderScene(buffer);
                    if (useWireFrame) {
                        world.drawWireframe(buffer, VitcoSettings.WIREFRAME_COLOR);
                    } else {
                        world.draw(buffer); // takes time (!) ~ 60ms
                        if (drawSelectedVoxels) { // only draw selected voxels if enables
                            selectedVoxelsWorld.drawAsShiftedWireframe(buffer,
                                    VitcoSettings.SELECTED_VOXEL_WIREFRAME_COLOR,
                                    VitcoSettings.SELECTED_VOXEL_WIREFRAME_COLOR_SHIFTED);
                        }
                    }
                }
                buffer.update(); // takes time (!) ~ 10ms
                if (drawOverlay) { // overlay part 1
                    drawLinkedOverlay((Graphics2D) buffer.getGraphics()); // refreshes with OpenGL
                }
            }
            Graphics2D gr = (Graphics2D) toDraw.getGraphics();

            buffer.display(gr);

            // draw the under/overlay (voxels in parallel planes)
            if (drawGhostOverlay) {
                drawGhostOverlay(gr, cameraChanged, hasResized);
            }
            if (drawOverlay && drawAnimationOverlay) { // overlay part 2
                drawAnimationOverlay(gr); // refreshes with animation data
            }
            if (drawOverlay && drawVoxelOverlay) {
                drawVoxelOverlay(gr);
            }
            cameraChanged = false; // camera is current for this redraw
            hasResized = false; // no resize pending
        }


        // handle the redrawing of this component
        // note: this MUSTN'T have any call to synchronized
        @Override
        protected final void paintComponent(Graphics g1) {
            super.paintComponent(g1);
            if (toDraw != null) {
                g1.drawImage(toDraw, 0, 0, null);
            } else {
                g1.setColor(bgColor);
                g1.fillRect(0, 0, this.getWidth(), this.getHeight());
            }
            repainting = false;
            if (needRepainting) {
                needRepainting = false;
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

    // makes sure the repaint doesn't get called too often
    // (only one in queue at a time)
    private boolean repainting = false;
    private boolean needRepainting = false;

    protected final void forceRepaint() {
        if (!repainting) {
            repainting = true;
            asyncActionManager.addAsyncAction(new AsyncAction("repaint" + side) {
                @Override
                public void performAction() {
                    container.render();
                    // this is thread save!
                    container.repaint();
                }

                @Override
                public boolean ready() {
                    return !globalMouseDown || localMouseDown;
                }
            });
        } else {
            needRepainting = true;
        }
    }

    @PostConstruct
    public final void startup() {
        // initialize the container
        container.init();
    }

    @PreDestroy
    public final void cleanup() {
        buffer.disableRenderer(IRenderer.RENDERER_OPENGL);
        buffer.dispose();
    }

    // only perform these actions once (even if the class is instantiated several times)
    static {
        Config.fadeoutLight = false;
        Config.maxPolysVisible = 5000;

        Config.useMultipleThreads = true;
        Config.maxNumberOfCores = Runtime.getRuntime().availableProcessors();
        Config.loadBalancingStrategy = 1; // default 0
        // usually not worth it (http://www.jpct.net/doc/com/threed/jpct/Config.html#useMultiThreadedBlitting)
        //Config.useMultiThreadedBlitting = false;   //default false

        Config.mipmap = true;

        //Config.mtDebug = true;
        Logger.setLogLevel(Logger.ERROR);
    }

    protected final int side;
    private boolean localMouseDown = false;
    private static boolean globalMouseDown = false;
    // constructor
    protected EngineViewPrototype(Integer side) {
        // make sure side defaults to -1
        if (side == null || side < 0 || side > 2) {
            side = -1;
        }
        this.side = side;

        container.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        globalMouseDown = true;
                        localMouseDown = true;
                    }
                });
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        globalMouseDown = false;
                        localMouseDown = false;
                    }
                });
            }
        });

        // set up world objects
        world = new CWorld(true, side);
        // no culling, since we want to see all selected voxels
        selectedVoxelsWorld = new CWorld(false, side);
        camera = new CCamera();
        world.setCameraTo(camera);
        selectedVoxelsWorld.setCameraTo(camera);
        // lighting (1,1,1) = true color
        world.setAmbientLight(1, 1, 1);

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
}
