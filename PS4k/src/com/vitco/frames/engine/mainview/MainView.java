package com.vitco.frames.engine.mainview;

import com.threed.jpct.*;
import com.threed.jpct.util.Light;
import com.vitco.frames.ViewPrototype;
import com.vitco.frames.engine.data.animationdata.AnimationDataInterface;
import com.vitco.frames.engine.data.listener.DataChangeListener;
import com.vitco.res.VitcoSettings;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 8/20/12
 * Time: 12:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class MainView extends ViewPrototype implements MainViewInterface {

    // var & setter
    private AnimationDataInterface animationData;
    public void setAnimationData(AnimationDataInterface animationData) {
        this.animationData = animationData;
    }

    // the container that we draw on
    private MPanel container = new MPanel();
    private class MPanel extends JPanel {

        private boolean fullRedraw = true;
        public void setFullRedraw(boolean b) {
            fullRedraw = b;
        }

        // internal - draw a point (takes (x,y))
        protected void drawPoint(int[] point, Graphics2D ig, Color innerColor, Color outerColor) {
            ig.setColor(innerColor);
            ig.fillOval(point[0] - VitcoSettings.ANIMATION_CIRCLE_RADIUS,
                    point[1] - VitcoSettings.ANIMATION_CIRCLE_RADIUS,
                    VitcoSettings.ANIMATION_CIRCLE_RADIUS *2,
                    VitcoSettings.ANIMATION_CIRCLE_RADIUS *2);
            ig.setColor(outerColor);
            ig.drawOval(Math.round(point[0] - VitcoSettings.ANIMATION_CIRCLE_RADIUS),
                    Math.round(point[1] - VitcoSettings.ANIMATION_CIRCLE_RADIUS),
                    VitcoSettings.ANIMATION_CIRCLE_RADIUS * 2,
                    VitcoSettings.ANIMATION_CIRCLE_RADIUS * 2);
        }

//        // internal - draw a point (takes (x,y))
//        private void drawPoint(float[] point, Graphics2D ig, Color innerColor, Color outerColor) {
//            ig.setColor(innerColor);
//            ig.fillOval(Math.round(point[0] - VitcoSettings.ANIMATION_CIRCLE_RADIUS + VitcoSettings.MAIN_VIEW_LINE_OVERLAY_SIZE / 2),
//                    Math.round(point[1] - VitcoSettings.ANIMATION_CIRCLE_RADIUS + VitcoSettings.MAIN_VIEW_LINE_OVERLAY_SIZE / 2),
//                    Math.round(VitcoSettings.ANIMATION_CIRCLE_RADIUS * 2 - VitcoSettings.MAIN_VIEW_LINE_OVERLAY_SIZE),
//                    Math.round(VitcoSettings.ANIMATION_CIRCLE_RADIUS * 2 - VitcoSettings.MAIN_VIEW_LINE_OVERLAY_SIZE));
//            ig.setColor(outerColor);
//            ig.drawOval(Math.round(point[0] - VitcoSettings.ANIMATION_CIRCLE_RADIUS),
//                    Math.round(point[1] - VitcoSettings.ANIMATION_CIRCLE_RADIUS),
//                    VitcoSettings.ANIMATION_CIRCLE_RADIUS * 2,
//                    VitcoSettings.ANIMATION_CIRCLE_RADIUS * 2);
//        }

        // draw overlay on top of the openGL
        private void drawOverlay(Graphics2D ig) {
            // Anti-alias
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            // line settings
            ig.setColor(VitcoSettings.MAIN_VIEW_LINE_OVERLAY_COLOR); // line color
            ig.setStroke(new BasicStroke(VitcoSettings.MAIN_VIEW_LINE_OVERLAY_SIZE, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_BEVEL)); // line size

            // draw all lines
            for (int[][][] line : animationData.getLines()) {
                SimpleVector point1 = Interact2D.project3D2D(camera, buffer,
                        new SimpleVector(line[0][0][0], line[0][0][1], line[0][0][2]));
                SimpleVector point2 = Interact2D.project3D2D(camera, buffer,
                        new SimpleVector(line[1][0][0], line[1][0][1], line[1][0][2]));
                if (point1 != null && point2 != null) {
                    ig.drawLine(Math.round(point1.x / 2), Math.round(point1.y / 2),
                            Math.round(point2.x / 2), Math.round(point2.y / 2));
                }
            }
            // draw highlighted point if there is one
//            int highlighted_point = animationData.getHighlightedPoint();
//            if (highlighted_point != -1) {
//                int[] point3d = animationData.getPoint(highlighted_point)[0];
//                SimpleVector point2d = Interact2D.project3D2D(camera, buffer,
//                        new SimpleVector(point3d[0], point3d[1], point3d[2]));
//                drawPoint(new int[] {Math.round(point2d.x), Math.round(point2d.y)}, ig, Color.RED, Color.BLACK);
//            }
            for (int[][] point : animationData.getPoints()) {
                SimpleVector point2d = Interact2D.project3D2D(camera, buffer,
                        new SimpleVector(point[0][0], point[0][1], point[0][2]));
                if (point2d != null) {
                    this.drawPoint(new int[] {Math.round(point2d.x/2), Math.round(point2d.y/2)}, ig,
                            VitcoSettings.MAIN_VIEW_DOT_OVERLAY_COLOR,
                            VitcoSettings.MAIN_VIEW_LINE_OVERLAY_COLOR);
                }
            }
        }

        // handle the redrawing of this component
        @Override
        protected void paintComponent(Graphics g1) {
            if (fullRedraw) {
                buffer.clear(Color.GRAY);
                world.renderScene(buffer);
                world.draw(buffer);
                buffer.update();
            }
            buffer.display(g1);
            drawOverlay((Graphics2D) g1);
        }
    }

    // the world required objects
    private World world;
    private FrameBuffer buffer = new FrameBuffer(100, 100,
            FrameBuffer.SAMPLINGMODE_OGSS);
    private Camera camera;

    @Override
    @PostConstruct
    public void init() {

    }

    @Override
    @PreDestroy
    public void finish() {
        buffer.disableRenderer(IRenderer.RENDERER_OPENGL);
        buffer.dispose();
    }

    @Override
    public void build(final JComponent frame) {

//        Config.useMultipleThreads=true;
//        Config.useMultiThreadedBlitting=true;
//        Config.maxNumberOfCores=Math.min(4,Runtime.getRuntime().availableProcessors());
        //Config.mtDebug=true;

        //Config.maxPolysVisible = 100000;

        // set up worlds
        world = new World();
        camera = world.getCamera();

        world.setAmbientLight(50, 50, 50);

        // light sources
        Light light = new Light(world);
        light.setAttenuation(1000); // high ~ light shines far
        light.setDiscardDistance(-1);
        light.setIntensity(3, 3, 3);
        light.setPosition(new SimpleVector(-200, -1300, -200));

        light = new Light(world);
        light.setAttenuation(1000); // high ~ light shines far
        light.setDiscardDistance(-1);
        light.setIntensity(3, 3, 3);
        light.setPosition(new SimpleVector(200, 1300, 200));

        light = new Light(world);
        light.setAttenuation(1000); // high ~ light shines far
        light.setDiscardDistance(-1);
        light.setIntensity(1, 1, 1);
        light.setPosition(new SimpleVector(1300, 200, 200));

        light = new Light(world);
        light.setAttenuation(1000); // high ~ light shines far
        light.setDiscardDistance(-1);
        light.setIntensity(1, 1, 1);
        light.setPosition(new SimpleVector(-1300, -200, -200));

//        // draw something for testing
//        Random rand = new Random();
//        int bigcubesize = 30;
//        for (int i = -bigcubesize; i <= bigcubesize; i++) {
//            for (int j = -bigcubesize; j <= bigcubesize; j++) {
//                for (int k = -bigcubesize; k <= bigcubesize; k++) {
//                    if (rand.nextInt(100) == 1) {
//                        Object3D box = Primitives.getCube(1f);
//                        box.setAdditionalColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
//                        box.setEnvmapped(Object3D.ENVMAP_ENABLED);
//                        box.setShadingMode(Object3D.SHADING_FAKED_FLAT);
//                        //box.setTransparency(100);
//                        box.translate(i * 2, k*2, j * 2);
//                        box.rotateY((float) Math.PI / 4);
//                        box.build();
//                        world.addObject(box);
//                    }
//                }
//            }
//        }

        Random rand = new Random();
        for (int i = -1; i < 2; i+=2) {
            for (int j = -1; j < 2; j+=2) {
                for (int k = -1; k < 2; k+=2) {
                        Object3D box = Primitives.getCube(10f);
                        box.setAdditionalColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
                        box.setEnvmapped(Object3D.ENVMAP_ENABLED);
                        box.setShadingMode(Object3D.SHADING_FAKED_FLAT);
                        //box.setTransparency(100);
                        box.translate(i * 50, k*50, j * 50);
                        box.rotateY((float) Math.PI / 4);
                        box.build();
                        world.addObject(box);
                }
            }
        }

        // user mouse input - change camera position
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) { // scroll = zoom in and out
                if (e.getWheelRotation() == -1) {
                    if (camera.getPosition().distance(SimpleVector.ORIGIN) > VitcoSettings.MAIN_VIEW_ZOOM_IN_LIMIT) {
                        camera.moveCamera(Camera.CAMERA_MOVEIN, VitcoSettings.MAIN_VIEW_ZOOM_SPEED);
                    }
                } else {
                    if (camera.getPosition().distance(new SimpleVector(0,0,0)) < VitcoSettings.MAIN_VIEW_ZOOM_OUT_LIMIT) {
                        camera.moveCamera(Camera.CAMERA_MOVEIN, -VitcoSettings.MAIN_VIEW_ZOOM_SPEED);
                    }
                }
                container.repaint();
            }

            private Point leftMouseDown = null;
            private Point rightMouseDown = null;

            @Override
            public void mousePressed(MouseEvent e) {
                switch (e.getButton()) {
                    case 1: leftMouseDown = e.getPoint(); break;
                    case 3: rightMouseDown = e.getPoint(); break;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                switch (e.getButton()) {
                    case 1: leftMouseDown = null; break;
                    case 3: rightMouseDown = null; break;
                }
            }

            private final float[] amountShifted = new float[2];

            @Override
            public void mouseDragged(MouseEvent e) {
                if (leftMouseDown != null) {
                    camera.moveCamera(
                            Camera.CAMERA_MOVELEFT,
                            -amountShifted[0]
                    );
                    camera.moveCamera(
                            Camera.CAMERA_MOVEUP,
                            -amountShifted[1]
                    );
                    float dist = camera.getPosition().distance(SimpleVector.ORIGIN);
                    camera.moveCamera(
                            Camera.CAMERA_MOVEIN,
                            dist
                    );
                    //new SimpleVector().
                    camera.rotateAxis(camera.getYAxis(), (e.getX() -
                            leftMouseDown.x)*VitcoSettings.MAIN_VIEW_ROTATION_Y_FACTOR);
                    camera.rotateX(-(e.getY() -
                            leftMouseDown.y)*VitcoSettings.MAIN_VIEW_ROTATION_X_FACTOR);
                    leftMouseDown.x = e.getX();
                    leftMouseDown.y = e.getY();
                    camera.moveCamera(
                            Camera.CAMERA_MOVEOUT,
                            dist
                    );
                    camera.moveCamera(
                            Camera.CAMERA_MOVEUP,
                            amountShifted[1]
                    );
                    camera.moveCamera(
                            Camera.CAMERA_MOVELEFT,
                            amountShifted[0]
                    );
                    container.repaint();
                } else if (rightMouseDown != null) {
                    amountShifted[0] += (e.getX() -
                            rightMouseDown.x)*VitcoSettings.MAIN_VIEW_SIDE_MOVE_FACTOR;
                    camera.moveCamera(Camera.CAMERA_MOVELEFT,
                            (e.getX() - rightMouseDown.x)*VitcoSettings.MAIN_VIEW_SIDE_MOVE_FACTOR);
                    rightMouseDown.x = e.getX();

                    amountShifted[1] += (e.getY() -
                            rightMouseDown.y)*VitcoSettings.MAIN_VIEW_SIDE_MOVE_FACTOR;
                    camera.moveCamera(Camera.CAMERA_MOVEUP,
                            (e.getY() - rightMouseDown.y)*VitcoSettings.MAIN_VIEW_SIDE_MOVE_FACTOR);
                    rightMouseDown.y = e.getY();

                    container.repaint();
                }
            }
        };
        container.addMouseWheelListener(mouseAdapter);
        container.addMouseMotionListener(mouseAdapter);
        container.addMouseListener(mouseAdapter);

        // camera initial position
        camera.setPosition(VitcoSettings.MAIN_VIEW_CAMERA_POSITION_X,
                VitcoSettings.MAIN_VIEW_CAMERA_POSITION_Y,
                VitcoSettings.MAIN_VIEW_CAMERA_POSITION_Z);
        camera.lookAt(SimpleVector.ORIGIN);

        // register reset action
        actionManager.registerAction("reset_main_view_camera", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.setPosition(VitcoSettings.MAIN_VIEW_CAMERA_POSITION_X,
                        VitcoSettings.MAIN_VIEW_CAMERA_POSITION_Y,
                        VitcoSettings.MAIN_VIEW_CAMERA_POSITION_Z);
                camera.lookAt(SimpleVector.ORIGIN);
                container.repaint();
            }
        });

        // register size change of parent, update container
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (container.getWidth() > 0 && container.getHeight() > 0) {
                    buffer.dispose();
                    buffer = new FrameBuffer(container.getWidth(), container.getHeight(),
                                        FrameBuffer.SAMPLINGMODE_OGSS);
                    container.repaint();
                }
            }
        });

        // register redraw on animation data change
        animationData.addDataChangeListener(new DataChangeListener() {
            @Override
            public void onAnimationDataChanged() {
                container.setFullRedraw(false);
                container.repaint();
                container.setFullRedraw(true);
            }

            @Override
            public void onAnimationSelectionChanged() {
                onAnimationDataChanged();
            }

            @Override
            public void onFrameDataChanged() {
                //...
            }

            @Override
            public void onVoxelDataChanged() {
                //...
            }
        });

        frame.add(container);
    }

}
