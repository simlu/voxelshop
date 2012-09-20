package com.vitco.engine;

import com.threed.jpct.*;
import com.vitco.engine.data.Data;
import com.vitco.engine.data.container.ExtendedVector;
import com.vitco.logic.ViewPrototype;
import com.vitco.res.VitcoSettings;
import com.vitco.util.G2DUtil;
import com.vitco.util.thread.LifeTimeThread;
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
import java.util.*;

/**
 * Rendering functionality of this World (data + overlay)
 *
 * Can switch each of them on/off. Defines the basic objects: data, world, buffer, camera.
 */
public abstract class EngineViewPrototype extends ViewPrototype {
    // var & setter
    protected Data data;
    @Autowired
    public void setData(Data data) {
        this.data = data;
    }

    private ThreadManagerInterface threadManager;
    // set the action handler
    @Autowired
    public void setThreadManager(ThreadManagerInterface threadManager) {
        this.threadManager = threadManager;
    }

    // the world-required objects
    protected final World world;
    protected FrameBuffer buffer;
    protected final CCamera camera;

    // holds all the instances of this class
    private static final ArrayList<EngineViewPrototype> derived = new ArrayList<EngineViewPrototype>();
    // repaints all instances of this class
    protected final void repaintAll() {
        for (EngineViewPrototype evp : derived) {
            evp.forceRepaint();
        }
    }
    public final void setBGColor(Color color) {
        data.setViewBgColor(color);
        repaintAll();
    }

    // conversion
    protected final SimpleVector convert2D3D(int x, int y, SimpleVector referencePoint) {
        SimpleVector result = Interact2D.reproject2D3DWS(camera, buffer, x*2, y*2).normalize();
        result.scalarMul(camera.getPosition().distance(referencePoint));
        result.add(camera.getPosition());
        return result;
    }

    // conversion
    protected final SimpleVector convert3D2D(SimpleVector point) {
        SimpleVector result = Interact2D.project3D2D(camera, buffer, point);
        if (result != null) {
            result.scalarMul(0.5f);
        }
        return result;
    }

    // conversion
    protected final ExtendedVector convertExt3D2D(ExtendedVector point) {
        ExtendedVector result = null;
        SimpleVector point2d = Interact2D.project3D2D(camera, buffer, point);
        if (point2d != null) {
            point2d.scalarMul(0.5f);
            result = new ExtendedVector(point2d, point.id);
        }
        return result;
    }

    // updates the world with voxels
    protected abstract void updateWorldWithVoxels();
    // true iff the world does not need to be updated with voxels
    private boolean worldVoxelCurrent = false;
    // force update of world before next draw
    protected final void invalidateVoxels() {
        if (localMouseDown) { // instant update needed for interaction
            updateWorldWithVoxels();
            worldVoxelCurrent = true;
        } else {
            worldVoxelCurrent = false;
        }
    }

    // the container that we draw on
    protected final MPanel container = new MPanel();
    protected final class MPanel extends JPanel {

        // this draws opengl content if enabled
        private boolean drawWorld = true;
        public void setDrawWorld(boolean b) {
            drawWorld = b;
        }

        // this enabled/disables all overlay
        private boolean drawOverlay = true;
        public void setDrawOverlay(boolean b) {
            drawOverlay = b;
        }

        // enabled/ disables animation overlay
        private boolean drawAnimationOverlay = true;
        public void setDrawAnimationOverlay(boolean b) {
            drawAnimationOverlay = b;
        }

        // enabled/ disables animation overlay
        private boolean drawVoxelOverlay = true;
        public void setDrawVoxelOverlay(boolean b) {
            drawVoxelOverlay = b;
        }

