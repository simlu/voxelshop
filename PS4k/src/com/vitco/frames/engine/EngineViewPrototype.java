package com.vitco.frames.engine;

import com.threed.jpct.*;
import com.vitco.frames.ViewPrototype;
import com.vitco.frames.engine.data.animationdata.AnimationDataInterface;
import com.vitco.res.VitcoSettings;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Rendering functionality of this World (data + overlay)
 *
 * Can switch each of them on/off.
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
    public SimpleVector convert2D3D(int x, int y, SimpleVector referencePoint) {
        SimpleVector result = Interact2D.reproject2D3DWS(camera, buffer, x*2, y*2).normalize();
        result.scalarMul(camera.getPosition().distance(referencePoint));
        result.add(camera.getPosition());
        return result;
    }

    // conversion
    public SimpleVector convert3D2D(float[] point) {
        SimpleVector result = Interact2D.project3D2D(camera, buffer,
                new SimpleVector(point[0], point[1], point[2]));
        result.scalarMul(0.5f);
        return result;
    }

    // the container that we draw on
    protected MPanel container = new MPanel();
    protected class MPanel extends JPanel {

        private boolean drawWorld = true;
        public void setDrawWorld(boolean b) {
            drawWorld = b;
        }

        private boolean drawOverlay = true;
        public void setDrawOverlay(boolean b) {
            drawOverlay = b;
        }

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
        private void drawOverlay(Graphics2D ig) {
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
        private void drawAxe(SimpleVector unitVector, Graphics2D ig, Color innerColor, Color outerColor, float size) {
            G2DUtil.drawLine(
                    new SimpleVector(Math.round(unitVector.x*15 + 25), Math.round(unitVector.y*15 + 25), 0),
                    new SimpleVector(Math.round(unitVector.x*3 + 25), Math.round(unitVector.y*3 + 25), 0),
                    ig,
                    innerColor,
                    outerColor,
                    size
            );
        }

        // called for content that only changes when the opengl content changes
        private void drawStatic(Graphics2D ig) {
            // Anti-alias
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // draw axies into corner
            Matrix matrix = camera.getDirection().getRotationMatrix().invert();
            SimpleVector vec1 = new SimpleVector(1,0,0);
            vec1.matMul(matrix);
            SimpleVector vec2 = new SimpleVector(0,1,0);
            vec2.matMul(matrix);
            SimpleVector vec3 = new SimpleVector(0,0,1);
            vec3.matMul(matrix);
            // sort values
            class Holder extends SimpleVector {
                public final Color col;
                public Holder(SimpleVector vector, Color col) {
                    super(vector);
                    this.col = col;
                }
            }
            ArrayList<Holder> list = new ArrayList<Holder>();
            list.add(new Holder(vec1, VitcoSettings.ANIMATION_AXIS_COLOR_X));
            list.add(new Holder(vec2, VitcoSettings.ANIMATION_AXIS_COLOR_Y));
            list.add(new Holder(vec3, VitcoSettings.ANIMATION_AXIS_COLOR_Z));
            vec1.scalarMul(-1);
            vec2.scalarMul(-1);
            vec3.scalarMul(-1);
            list.add(new Holder(vec1, VitcoSettings.ANIMATION_AXIS_COLOR_X));
            list.add(new Holder(vec2, VitcoSettings.ANIMATION_AXIS_COLOR_Y));
            list.add(new Holder(vec3, VitcoSettings.ANIMATION_AXIS_COLOR_Z));
            Collections.sort(list, new Comparator<SimpleVector>() {
                @Override
                public int compare(SimpleVector o1, SimpleVector o2) {
                    return (int)Math.signum(o2.z - o1.z);
                }
            });
            for (Holder vec : list) {
                drawAxe(vec, ig, vec.col, VitcoSettings.ANIMATION_AXIS_OUTER_COLOR, VitcoSettings.ANIMATION_AXIS_LINE_SIZE);
            }

            // draw center cross
            ig.setColor(VitcoSettings.ANIMATION_CENTER_CROSS_COLOR);
            ig.setStroke(new BasicStroke(1.0f));
            SimpleVector center = convert3D2D(new float[] {0, 0, 0});
            ig.drawLine(Math.round(center.x - 5), Math.round(center.y), Math.round(center.x + 5), Math.round(center.y));
            ig.drawLine(Math.round(center.x), Math.round(center.y - 5), Math.round(center.x), Math.round(center.y + 5));
        }


        // handle the redrawing of this component
        @Override
        protected void paintComponent(Graphics g1) {
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
                    drawStatic((Graphics2D)buffer.getGraphics()); // refreshes with OpenGL
                }
            }
            buffer.display(g1);
            if (drawOverlay) { // overlay part 2
                drawOverlay((Graphics2D) g1); // refreshes with animation data
            }
        }
    }

    @PreDestroy
    public void finish() {
        buffer.disableRenderer(IRenderer.RENDERER_OPENGL);
        buffer.dispose();
    }

    protected EngineViewPrototype() {
        // set up world objects
        world = new World();
        camera = new CCamera();
        world.setCameraTo(camera);

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
    }
}
