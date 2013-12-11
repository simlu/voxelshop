package com.vitco.engine.view;

import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Interact2D;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.vitco.async.AsyncActionManager;
import com.vitco.engine.CCamera;
import com.vitco.engine.CameraChangeListener;
import com.vitco.engine.data.Data;
import com.vitco.engine.data.container.ExtendedVector;
import com.vitco.engine.data.container.Voxel;
import com.vitco.engine.data.notification.DataChangeAdapter;
import com.vitco.engine.world.AbstractCWorld;
import com.vitco.res.VitcoSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Defines variables and some basic functionality that the draw container has.
 */
public abstract class AbstractDrawContainer extends JPanel {

    // the side this panel represents
    protected final int side;

    // constructor
    protected AbstractDrawContainer(int side) {
        this.side = side;
    }

    // var & setter
    protected AsyncActionManager asyncActionManager;
    public final void setAsyncActionManager(AsyncActionManager asyncActionManager) {
        this.asyncActionManager = asyncActionManager;
    }

    // var & setter
    protected Data data;
    public final void setData(final Data data) {
        this.data = data;

        // what to do when data changes
        DataChangeAdapter dca = new DataChangeAdapter() {

            @Override
            public void onAnimationDataChanged() {
                refresh2DIndex();
            }

            @Override
            public void onVoxelHighlightingChanged() {
                highlighted = data.getHighlightedVoxel();
                highlightedFloat = highlighted == null ? null : new float[] {
                        highlighted[0], highlighted[1], highlighted[2]
                };
            }

            @Override
            public void onOutlineBoxesChanged() {
                outlineBoxed = data.getOutlineBoxes();
            }

            @Override
            public void onSelectionRectChanged() {
                selectedRect = data.getSelectionRect();
            }

        };
        data.addDataChangeListener(dca);

    }

    // ################################
    // Define variables and some basic setter/getter interaction
    // ################################

    // highlighted voxel
    protected int[] highlighted = null;
    protected float[] highlightedFloat = null;
    // outlined boxes
    protected int[][][] outlineBoxed = new int[0][][];
    // selected rect
    protected Rectangle selectedRect = null;

    // activates this container
    // note: Only one container can be active at a time
    private static AbstractDrawContainer activeContainer = null;
    public static void setActiveContainer(AbstractDrawContainer activeContainer) {
        AbstractDrawContainer.activeContainer = activeContainer;
    }
    public final void activate() {
        setActiveContainer(this);
    }
    public final void deactivate() {
        setActiveContainer(this);
    }
    public final boolean isActive() {
        return activeContainer == this;
    }

    // if this is a side view, this indicates the current plane
    // this side view is in
    private int plane = 0;
    public int getPlane() {
        return plane;
    }
    public void setPlane(int plane) {
        this.plane = plane;
    }

    // bg color of this panel
    protected Color bgColor = VitcoSettings.ANIMATION_BG_COLOR;
    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    // the current preview plane (e.g. the current "center" or "slice")
    protected int previewPlane = 0;
    public void setPreviewPlane(int previewPlane) {
        this.previewPlane = previewPlane;
    }

    // true iff camera has changed since last draw call
    protected boolean cameraChanged = true;
    public void setCameraChanged(boolean cameraChanged) {
        this.cameraChanged = cameraChanged;
    }

    // true iff container has resized since last draw call
    protected boolean hasResized = true;

    // this draws jpct engine content if enabled
    protected boolean drawWorld = true;
    public final void setDrawWorld(boolean b) {
        drawWorld = b;
    }

    // this enables/disables drawing of selected voxels
    protected boolean drawSelectedVoxels = true;
    public final void setDrawSelectedVoxels(boolean b) {
        drawSelectedVoxels = b;
    }

    // this enabled/disables all overlay
    protected boolean drawOverlay = true;
    public final void setDrawOverlay(boolean b) {
        drawOverlay = b;
    }

    // enabled/ disables animation overlay
    protected boolean drawAnimationOverlay = true;
    public final void setDrawAnimationOverlay(boolean b) {
        drawAnimationOverlay = b;
    }

    // enabled/ disables voxel overlay
    protected boolean drawVoxelOverlay = true;
    public final void setDrawVoxelOverlay(boolean b) {
        drawVoxelOverlay = b;
    }

    // for the next refresh do not update the world (OpenGL render)
    protected boolean skipNextWorldRender = false;
    public final void skipNextWorldRender() {
        skipNextWorldRender = true;
    }
    public boolean isSkipNextWorldRender() {
        return skipNextWorldRender;
    }

