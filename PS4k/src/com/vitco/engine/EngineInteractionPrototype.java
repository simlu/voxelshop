package com.vitco.engine;

import com.newbrightidea.util.RTree;
import com.threed.jpct.Interact2D;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.vitco.engine.data.container.ExtendedVector;
import com.vitco.engine.data.notification.DataChangeAdapter;
import com.vitco.res.VitcoSettings;
import com.vitco.util.action.ChangeListener;
import com.vitco.util.action.types.StateActionPrototype;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Defines general (common) interactions available for this engine view and sets them up.
 */
public abstract class EngineInteractionPrototype extends EngineViewPrototype {

    protected final AnimationMouseAdapter animationAdapter = new AnimationMouseAdapter();
    protected class AnimationMouseAdapter extends MouseAdapter {
        private int dragPoint = -1; // the point that is dragged
        private long wasDragged = -1; // -1 if not dragged or the time in ms of first drag event

        // rebuild 2d index to do hit test when mouse is moving
        private final RTree<Integer> points2D = new RTree<Integer>(50, 2, 2); //2D Rtree
        private boolean needToRebuild = true;
        private void rebuild2DIndex() {
            if (needToRebuild) {
                points2D.clear();
                for (ExtendedVector point : data.getPoints()) {
                    SimpleVector tmp = convert3D2D(point);
                    points2D.insert(new float[]{tmp.x, tmp.y}, new float[] {0,0}, point.id);
                }
                needToRebuild = false;
            }
        }

