package com.vitco.frames.engine.data;

import com.threed.jpct.SimpleVector;
import com.vitco.frames.engine.data.container.ExtendedLine;
import com.vitco.frames.engine.data.container.ExtendedVector;
import com.vitco.frames.engine.data.container.Frame;
import com.vitco.frames.engine.data.history.BasicActionIntent;
import com.vitco.frames.engine.data.history.HistoryChangeListener;
import com.vitco.frames.engine.data.history.HistoryManager;
import com.vitco.frames.engine.data.notification.DataChangeListener;
import com.vitco.util.action.ChangeListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implements all functions defined in the AnimationDataInterface
 */
public abstract class AnimationData extends ListenerData implements AnimationDataInterface {

    // constructor
    protected AnimationData() {
        super();
        // notify when the data changes
        historyManagerA.addChangeListener(new HistoryChangeListener() {
            @Override
            public final void onChange() {
                lineBufferValid = false;
                pointBufferValid = false;
                frameBufferValid = false;
                notifier.onAnimationDataChanged();
            }
        });
    }

    // ####################### DATA
    // history manager
    protected final HistoryManager historyManagerA = new HistoryManager();
    // holds the points
    protected final HashMap<Integer, ExtendedVector> points = new HashMap<Integer, ExtendedVector>();
    // holds the lines
    protected final HashMap<String, ExtendedLine> lines = new HashMap<String, ExtendedLine>();
    // maps the points to lines
    protected final HashMap<Integer, ArrayList<ExtendedLine>> pointsToLines = new HashMap<Integer, ArrayList<ExtendedLine>>();
    // currently active frame
    protected int activeFrame = -1;
    // all frames
    protected final HashMap<Integer, Frame> frames = new HashMap<Integer, Frame>();

    // ###################### PRIVATE HELPER CLASSES
    // "add point" intent
    private class AddPointIntent extends BasicActionIntent {
        private final ExtendedVector point;

        // constructor
        public AddPointIntent(ExtendedVector point, boolean attach) {
            super(attach);
            this.point = point;
        }

        @Override
        protected void applyAction() {
            points.put(point.id, point);
        }

        @Override
        protected void unapplyAction() {
            points.remove(point.id);
            pointsToLines.remove(point.id);
        }
    }

    // "remove point" intent
    private class RemovePointIntent extends BasicActionIntent {
        private final Integer pointId;
        private ExtendedVector point;

        // constructor
        public RemovePointIntent(Integer pointId, boolean attach) {
            super(attach);
            this.pointId = pointId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                // dc all lines that include this point
                if (pointsToLines.containsKey(pointId)) {
                    ExtendedLine[] lines = new ExtendedLine[pointsToLines.get(pointId).size()];
                    pointsToLines.get(pointId).toArray(lines);
                    for (ExtendedLine line : lines) {
                        historyManagerA.applyIntent(new DisconnectIntent(line.point1, line.point2, true));
                    }
                }
                // delete this points in all frames
                for (Integer frameId : frames.keySet()) {
                    if (frames.get(frameId).getPoint(pointId) != null) {
                        historyManagerA.applyIntent(new RemoveFramePointIntent(pointId, frameId, true));
                    }
                }
                // store point
                point = points.get(pointId);
            }
            pointsToLines.remove(pointId);
            points.remove(pointId);
        }

