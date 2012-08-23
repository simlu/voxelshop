package com.vitco.frames.engine;

import com.threed.jpct.*;
import com.vitco.frames.ViewPrototype;
import com.vitco.frames.engine.data.animationdata.AnimationDataInterface;
import com.vitco.res.VitcoSettings;
import com.vitco.util.G2DUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Rendering functionality of this World (data + overlay)
 *
 * Can switch each of them on/off. Defines the basic objects: data, world, buffer, camera.
 */
public abstract class EngineViewPrototype extends ViewPrototype {
    // var & setter
    protected AnimationDataInterface animationData;
    @Autowired
    public void setAnimationData(AnimationDataInterface animationData) {
        this.animationData = animationData;
    }

    // the world-required objects
    protected final World world;
    protected FrameBuffer buffer = new FrameBuffer(100, 100, FrameBuffer.SAMPLINGMODE_OGSS);
    protected final CCamera camera;

    // conversion
    public final SimpleVector convert2D3D(int x, int y, SimpleVector referencePoint) {
        SimpleVector result = Interact2D.reproject2D3DWS(camera, buffer, x*2, y*2).normalize();
        result.scalarMul(camera.getPosition().distance(referencePoint));
        result.add(camera.getPosition());
        return result;
    }

    // conversion
    public final SimpleVector convert3D2D(float[] point) {
        SimpleVector result = Interact2D.project3D2D(camera, buffer,
                new SimpleVector(point[0], point[1], point[2]));
        result.scalarMul(0.5f);
        return result;
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

        // for the next refresh do not update the
        // world (OpenGL render)
        private boolean skipNextWorldRender = false;
        public void skipNextWorldRender() {
            skipNextWorldRender = true;
        }

        // wrapper
        private void drawLine(float[][] p1, float[][] p2, Graphics2D ig, Color innerColor, Color outerColor, float size) {
            SimpleVector point1 = convert3D2D(p1[0]);
            SimpleVector point2 = convert3D2D(p2[0]);
            if (point1 != null && point2 != null) {
                G2DUtil.drawLine(point1, point2, ig, innerColor, outerColor, size);
            }
        }

        // wrapper
        private void drawPoint(float[] point, Graphics2D ig, Color innerColor, Color outerColor, float radius, float borderSize) {
            SimpleVector point2d = convert3D2D(point);
            if (point2d != null) {
                G2DUtil.drawPoint(point2d, ig, innerColor, outerColor, radius, borderSize);
            }
        }

        // draw dynamic overlay on top of the openGL
        private void drawAnimationOverlay(Graphics2D ig) {
            // Anti-alias
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            // draw all lines
            for (float[][][] line : animationData.getLines()) { // note: lines and points are buffered internally by AnimationDataCore(!)
                drawLine(line[0], line[1], ig,
                        VitcoSettings.ANIMATION_LINE_INNER_COLOR, VitcoSettings.ANIMATION_LINE_OUTER_COLOR,
                        VitcoSettings.ANIMATION_LINE_SIZE);
            }

            // draw preview line
            float[][][] preview_line = animationData.getPreviewLine();
            if (preview_line != null) {
                boolean connected = animationData.areConnected((int)preview_line[0][1][0], (int)preview_line[1][1][0]);
                drawLine(preview_line[0], preview_line[1], ig,
                        connected ? VitcoSettings.ANIMATION_LINE_PREVIEW_REMOVE_COLOR : VitcoSettings.ANIMATION_LINE_PREVIEW_ADD_COLOR,
                        VitcoSettings.ANIMATION_LINE_OUTER_COLOR,
                        VitcoSettings.ANIMATION_LINE_SIZE);
            }

            // draw all points
            int selected_point = animationData.getSelectedPoint();
            int highlighted_point = animationData.getHighlightedPoint();
            for (float[][] point : animationData.getPoints()) {
                if (point[1][0] == selected_point) { // selected
                    drawPoint(point[0], ig,
                            VitcoSettings.ANIMATION_DOT_SEL_INNER_COLOR,
                            VitcoSettings.ANIMATION_DOT_SEL_OUTER_COLOR,
                            VitcoSettings.ANIMATION_CIRCLE_RADIUS,
                            VitcoSettings.ANIMATION_CIRCLE_BORDER_SIZE);
                } else if (point[1][0] == highlighted_point) { // highlighted
                    drawPoint(point[0], ig,
                            VitcoSettings.ANIMATION_DOT_HL_INNER_COLOR,
                            VitcoSettings.ANIMATION_DOT_HL_OUTER_COLOR,
                            VitcoSettings.ANIMATION_CIRCLE_RADIUS,
                            VitcoSettings.ANIMATION_CIRCLE_BORDER_SIZE);
                } else { // default
                    drawPoint(point[0], ig,
                            VitcoSettings.ANIMATION_DOT_INNER_COLOR,
                            VitcoSettings.ANIMATION_DOT_OUTER_COLOR,
                            VitcoSettings.ANIMATION_CIRCLE_RADIUS,
                            VitcoSettings.ANIMATION_CIRCLE_BORDER_SIZE);
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
        private void drawLinkedOverlay(Graphics2D ig) {
            // Anti-alias
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // draw axis into corner (top left)
            Matrix matrix = camera.getDirection().getRotationMatrix().invert();
            SimpleVector vec1 = new SimpleVector(1,0,0);
            vec1.matMul(matrix);
            SimpleVector vec2 = new SimpleVector(0,1,0);
            vec2.matMul(matrix);
            SimpleVector vec3 = new SimpleVector(0,0,1);
            vec3.matMul(matrix);
            // sort according to z value
            if (vec1.z < 0) {
                vec1.scalarMul(-1);
            }
            if (vec2.z < 0) {
                vec2.scalarMul(-1);
            }
            if (vec3.z < 0) {
                vec3.scalarMul(-1);
            }
            // draw
            drawAxeHalf(vec1, false, ig, VitcoSettings.ANIMATION_AXIS_COLOR_X, VitcoSettings.ANIMATION_AXIS_OUTER_COLOR, VitcoSettings.ANIMATION_AXIS_LINE_SIZE);
            drawAxeHalf(vec2, false, ig, VitcoSettings.ANIMATION_AXIS_COLOR_Y, VitcoSettings.ANIMATION_AXIS_OUTER_COLOR, VitcoSettings.ANIMATION_AXIS_LINE_SIZE);
            drawAxeHalf(vec3, false, ig, VitcoSettings.ANIMATION_AXIS_COLOR_Z, VitcoSettings.ANIMATION_AXIS_OUTER_COLOR, VitcoSettings.ANIMATION_AXIS_LINE_SIZE);
            drawAxeHalf(vec1, true, ig, VitcoSettings.ANIMATION_AXIS_COLOR_X, VitcoSettings.ANIMATION_AXIS_OUTER_COLOR, VitcoSettings.ANIMATION_AXIS_LINE_SIZE);
            drawAxeHalf(vec2, true, ig, VitcoSettings.ANIMATION_AXIS_COLOR_Y, VitcoSettings.ANIMATION_AXIS_OUTER_COLOR, VitcoSettings.ANIMATION_AXIS_LINE_SIZE);
            drawAxeHalf(vec3, true, ig, VitcoSettings.ANIMATION_AXIS_COLOR_Z, VitcoSettings.ANIMATION_AXIS_OUTER_COLOR, VitcoSettings.ANIMATION_AXIS_LINE_SIZE);

            // draw center cross
            ig.setColor(VitcoSettings.ANIMATION_CENTER_CROSS_COLOR);
            ig.setStroke(new BasicStroke(1.0f));
            SimpleVector center = convert3D2D(new float[] {0, 0, 0});
            ig.drawLine(Math.round(center.x - 5), Math.round(center.y), Math.round(center.x + 5), Math.round(center.y));
            ig.drawLine(Math.round(center.x), Math.round(center.y - 5), Math.round(center.x), Math.round(center.y + 5));
        }


        // handle the redrawing of this component
        @Override
        protected final void paintComponent(Graphics g1) {
            if (skipNextWorldRender) {
                skipNextWorldRender = false;
            } else {
                buffer.clear(VitcoSettings.ANIMATION_BG_COLOR);
                if (drawWorld) {
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
        }
    }

    @PreDestroy
    public final void cleanup() {
        buffer.disableRenderer(IRenderer.RENDERER_OPENGL);
        buffer.dispose();
    }

    protected EngineViewPrototype() {
        // set up world objects
        world = new World();
        camera = new CCamera();
        world.setCameraTo(camera);

        // add a border to our view
        container.setBorder(BorderFactory.createLineBorder(VitcoSettings.DEFAULT_BORDER_COLOR));

        // register size change of container and change buffer size accordingly
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (container.getWidth() > 0 && container.getHeight() > 0) {
                    buffer.dispose();
                    buffer = new FrameBuffer(container.getWidth(), container.getHeight(),
                            FrameBuffer.SAMPLINGMODE_OGSS);
                    buffer.clear(VitcoSettings.ANIMATION_BG_COLOR); // init the buffer
                    container.repaint();
                }
            }
        });
    }
}