        // get the 3D point for the mouse event in the same distance as the refPoint (from camera)
        private SimpleVector getPoint(MouseEvent e, SimpleVector refPoint) {
            SimpleVector result;
            if (voxelSnap) {
                SimpleVector dir = Interact2D.reproject2D3DWS(camera, buffer, e.getX()*2, e.getY()*2).normalize();
                Object[] res = world.calcMinDistanceAndObject3D(camera.getPosition(), dir, 10000);
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
        public void setVoxelSnap(boolean b) {
            voxelSnap = b;
        }
        public boolean getVoxelSnap() {
            return voxelSnap;
        }

        // call this when the animation data points have changed (e.g the perspective changed or
        // the data changed)
        public void refresh2DIndex() {
            needToRebuild = true;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragPoint != -1) { // there is a point dragged
                if (buffer.getOutputWidth() > e.getX() && buffer.getOutputHeight() > e.getY() && e.getX() > 0 && e.getY() > 0) {
                    if (wasDragged == -1) { // remember that this point was dragged
                        wasDragged = System.currentTimeMillis();
                    } else {
                        data.undoA();
                    }
                    data.setPreviewLine(-1, -1); // reset the preview line
                    // move the point to the correct position
                    ExtendedVector tmp = data.getPoint(dragPoint);

                    SimpleVector point = getPoint(e, tmp);
                    data.movePoint(dragPoint, point);
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            rebuild2DIndex();
            final int highlighted_point = data.getHighlightedPoint();
            final int selected_point = data.getSelectedPoint();
            //SimpleVector realPoint = convert2D3D(e.getX(), e.getY());
            // find if there is a point nearby
            List<Integer> search = points2D.search(new float[]{e.getX() - VitcoSettings.ANIMATION_CIRCLE_RADIUS,e.getY() - VitcoSettings.ANIMATION_CIRCLE_RADIUS},
                    new float[]{VitcoSettings.ANIMATION_CIRCLE_RADIUS * 2, VitcoSettings.ANIMATION_CIRCLE_RADIUS * 2});
            float tmp = -1;
            if (search.size() > 0) {
                tmp = search.get(0);
            }

            data.highlightPoint((int)tmp); // highlight that point

            // set the preview line iff highlighted and selected point exist and are different
            if (selected_point != -1 && highlighted_point != selected_point && highlighted_point != -1) {
                data.setPreviewLine(selected_point, highlighted_point);
            } else {
                data.setPreviewLine(-1, -1);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            camera.setEnabled(true);
            needToRebuild = true;
            if (e.getButton() == 1) { // left mb
                if (dragPoint != -1 && wasDragged != -1) {
                    // do not save this move action if the point position did not change
                    ExtendedVector point = data.getPoint(dragPoint);
                    data.undoA();
                    ExtendedVector point2 = data.getPoint(dragPoint);
                    if (!point.equals(point2)) {
                        data.redoA();
                    }
                }
                dragPoint = -1; // stop dragging
                final int highlighted_point = data.getHighlightedPoint();
                final int selected_point = data.getSelectedPoint();
                if (highlighted_point != -1) { // there is a highlighted point
                    // if it was not at all or only for a short time dragged
                    if (wasDragged == -1 || (System.currentTimeMillis() - wasDragged < 75) ) {
                        if (selected_point == highlighted_point) { // click on selected point
                            data.selectPoint(-1); // deselect
                        } else {
                            if (selected_point == -1) { // click on new point
                                data.selectPoint(highlighted_point); // select
                            } else {
                                // click on different point -> connect/disconnect line
                                if (data.areConnected(selected_point, highlighted_point)) {
                                    data.disconnect(selected_point, highlighted_point);
                                    //animationData.selectPoint(-1); // unselect after disconnect
                                } else {
                                    data.connect(selected_point, highlighted_point);
                                    //animationData.selectPoint(highlighted_point); // select after connect
                                }
                                data.selectPoint(-1); // unselect
                                // reset "highlighting"
                                data.setPreviewLine(-1, -1);
                            }
                        }
                    } else {
                        data.selectPoint(-1);
                    }
                }
            }
            mouseMoved(e);
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            final int highlighted_point = data.getHighlightedPoint();
            final int selected_point = data.getSelectedPoint();
            if (highlighted_point != -1) {
                camera.setEnabled(false);
            }
            switch (e.getButton()) {
                case 3: if (highlighted_point != -1) {
                        // highlighted point -> ask to remove
                        JPopupMenu popup = new JPopupMenu();
                        JMenuItem remove = new JMenuItem(langSelector.getString("remove_point"));
                        remove.addActionListener(new ActionListener() {
                            private final int tmp_point = highlighted_point; // store point for later access
                            @Override
                            public void actionPerformed(ActionEvent evt) {
                                // add a point
                                data.removePoint(tmp_point);
                                data.highlightPoint(-1);
                            }
                        });
                        popup.add(remove);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    } else {
                        // right click on background -> deselect
                        data.selectPoint(-1);
                    }
                    break;
                case 1: // if left mouse
                    if (highlighted_point != -1) { // highlighted -> select point
                        wasDragged = -1;
                        dragPoint = highlighted_point;
                    } else if (e.getClickCount() == 2) {
                        // not highlighted and double-click -> add a point
                        // check if we hit something
                        SimpleVector point = getPoint(e, SimpleVector.ORIGIN);
                        int added = data.addPoint(point);
                        if (selected_point != -1) { // connect if possible
                            data.connect(added, selected_point);
                        }
                        data.selectPoint(added); // and select
                    }
                    break;
            }
        }
    }

    // constructor
    protected EngineInteractionPrototype() {
        super();
        // refresh the 2D index when the camera changes
        camera.addCameraChangeListener(new CameraChangeListener() {
            @Override
            public void onCameraChange() {
                animationAdapter.refresh2DIndex();
            }
        });
    }

    private static boolean initialized = false;
    private static boolean animationMode = true;
    @PostConstruct
    protected final void init() {

        // change what is drawn/what user can do when the mode changes
        actionManager.performWhenActionIsReady("toggle_animation_mode", new Runnable() {
            @Override
            public void run() {
                ((StateActionPrototype)actionManager.getAction("toggle_animation_mode")).addChangeListener(new ChangeListener() {
                    @Override
                    public void actionFired(boolean b) { // this is fired once on setup
                        if (b) {
                            container.addMouseMotionListener(animationAdapter);
                            container.addMouseListener(animationAdapter);
                        } else {
                            data.removeAnimationHighlights();
                            container.removeMouseMotionListener(animationAdapter);
                            container.removeMouseListener(animationAdapter);
                        }
                        container.setDrawAnimationOverlay(b);
                        container.repaint();
                    }
                });
            }
        });

        // only perform these actions once (even if the class is instantiated several times)
        if (!initialized) {
            initialized = true;

            // action to refresh the status of the history buttons
            final Runnable refreshHistoryButton = new Runnable() {
                @Override
                public void run() {
                    actionManager.performWhenActionIsReady("global_action_undo", new Runnable() {
                        @Override
                        public void run() {
                            ((StateActionPrototype)actionManager.getAction("global_action_undo")).refresh();
                        }
                    });
                    actionManager.performWhenActionIsReady("global_action_redo", new Runnable() {
                        @Override
                        public void run() {
                            ((StateActionPrototype)actionManager.getAction("global_action_redo")).refresh();
                        }
                    });
                }
            };

            // know the mode at all times
            actionManager.performWhenActionIsReady("toggle_animation_mode", new Runnable() {
                @Override
                public void run() {
                    ((StateActionPrototype) actionManager.getAction("toggle_animation_mode")).addChangeListener(new ChangeListener() {
                        @Override
                        public void actionFired(boolean b) {
                            animationMode = b;
                            refreshHistoryButton.run();
                        }
                    });
                }
            });

            // register action status change events
            data.addDataChangeListener(new DataChangeAdapter() {
                @Override
                public void onAnimationDataChanged() {
                    refreshHistoryButton.run();
                }

                @Override
                public void onVoxelDataChanged() {
                    refreshHistoryButton.run();
                }
            });

            // register global shortcuts for data interaction
            // register undo action
            actionManager.registerAction("global_action_undo", new StateActionPrototype() {
                @Override
                public void action(ActionEvent actionEvent) {
                    if (getStatus()) {
                        if (animationMode) {
                            data.removeAnimationHighlights();
                            data.undoA();
                        } else {
                            data.removeVoxelHighlights();
                            data.undoV();
                        }
                    }
                }

                @Override
                public boolean getStatus() {
                    if (animationMode) {
                        return data.canUndoA();
                    } else {
                        return data.canUndoV();
                    }
                }
            });

            // register redo action
            actionManager.registerAction("global_action_redo", new StateActionPrototype() {
                @Override
                public void action(ActionEvent actionEvent) {
                    if (getStatus()) {
                        if (animationMode) {
                            data.removeAnimationHighlights();
                            data.redoA();
                        } else {
                            data.removeVoxelHighlights();
                            data.redoV();
                        }
                    }
                }

                @Override
                public boolean getStatus() {
                    if (animationMode) {
                        return data.canRedoA();
                    } else {
                        return data.canRedoV();
                    }
                }
            });

            // register clear history action
            actionManager.registerAction("clear_history_action", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    data.clearHistory();
                }
            });
        }
    }
}
