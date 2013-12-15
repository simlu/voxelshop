package com.vitco.core.modes.tools;

import com.threed.jpct.SimpleVector;
import com.vitco.core.container.DrawContainer;
import com.vitco.core.data.container.ExtendedVector;
import com.vitco.settings.VitcoSettings;

import java.awt.event.MouseEvent;

/**
 * The basic animation point tool.
 */
public class PointTool extends AbstractAnimationTool {

    // constructor
    public PointTool(DrawContainer container, int side) {
        super(container, side);
    }

    // the point that is dragged
    private int dragPoint = -1;
    // -1 if not dragged or the time in ms of first drag event
    private long wasDragged = -1;

    @Override
    protected void move(MouseEvent e) {
        // highlight point (if we hover one)
        int highlighted_point = container.hitTestAnimationPoint(e.getPoint());
        data.highlightPoint(highlighted_point);

        // set the preview line iff highlighted and selected point exist and are different
        int selected_point = data.getSelectedPoint();
        if (selected_point != -1 && highlighted_point != -1 && highlighted_point != selected_point) {
            data.setPreviewLine(selected_point, highlighted_point);
        } else {
            data.setPreviewLine(-1, -1);
        }
    }

    @Override
    protected void press(MouseEvent e) {
        // nullify drag event
        wasDragged = -1;
        dragPoint = -1;
        // right click
        if (isMouse3Down()) {
            final int highlighted_point = data.getHighlightedPoint();
            // point is highlighted
            if (highlighted_point != -1) {

//                // ask to remove
//                JPopupMenu popup = new JPopupMenu();
//                JMenuItem remove = new JMenuItem(langSelector.getString("remove_point"));
//                remove.addActionListener(new ActionListener() {
//                    private final int tmp_point = highlighted_point; // store point for later access
//
//                    @Override
//                    public void actionPerformed(ActionEvent evt) {
//                        asyncActionManager.addAsyncAction(new AsyncAction() {
//                            @Override
//                            public void performAction() {
//                                // remove point
//                                data.highlightPoint(-1);
//                                data.setPreviewLine(-1, -1);
//                                data.removePoint(tmp_point);
//                            }
//                        });
//                    }
//                });
//                popup.add(remove);
//                popup.show(e.getComponent(), e.getX(), e.getY());

                // remove point
                data.highlightPoint(-1);
                data.setPreviewLine(-1, -1);
                data.removePoint(highlighted_point);
            }
        } else
        // left click
        if (isMouse1Down()) {
            final int highlighted_point = data.getHighlightedPoint();
            // point is highlighted
            if (highlighted_point != -1) { // highlighted -> select point
                wasDragged = -1;
                dragPoint = highlighted_point;
            }
        }
    }

    @Override
    protected void release(MouseEvent e) {
        final int highlighted_point = data.getHighlightedPoint();
        // point was highlighted
        if (highlighted_point != -1) {
            // this was a drag event
            if (dragPoint != -1 && wasDragged != -1) {
                // do not save this move action if the point position did not change
                ExtendedVector point = data.getPoint(dragPoint);
                data.undoA();
                ExtendedVector point2 = data.getPoint(dragPoint);
                if (!point.equals(point2)) {
                    data.redoA();
                }
                // quick select for side view "current" planes
                SimpleVector position = data.getPoint(dragPoint);
                preferences.storeObject("currentplane_sideview1", Math.round(position.z/ VitcoSettings.VOXEL_SIZE));
                preferences.storeObject("currentplane_sideview2", Math.round(position.y/ VitcoSettings.VOXEL_SIZE));
                preferences.storeObject("currentplane_sideview3", Math.round(position.x/ VitcoSettings.VOXEL_SIZE));
            }

            // -----------------------

            // handle select, deselect, connect and disconnect logic
            final int selected_point = data.getSelectedPoint();
            // no or short drag event
            if (wasDragged == -1 || (System.currentTimeMillis() - wasDragged < 75) ) {
                // click on selected point
                if (selected_point == highlighted_point) {
                    // deselect point
                    data.selectPoint(-1);
                } else {
                    // no point selected
                    if (selected_point == -1) {
                        // select point
                        data.selectPoint(highlighted_point);
                    } else {
                        // click on different point -> connect/disconnect line
                        if (data.areConnected(selected_point, highlighted_point)) {
                            data.disconnect(selected_point, highlighted_point);
                        } else {
                            data.connect(selected_point, highlighted_point);
                        }
                        // unselect point
                        data.selectPoint(-1);
                        // reset "highlighting"
                        data.setPreviewLine(-1, -1);
                    }
                }
            } else {
                data.selectPoint(-1);
            }
        }
    }

    @Override
    protected void drag(MouseEvent e) {
        // there is a point that we're dragging
        if (dragPoint != -1) {
            // check that we are in the boundaries of the container
            if (container.getBounds().contains(e.getPoint())) {
                // check if we already know that we are dragging
                if (wasDragged == -1) {
                    // store the initial drag event time
                    wasDragged = System.currentTimeMillis();
                    // reset the preview line
                    data.setPreviewLine(-1, -1);
                } else {
                    // erase previous history step
                    data.undoA();
                }
                // move the point to the correct position
                SimpleVector point = container.get3DPoint(e, data.getPoint(dragPoint));
                data.movePoint(dragPoint, point);
            }
        }
    }

    @Override
    protected void click(MouseEvent e) {
        // single click
        if (e.getClickCount() == 1) {
            final int highlighted_point = data.getHighlightedPoint();
            // no point is highlighted
            if (highlighted_point == -1) {
                // right click
                if (isMouse3Down()) {
                    // deselect point
                    data.selectPoint(-1);
                }
            }
        }
        // double click (left mouse)
        if (e.getClickCount() == 2 && isMouse1Down()) {
            final int highlighted_point = data.getHighlightedPoint();
            // no point is highlighted
            if (highlighted_point == -1) {
                // add point
                SimpleVector point = container.get3DPoint(e, container.getRefPoint());
                int added = data.addPoint(point);
                // connect to selected point if possible
                final int selected_point = data.getSelectedPoint();
                if (selected_point != -1) {
                    data.connect(added, selected_point);
                }
                // select new point
                data.selectPoint(added);
            }
        }
    }

    @Override
    protected void singleClick(MouseEvent e) {}

    @Override
    protected void shiftMove(MouseEvent e) {}

    @Override
    protected void shiftPress(MouseEvent e) {}

    @Override
    protected void shiftRelease(MouseEvent e) {}

    @Override
    protected void shiftDrag(MouseEvent e) {}

    @Override
    protected void shiftClick(MouseEvent e) {}

    @Override
    protected void singleShiftClick(MouseEvent e) {}

    @Override
    protected void key() {}

}