    // prevent skipping of next world view
    protected boolean doNotSkipNextWorldRender = false;
    public final void doNotSkipNextWorldRender() {
        doNotSkipNextWorldRender = true;
    }
    public boolean isDoNotSkipNextWorldRender() {
        return doNotSkipNextWorldRender;
    }

    // reset the redraw flags
    public final void resetSkipRenderFlags() {
        skipNextWorldRender = false;
        doNotSkipNextWorldRender = false;
    }

    // enable/disable wireframe mode
    protected boolean useWireFrame = false;
    public final void useWireFrame(boolean b) {
        useWireFrame = b;
    }

    // draw some ghosting lines (the voxel outline)
    protected boolean drawGhostOverlay = false;
    public final void setDrawGhostOverlay(boolean b) {
        drawGhostOverlay = b;
    }

    // true if this container is currently pending a repaint
    protected boolean repainting = false;
    public boolean isRepainting() {
        return repainting;
    }
    public void setRepainting(boolean repainting) {
        this.repainting = repainting;
    }

    // true if this panel needs repainting
    protected boolean needRepainting = false;
    public void setNeedRepainting(boolean needRepainting) {
        this.needRepainting = needRepainting;
    }

    // used to trigger additional repaint (queued)
    protected boolean skipNextWorldRenderBuffer;
    protected boolean doNotSkipNextWorldRenderBuffer;
    public void bufferWorldRenderFlags() {
        skipNextWorldRenderBuffer = skipNextWorldRender;
        doNotSkipNextWorldRenderBuffer = doNotSkipNextWorldRender;
    }

    // container for the data
    protected FrameBuffer buffer = new FrameBuffer(100, 100, VitcoSettings.SAMPLING_MODE);

    // the world-required objects
    protected AbstractCWorld world;
    public final void setWorld(AbstractCWorld world) {
        this.world = world;
    }
    protected AbstractCWorld selectedVoxelsWorld;
    public final void setSelectedVoxelsWorld(AbstractCWorld selectedVoxelsWorld) {
        this.selectedVoxelsWorld = selectedVoxelsWorld;
    }
    protected CCamera camera;
    public final void setCamera(CCamera camera) {
        if (this.camera == null) {
            this.camera = camera;
            // refresh the 2D index when the camera changes
            camera.addCameraChangeListener(new CameraChangeListener() {
                @Override
                public void onCameraChange() {
                    refresh2DIndex();
                }
            });
        }
    }

    // if true, we draw the bounding box that wraps the drawing space
    protected boolean drawBoundingBox = false;
    public final void setDrawBoundingBox(boolean value) {
        drawBoundingBox = value;
    }

    // some buffers
    protected BufferedImage toDraw;
    protected BufferedImage overlayBuffer;
    protected Graphics2D overlayBufferGraphics;

    // ################################
    // some static variables
    // ################################

    // how many voxels is the ground plane wide
    protected final static float size = VitcoSettings.VOXEL_GROUND_PLANE_SIZE / VitcoSettings.VOXEL_SIZE;

    // list of all vectors
    private final static SimpleVector[] vectorsStatic = new SimpleVector[] {
            new SimpleVector( + 0.5, -0.5 + 0.5/size + 0.5,  + 0.5),
            new SimpleVector( + 0.5, -0.5 + 0.5/size + 0.5,  - 0.5),
            new SimpleVector( + 0.5, -0.5 + 0.5/size - 0.5,  - 0.5),
            new SimpleVector( + 0.5, -0.5 + 0.5/size - 0.5,  + 0.5),
            new SimpleVector( - 0.5, -0.5 + 0.5/size + 0.5,  + 0.5),
            new SimpleVector( - 0.5, -0.5 + 0.5/size + 0.5,  - 0.5),
            new SimpleVector( - 0.5, -0.5 + 0.5/size - 0.5,  - 0.5),
            new SimpleVector( - 0.5, -0.5 + 0.5/size - 0.5,  + 0.5)
    };
    public static SimpleVector getVectorsStatic(int i) {
        return new SimpleVector(vectorsStatic[i]);
    }

    protected final float[] defaultVoxel = new float[] {0.5f, 0.5f, 0.5f};

    // ################################
    // Define some basic functionality
    // ################################

    // conversion
    public final SimpleVector convert2D3D(int x, int y, SimpleVector referencePoint) {
        SimpleVector result = Interact2D.reproject2D3DWS(camera, buffer,
                x * VitcoSettings.SAMPLING_MODE_MULTIPLICAND,
                y * VitcoSettings.SAMPLING_MODE_MULTIPLICAND).normalize();
        result.scalarMul(camera.getPosition().distance(referencePoint));
        result.add(camera.getPosition());
        return result;
    }