        // for the next refresh do not update the world (OpenGL render)
        private boolean skipNextWorldRender = false;
        public void skipNextWorldRender() {
            skipNextWorldRender = true;
        }
        // prevent skipping of next world view
        private boolean doNotSkipNextWorldRender = false;
        public void doNotSkipNextWorldRender() {
            doNotSkipNextWorldRender = true;
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

            final int[] voxel = data.getHighlightedVoxel();
            // draw selected voxel (ghost / preview voxel)
            if (voxel != null) {
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
                        drawCubeLine(ig, vectors, i, (i + 1) % 4, VitcoSettings.VOXEL_PREVIEW_LINE_COLOR, distance, zRange);
                        drawCubeLine(ig, vectors, i + 4, (i + 1) % 4 + 4, VitcoSettings.VOXEL_PREVIEW_LINE_COLOR, distance, zRange);
                        drawCubeLine(ig, vectors, i, i + 4, VitcoSettings.VOXEL_PREVIEW_LINE_COLOR, distance, zRange);
                    }

                    // draw the highlighted side
                    int side = data.getPreviewPlane();
                    if (side != -1) {
                        // calculate center and some variables
                        int RANGE = 4;
                        float shift = (side%2 == 0 ? 0.5f : -0.5f);
                        int plane = side / 2;
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
                            Color transColor = new Color(VitcoSettings.VOXEL_PREVIEW_LINE_COLOR.getRed(),
                                    VitcoSettings.VOXEL_PREVIEW_LINE_COLOR.getGreen(),
                                    VitcoSettings.VOXEL_PREVIEW_LINE_COLOR.getBlue(),
                                    0);
                            for (int i = 0, len = points.length/4; i < len; i++) {

                                float alpha = (halfLen-Math.abs(i-halfLen))/halfLen;
                                Color visColor = new Color(
                                        VitcoSettings.VOXEL_PREVIEW_LINE_COLOR.getRed(),
                                        VitcoSettings.VOXEL_PREVIEW_LINE_COLOR.getGreen(),
                                        VitcoSettings.VOXEL_PREVIEW_LINE_COLOR.getBlue(),
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
                    mid.scalarMul(0.5f);
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
                    mid.scalarMul(0.5f);
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

        // handle the redrawing of this component
        @Override
        protected final void paintComponent(Graphics g1) {
            if (skipNextWorldRender && !doNotSkipNextWorldRender) {
                skipNextWorldRender = false;
            } else {
                if (doNotSkipNextWorldRender) {
                    doNotSkipNextWorldRender = false;
                    skipNextWorldRender = false;
                }
                buffer.clear(data.getViewBgColor());
                if (drawWorld) {
                    if (!worldVoxelCurrent) {
                        updateWorldWithVoxels();
                        worldVoxelCurrent = true;
                    }
                    world.renderScene(buffer);
                    world.draw(buffer);
                }
                buffer.update();
                if (drawOverlay) { // overlay part 1
                    drawLinkedOverlay((Graphics2D) buffer.getGraphics()); // refreshes with OpenGL
                }
            }
            buffer.display(g1);
            if (drawOverlay && drawAnimationOverlay) { // overlay part 2
                drawAnimationOverlay((Graphics2D) g1); // refreshes with animation data
            }
            if (drawOverlay && drawVoxelOverlay) {
                drawVoxelOverlay((Graphics2D) g1);
            }
        }
    }

    private boolean needRepaint = true;
    protected final void forceRepaint() {
        needRepaint = true;
    }

    @PostConstruct
    public final void startup() {
        // manages the repainting of this container instance
        threadManager.manage(new LifeTimeThread() {
            @Override
            public void loop() throws InterruptedException {
                if (needRepaint) {
                    needRepaint = false;
                    container.repaint();
                }
                sleep(33); // about 25 fps
                // stop repaint when mouse is down on another view
                while (globalMouseDown && !localMouseDown) {
                    sleep(50);
                }
            }
        });
    }

    @PreDestroy
    public final void cleanup() {
        buffer.disableRenderer(IRenderer.RENDERER_OPENGL);
        buffer.dispose();
    }

    private boolean localMouseDown = false;
    private static boolean globalMouseDown = false;
    private static boolean initialized = false;
    protected EngineViewPrototype() {

        // only perform these actions once (even if the class is instantiated several times)
        if (!initialized) {
            Config.maxPolysVisible = 10000;
            Logger.setLogLevel(Logger.ERROR);
            initialized = true;
        }

        // register this instance
        derived.add(this);

        container.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                globalMouseDown = true;
                localMouseDown = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                globalMouseDown = false;
                localMouseDown = false;
            }
        });

        // set up world objects
        world = new World();
        camera = new CCamera();
        world.setCameraTo(camera);
        buffer = new FrameBuffer(100, 100, FrameBuffer.SAMPLINGMODE_OGSS);

        // lighting (1,1,1) = true color
        world.setAmbientLight(1, 1, 1);

        // add a border to our view
        container.setBorder(BorderFactory.createLineBorder(VitcoSettings.DEFAULT_BORDER_COLOR));

        // register size change of container and change buffer size accordingly
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (container.getWidth() > 0 && container.getHeight() > 0) {
                    buffer.dispose();
                    buffer = new FrameBuffer(container.getWidth(), container.getHeight(), FrameBuffer.SAMPLINGMODE_OGSS);
                    forceRepaint();
                }
            }
        });

    }
}