        @Override
        protected void unapplyAction() {
            points.put(point.id, point);
        }
    }

    // "move point" intent
    private class MovePointIntent extends BasicActionIntent {
        private final Integer pointId;
        private final ExtendedVector point;
        private ExtendedVector previousPoint;

        // constructor
        public MovePointIntent(Integer pointId, SimpleVector pos, boolean attach) {
            super(attach);
            this.pointId = pointId;
            this.point = new ExtendedVector(pos, pointId);
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                previousPoint = points.get(pointId);
            }
            points.put(pointId, point);
        }

        @Override
        protected void unapplyAction() {
            if (previousPoint != null) {
                points.put(pointId, previousPoint);
            } else {
                points.remove(pointId);
            }
        }
    }

    // "connect two points with a line" intent
    private class ConnectIntent extends BasicActionIntent {
        private final Integer pointId1;
        private final Integer pointId2;

        // constructor
        public ConnectIntent(Integer pointId1, Integer pointId2, boolean attach) {
            super(attach);
            this.pointId1 = Math.min(pointId1, pointId2);
            this.pointId2 = Math.max(pointId1, pointId2);
        }

        @Override
        protected void applyAction() {
            ExtendedLine line = new ExtendedLine(pointId1, pointId2);
            lines.put(pointId1 + "_" + pointId2, line);
            // make sure the pointToLines is initialized
            if (!pointsToLines.containsKey(pointId1)) {
                pointsToLines.put(pointId1, new ArrayList<ExtendedLine>());
            }
            if (!pointsToLines.containsKey(pointId2)) {
                pointsToLines.put(pointId2, new ArrayList<ExtendedLine>());
            }
            pointsToLines.get(pointId1).add(line);
            pointsToLines.get(pointId2).add(line);
        }

        @Override
        protected void unapplyAction() {
            ExtendedLine line = lines.get(pointId1 + "_" + pointId2);
            lines.remove(pointId1 + "_" + pointId2);
            // make sure the pointToLines is initialized
            if (!pointsToLines.containsKey(pointId1)) {
                pointsToLines.put(pointId1, new ArrayList<ExtendedLine>());
            }
            if (!pointsToLines.containsKey(pointId2)) {
                pointsToLines.put(pointId2, new ArrayList<ExtendedLine>());
            }
            pointsToLines.get(pointId1).remove(line);
            pointsToLines.get(pointId2).remove(line);
        }
    }

    // "disconnect two points" intent
    private class DisconnectIntent extends BasicActionIntent {
        private final Integer pointId1;
        private final Integer pointId2;

        // constructor
        public DisconnectIntent(Integer pointId1, Integer pointId2, boolean attach) {
            super(attach);
            this.pointId1 = Math.min(pointId1, pointId2);
            this.pointId2 = Math.max(pointId1, pointId2);
        }

        @Override
        protected void applyAction() {
            ExtendedLine line = lines.get(pointId1 + "_" + pointId2);
            lines.remove(pointId1 + "_" + pointId2);
            // make sure the pointToLines is initialized
            if (!pointsToLines.containsKey(pointId1)) {
                pointsToLines.put(pointId1, new ArrayList<ExtendedLine>());
            }
            if (!pointsToLines.containsKey(pointId2)) {
                pointsToLines.put(pointId2, new ArrayList<ExtendedLine>());
            }
            pointsToLines.get(pointId1).remove(line);
            pointsToLines.get(pointId2).remove(line);
        }

        @Override
        protected void unapplyAction() {
            ExtendedLine line = new ExtendedLine(pointId1, pointId2);
            lines.put(pointId1 + "_" + pointId2, line);
            // make sure the pointToLines is initialized
            if (!pointsToLines.containsKey(pointId1)) {
                pointsToLines.put(pointId1, new ArrayList<ExtendedLine>());
            }
            if (!pointsToLines.containsKey(pointId2)) {
                pointsToLines.put(pointId2, new ArrayList<ExtendedLine>());
            }
            pointsToLines.get(pointId1).add(line);
            pointsToLines.get(pointId2).add(line);
        }
    }

    // "clear everything (animation data)" intent
    private class ClearAIntent extends BasicActionIntent {
        // constructor
        public ClearAIntent(boolean attach) {
            super(attach);
        }

        @Override
        protected void applyAction() {
            // remove all points (attach)
            if (isFirstCall()) { // delete all points of this frame
                Integer[] pointIds = new Integer[points.size()];
                points.keySet().toArray(pointIds);
                for (Integer pointId : pointIds) {
                    historyManagerA.applyIntent(new RemovePointIntent(pointId, true));
                }
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }
    }

    // "create frame" intent
    private class CreateFrameIntent extends BasicActionIntent {
        private final Integer frameId;
        private final String frameName;

        // constructor
        public CreateFrameIntent(Integer frameId, String frameName, boolean attach) {
            super(attach);
            this.frameId = frameId;
            this.frameName = frameName;
        }

        @Override
        protected void applyAction() {
            frames.put(frameId, new Frame(frameName));
        }

        @Override
        protected void unapplyAction() {
            frames.remove(frameId);
        }
    }

    // "delete frame" intent
    private class DeleteFrameIntent extends BasicActionIntent {
        private final Integer frameId;
        private String frameName;

        // constructor
        public DeleteFrameIntent(Integer frameId, boolean attach) {
            super(attach);
            this.frameId = frameId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                // delete all points of this frame
                Frame frameRef = frames.get(frameId);
                for (Integer pointId : frameRef.getPoints()) {
                    historyManagerA.applyIntent(new RemoveFramePointIntent(pointId, frameId, true));
                }
                if (activeFrame == frameId) { // make sure the frameId is still valid
                    historyManagerA.applyIntent(new SetActiveFrameIntent(-1, true));
                }
                // get the frame name
                frameName = frameRef.getName();
            }
            frames.remove(frameId);
        }

        @Override
        protected void unapplyAction() {
            frames.put(frameId, new Frame(frameName));
        }
    }

    // "reset frame" intent
    private class ResetFrameIntent extends BasicActionIntent {
        private final Integer frameId;

        // constructor
        public ResetFrameIntent(Integer frameId, boolean attach) {
            super(attach);
            this.frameId = frameId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                Frame frameRef = frames.get(frameId);
                for (Integer pointId : frameRef.getPoints()) {
                    historyManagerA.applyIntent(new RemoveFramePointIntent(pointId, frameId, true));
                }
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do here
        }
    }

    // "rename frame" intent
    private class RenameFrameIntent extends BasicActionIntent {
        private final Integer frameId;
        private final String frameName;
        private String oldFrameName;

        // constructor
        public RenameFrameIntent(Integer frameId, String frameName, boolean attach) {
            super(attach);
            this.frameId = frameId;
            this.frameName = frameName;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                oldFrameName = frames.get(frameId).getName();
            }
            frames.get(frameId).setName(frameName);
        }

        @Override
        protected void unapplyAction() {
            frames.get(frameId).setName(oldFrameName);
        }
    }

    // "place frame point" intent
    private class PlaceFramePointIntent extends BasicActionIntent {
        private final ExtendedVector point;
        private final Integer frameId;
        private ExtendedVector oldPoint;

        // constructor
        public PlaceFramePointIntent(Integer pointId, SimpleVector pos, Integer frameId, boolean attach) {
            super(attach);
            this.point = new ExtendedVector(pos, pointId);
            this.frameId = frameId;
        }

        @Override
        protected void applyAction() {
            Frame frameRef = frames.get(frameId);
            if (isFirstCall()) {
                oldPoint = frameRef.getPoint(point.id);
            }
            frameRef.setPoint(point.id, point);
        }

        @Override
        protected void unapplyAction() {
            if (oldPoint != null) {
                frames.get(frameId).setPoint(oldPoint.id, oldPoint);
            } else {
                frames.get(frameId).removePoint(point.id);
            }
        }
    }

    // "remove frame point" intent
    private class RemoveFramePointIntent extends BasicActionIntent {
        private final Integer pointId;
        private final Integer frameId;
        private ExtendedVector oldPoint;

        // constructor
        public RemoveFramePointIntent(Integer pointId, Integer frameId, boolean attach) {
            super(attach);
            this.pointId = pointId;
            this.frameId = frameId;
        }

        @Override
        protected void applyAction() {
            Frame frameRef = frames.get(frameId);
            if (isFirstCall()) {
                oldPoint = frameRef.getPoint(pointId);
            }
            frameRef.removePoint(pointId);
        }

        @Override
        protected void unapplyAction() {
            frames.get(frameId).setPoint(oldPoint.id, oldPoint);
        }
    }

    // "set active frame" intent
    private class SetActiveFrameIntent extends BasicActionIntent {
        private final Integer frameId;
        private Integer oldFrameId;

        // constructor
        public SetActiveFrameIntent(Integer frameId, boolean attach) {
            super(attach);
            this.frameId = frameId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                oldFrameId = activeFrame;
            }
            activeFrame = frameId;
        }

        @Override
        protected void unapplyAction() {
            activeFrame = oldFrameId;
        }
    }


    // ###################### PRIVATE HELPER FUNCTIONS
    // returns a free point id
    private int lastPoint = -1;
    private int getFreePointId() {
        do {
            lastPoint++;
        } while (points.containsKey(lastPoint));
        return lastPoint;
    }

    // returns a free frame id
    private int lastFrame = -1;
    private int getFreeFrameId() {
        do {
            lastFrame++;
        } while (frames.containsKey(lastFrame));
        return lastFrame;
    }



    // =========================
    // === interface methods ===
    // =========================

    @Override
    public final boolean isValid(int pointId) {
        return points.containsKey(pointId);
    }

    @Override
    public final int addPoint(SimpleVector position) {
        int pointId = getFreePointId();
        ExtendedVector point = new ExtendedVector(position.x, position.y, position.z, pointId);
        historyManagerA.applyIntent(new AddPointIntent(point, false));
        return pointId;
    }

    @Override
    public final boolean removePoint(int pointId) {
        boolean result = false;
        if (isValid(pointId)) {
            historyManagerA.applyIntent(new RemovePointIntent(pointId, false));
            result = true;
        }
        return result;
    }

    @Override
    public final boolean movePoint(int pointId, SimpleVector pos) {
        boolean result = false;
        if (isValid(pointId)) {
            if (activeFrame == -1) { // move real point
                historyManagerA.applyIntent(new MovePointIntent(pointId, pos, false));
            } else { // move frame point
                historyManagerA.applyIntent(new PlaceFramePointIntent(pointId, pos, activeFrame, false));
            }
            result = true;
        }
        return result;
    }

    @Override
    public final boolean areConnected(int id1, int id2) {
        return lines.containsKey(Math.min(id1, id2) + "_" + Math.max(id1, id2));
    }

    @Override
    public final boolean connect(int id1, int id2) {
        boolean result = false;
        if (isValid(id1) && isValid(id2) && !areConnected(id1, id2)) {
            historyManagerA.applyIntent(new ConnectIntent(id1, id2, false));
            result = true;
        }
        return result;
    }

    @Override
    public final boolean clearA() {
        boolean result = false;
        if (points.size() > 0) {
            historyManagerA.applyIntent(new ClearAIntent(false));
            result = true;
        }
        return result;
    }

    @Override
    public final boolean disconnect(int id1, int id2) {
        boolean result = false;
        if (isValid(id1) && isValid(id2) && areConnected(id1, id2)) {
            historyManagerA.applyIntent(new DisconnectIntent(id1, id2, false));
            result = true;
        }
        return result;
    }

    @Override
    public final ExtendedVector getPoint(int pointId) {
        if (activeFrame != -1) { // return frame point if defined
            ExtendedVector point = frames.get(activeFrame).getPoint(pointId);
            if (point != null) {
                return point;
            }
        }
        return points.get(pointId);
    }

    private ExtendedVector[] pointBuffer = new ExtendedVector[]{};
    private boolean pointBufferValid = false;
    @Override
    public final ExtendedVector[] getPoints() {
        if (!pointBufferValid) {
            if (pointBuffer.length != points.size()) {
                pointBuffer = new ExtendedVector[points.size()];
            }
            int i = 0;
            for (int pointId : points.keySet()) {
                pointBuffer[i++] = getPoint(pointId);
            }
            pointBufferValid = true;
        }
        return pointBuffer.clone();
    }

    private ExtendedVector[][] lineBuffer = new ExtendedVector[][]{};
    private boolean lineBufferValid = false;
    @Override
    public final ExtendedVector[][] getLines() {
        if (!lineBufferValid) {
            if (lineBuffer.length != lines.size()) {
                lineBuffer = new ExtendedVector[lines.size()][2];
            }
            int i = 0;
            for (ExtendedLine line : lines.values()) {
                lineBuffer[i][0] = getPoint(line.point1);
                lineBuffer[i][1] = getPoint(line.point2);
                i++;
            }
            lineBufferValid = true;
        }
        return lineBuffer.clone();
    }

    @Override
    public final void undoA() {
        historyManagerA.unapply();
    }

    @Override
    public final void redoA() {
        historyManagerA.apply();
    }

    @Override
    public final boolean canUndoA() {
        return historyManagerA.canUndo();
    }

    @Override
    public final boolean canRedoA() {
        return historyManagerA.canRedo();
    }

    @Override
    public final boolean selectFrame(int frameId) {
        boolean result = false;
        if (frames.containsKey(frameId) || frameId == -1) {
            historyManagerA.applyIntent(new SetActiveFrameIntent(frameId, false));
            result = true;
        }
        return result;
    }

    @Override
    public final int getSelectedFrame() {
        return activeFrame;
    }

    @Override
    public final int createFrame(String frameName) {
        int frameId = getFreeFrameId();
        historyManagerA.applyIntent(new CreateFrameIntent(frameId, frameName, false));
        return frameId;
    }

    @Override
    public final boolean deleteFrame(int frameId) {
        boolean result = false;
        if (frames.containsKey(frameId)) {
            historyManagerA.applyIntent(new DeleteFrameIntent(frameId, false));
            result = true;
        }
        return result;
    }

    @Override
    public final boolean renameFrame(int frameId, String newName) {
        boolean result = false;
        if (frames.containsKey(frameId)) {
            historyManagerA.applyIntent(new RenameFrameIntent(frameId, newName, false));
            result = true;
        }
        return result;
    }

    private Integer[] frameBuffer = new Integer[]{};
    private boolean frameBufferValid = false;
    @Override
    public final Integer[] getFrames() {
        if (!frameBufferValid) {
            if (frameBuffer.length != frames.size()) {
                frameBuffer = new Integer[frames.size()];
            }
            frames.keySet().toArray(frameBuffer);
            frameBufferValid = true;
        }
        return frameBuffer.clone();
    }

    @Override
    public final boolean resetFrame(int frameId) {
        boolean result = false;
        if (frames.containsKey(frameId)) {
            historyManagerA.applyIntent(new ResetFrameIntent(frameId, false));
            result = true;
        }
        return result;
    }

    @Override
    public final String getFrameName(int frameId) {
        if (frames.containsKey(frameId)) {
            return frames.get(frameId).getName();
        }
        return null;
    }
}