    // conversion
    public final SimpleVector convert3D2D(SimpleVector point) {
        SimpleVector result = Interact2D.project3D2D(camera, buffer, point);
        if (result != null) {
            result.scalarMul(VitcoSettings.SAMPLING_MODE_DIVIDEND);
        }
        return result;
    }

    // conversion
    public final ExtendedVector convertExt3D2D(ExtendedVector point) {
        ExtendedVector result = null;
        SimpleVector point2d = Interact2D.project3D2D(camera, buffer, point);
        if (point2d != null) {
            point2d.scalarMul(VitcoSettings.SAMPLING_MODE_DIVIDEND);
            result = new ExtendedVector(point2d, point.id);
        }
        return result;
    }

    // get direction for a 2D point
    public final SimpleVector getDirection(int x, int y) {
        return Interact2D.reproject2D3DWS(camera, buffer,
                x * VitcoSettings.SAMPLING_MODE_MULTIPLICAND,
                y * VitcoSettings.SAMPLING_MODE_MULTIPLICAND).normalize();
    }

    // -----------------------------

//    // holds all the direction vectors (right, left, lower, upper, back, front)
//    private final static SimpleVector[] directionVectors = new SimpleVector[] {
//            new SimpleVector(1,0,0),
//            new SimpleVector(-1,0,0),
//            new SimpleVector(0,1,0),
//            new SimpleVector(0,-1,0),
//            new SimpleVector(0,0,1),
//            new SimpleVector(0,0,-1)
//    };


    // gets last active side
    private int lastActiveSide = 0;
    public final int getLastActiveSide() {
        return lastActiveSide;
    }

    // holds the voxel side that was last hit by a hover event
    // get active voxel for mouse hover
    public final int[] voxelForHover3D(Point point, boolean selectNeighbour, boolean useFloor) {
        int[] voxelPos = null;
        // check if we hit something
        SimpleVector dir = this.getDirection(point.x, point.y);
        Object[] res = world.calcMinDistanceAndObject3D(camera.getPosition(), dir, 10000);
        if (res[1] != null) { // something hit
            Object3D obj3D = ((Object3D)res[1]);

            // find collision point
            SimpleVector colPoint = camera.getPosition();
            dir.scalarMul((Float)res[0]);
            colPoint.add(dir);
            //colPoint.sub(obj3D.getOrigin());

//            // find side that it hits
//            lastActiveSide = 0; // assume it's zero (no need to check)
//            float dist = colPoint.distance(directionVectors[0]);
//            for (int i = 1; i < directionVectors.length; i++) {
//                float tempDist = colPoint.distance(directionVectors[i]);
//                if (dist > tempDist) {
//                    dist = tempDist;
//                    lastActiveSide = i;
//                }
//            }

            voxelPos = world.getVoxelPos(obj3D.getID(), colPoint.x, colPoint.y, colPoint.z);
            if (voxelPos != null) {
                Voxel hitVoxel = data.searchVoxel(voxelPos, false);
                if (hitVoxel != null) {
                    // find side that it hits
                    lastActiveSide = world.getSide(obj3D.getID());
                    if (selectNeighbour) {
                        switch (lastActiveSide) {
                            case 0: voxelPos[0] += 1; break;
                            case 1: voxelPos[0] -= 1; break;
                            case 2: voxelPos[1] += 1; break;
                            case 3: voxelPos[1] -= 1; break;
                            case 4: voxelPos[2] += 1; break;
                            case 5: voxelPos[2] -= 1; break;
                            default: break;
                        }
                    }
                }
            }
        } else if (useFloor) {

            float[] dirArr = dir.toArray();
            for (int[] tuple : new int[][] {new int[]{0,10,-10}, new int[]{1,0,-20}, new int[]{2,10,-10}}) {
                int[] pos = this.voxelForHover3D(
                        new SimpleVector(dir), dirArr[tuple[0]] > 0 ? tuple[1] : tuple[2], tuple[0]
                );
                if (pos != null) {
                        if (Math.abs(pos[0]) < VitcoSettings.VOXEL_GROUND_MAX_RANGE &&
                                Math.abs(pos[2]) < VitcoSettings.VOXEL_GROUND_MAX_RANGE &&
                                pos[1] <= 0 && pos[1] > -VitcoSettings.VOXEL_GROUND_MAX_RANGE * 2) {
                            voxelPos = pos;
                            lastActiveSide = tuple[0] * 2;
                        }
                    }
            }

        }
        return voxelPos;
    }

    // get voxel position for point, side and plane
    public final int[] voxelForHover3D(SimpleVector dir, int plane, int side) {
        float[] dirArr = dir.toArray();
        if (Math.abs(dirArr[side]) > 0.05) { // angle big enough
            float[] camArr = camera.getPosition().toArray();
            // calculate position
            float t = ((plane + (dirArr[side] > 0 ? +0.5f : -0.5f)) * VitcoSettings.VOXEL_SIZE - camArr[side]) / dirArr[side];
            dir.scalarMul(t);
            SimpleVector pos = camera.getPosition();
            pos.add(dir);
            pos.scalarMul(1/VitcoSettings.VOXEL_SIZE);
            float[] posArray = pos.toArray();
            posArray[side] += (dirArr[side] > 0 ? -0.5f : 0.5f);
            return new int[]{Math.round(posArray[0]),Math.round(posArray[1]),Math.round(posArray[2])};
        }
        return null;
    }

    // get voxel position for hover (fast in 2D)
    public final int[] voxelForHover2D(Point point) {
        // calculate position
        SimpleVector nPos = this.convert2D3D((int) Math.round(point.getX()), (int) Math.round(point.getY()),
                new SimpleVector(
                        side == 2 ? plane : 0,
                        side == 1 ? plane : 0,
                        side == 0 ? plane : 0
                )
        );
        return new int[]{
                side == 2 ? plane : Math.round(nPos.x/VitcoSettings.VOXEL_SIZE),
                side == 1 ? plane : Math.round(nPos.y/VitcoSettings.VOXEL_SIZE),
                side == 0 ? plane : Math.round(nPos.z/VitcoSettings.VOXEL_SIZE)
        };
    }

    // get reference point (original or side)
    public final SimpleVector getRefPoint() {
        return new SimpleVector(
                side == 2 ? plane*VitcoSettings.VOXEL_SIZE : 0,
                side == 1 ? plane*VitcoSettings.VOXEL_SIZE : 0,
                side == 0 ? plane*VitcoSettings.VOXEL_SIZE : 0
        );
    }

    // enable/disable camera
    public final void enableCamera(boolean b) {
        camera.setEnabled(b);
    }

    // do hit detection with shifted selection
    public final SimpleVector shiftedCollisionPoint(Point p) {
        return selectedVoxelsWorld.shiftedCollisionPoint(
                this.getDirection(p.x, p.y)
        );
    }

    // ##############################################################
    // #### Animation Tool functions ################################
    // ##############################################################

    // hit test
    public final int hitTestAnimationPoint(Point center) {
        rebuild2DIndex(); // only recomputes if necessary

        // find if there is a point nearby
        java.util.List<ExtendedVector> search = new ArrayList<ExtendedVector>();
        for (ExtendedVector point : points2D) {
            if (center.distance(point.x, point.y) <= VitcoSettings.ANIMATION_CIRCLE_RADIUS) {
                search.add(point);
            }
        }

        int tmp = -1;
        if (search.size() > 0) {
            // get the circle on top
            Collections.sort(search, new Comparator<ExtendedVector>() {
                @Override
                public int compare(ExtendedVector o1, ExtendedVector o2) {
                    return (int)Math.signum(o2.z - o1.z);
                }
            });
            // remember id
            tmp = search.get(0).id;
        }

        return tmp;
    }

    // get the 3D point for the mouse event in the same distance as the refPoint (from camera)
    public final SimpleVector get3DPoint(MouseEvent e, SimpleVector refPoint) {
        SimpleVector result;
        if (voxelSnap) {
            SimpleVector dir = getDirection(e.getX(), e.getY());
            Object[] res = world.calcMinDistanceAndObject3D(camera.getPosition(), dir, 100000);
            if (res[1] != null) {
                Object3D obj3D = ((Object3D)res[1]);
                result = obj3D.getOrigin();
            } else {
                result = convert2D3D(e.getX(), e.getY(), refPoint);
            }
        } else {
            result = convert2D3D(e.getX(), e.getY(), refPoint);
        }
        return result;
    }

    // set the snap functionality for animations dots (they "snap" to voxels)
    private boolean voxelSnap = false;
    public final void setVoxelSnap(boolean b) {
        voxelSnap = b;
    }
    public final boolean getVoxelSnap() {
        return voxelSnap;
    }

    // rebuild 2d index to do hit test when mouse is moving
    private final ArrayList<ExtendedVector> points2D = new ArrayList<ExtendedVector>();
    private boolean needToRebuild = true;
    private void rebuild2DIndex() {
        if (needToRebuild) {
            points2D.clear();
            for (ExtendedVector point : data.getPoints()) {
                ExtendedVector tmp = convertExt3D2D(point);
                if (tmp != null) {
                    points2D.add(tmp);
                }
            }
            needToRebuild = false;
        }
    }

    // call this when the animation data points have changed (e.g the perspective changed or
    // the data changed)
    public final void refresh2DIndex() {
        needToRebuild = true;
    }

}
