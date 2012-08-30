package com.vitco.frames.engine.data;

import com.threed.jpct.SimpleVector;
import com.vitco.frames.engine.data.container.ExtendedLine;
import com.vitco.frames.engine.data.container.ExtendedVector;
import com.vitco.frames.engine.data.container.Frame;
import com.vitco.frames.engine.data.history.BasicActionIntent;
import com.vitco.frames.engine.data.listener.DataChangeListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implements all functions defined in the AnimationDataInterface
 */
public abstract class AnimationData extends ListenerData implements AnimationDataInterface {

    // constructor
    protected AnimationData() {
        super();
        // make sure we know when we need to rebuild the buffers
        addDataChangeListener(new DataChangeListener() {
            @Override
            public void onAnimationDataChanged() {
                lineBufferValid = false;
                pointBufferValid = false;
                frameBufferValid = false;
            }

            @Override
            public void onAnimationSelectionChanged() {}

            @Override
            public void onVoxelDataChanged() {}

            @Override
            public void onLayerDataChanged() {}
        });
    }

    // ####################### DATA
    // holds the history data
    protected int historyPosition = -1;
    protected final ArrayList<BasicActionIntent> history = new ArrayList<BasicActionIntent>();
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
                        applyIntent(new DisconnectIntent(line.point1, line.point2, true));
                    }
                }
                // delete this points in all frames
                for (Integer frameId : frames.keySet()) {
                    if (frames.get(frameId).getPoint(pointId) != null) {
                        applyIntent(new RemoveFramePointIntent(pointId, frameId, true));
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
                    applyIntent(new RemovePointIntent(pointId, true));
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
                    applyIntent(new RemoveFramePointIntent(pointId, frameId, true));
                }
                if (activeFrame == frameId) { // make sure the frameId is still valid
                    applyIntent(new SetActiveFrameIntent(-1, true));
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

        public ResetFrameIntent(Integer frameId, boolean attach) {
            super(attach);
            this.frameId = frameId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                Frame frameRef = frames.get(frameId);
                for (Integer pointId : frameRef.getPoints()) {
                    applyIntent(new RemoveFramePointIntent(pointId, frameId, true));
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

    // adds a new intent to the history and executes it
    private void applyIntent(BasicActionIntent actionIntent) {
        // delete all "re-dos"
        while (history.size() > historyPosition + 1) {
            history.remove(historyPosition + 1);
        }
        // apply the intent
        actionIntent.apply();
        historyPosition++;
        // and add it to the history
        history.add(actionIntent);
        // invalidate the cache
        notifier.onAnimationDataChanged();
    }

    // apply the next history intent
    private void applyNextHistory() {
        if (history.size() > historyPosition + 1) { // we can still "redo"
            historyPosition++; // move one "up"
            history.get(historyPosition).apply(); // redo action
            // make sure the attached histories are applied
            if (history.size() > historyPosition + 1 && history.get(historyPosition).attach) {
                applyNextHistory();
            } else {
                notifier.onAnimationDataChanged();
            }
        }

    }

    // apply the last history intent
    private void applyPreviousHistory() {
        if (historyPosition > -1) { // we can still undo
            history.get(historyPosition).unapply(); // undo action
            historyPosition--; // move one "down"
            // make sure the attached histories are applied
            if (historyPosition > -1 && history.get(historyPosition).attach) {
                applyPreviousHistory();
            } else {
                notifier.onAnimationDataChanged();
            }
        }
    }

    // displays current history information
    public final void debug() {
        int i = -1;
        for (BasicActionIntent ai : history) {
            i++;
            System.out.println(ai + " @ " + ai.attach + (i == historyPosition ? " XXX " : ""));
        }
        System.out.println("=================");
    }

    // =========================
    // === interface methods ===
    // =========================

    @Override
    public boolean isValid(int pointId) {
        return points.containsKey(pointId);
    }

    @Override
    public int addPoint(SimpleVector position) {
        int pointId = getFreePointId();
        ExtendedVector point = new ExtendedVector(position.x, position.y, position.z, pointId);
        applyIntent(new AddPointIntent(point, false));
        return pointId;
    }

    @Override
    public boolean removePoint(int pointId) {
        boolean result = false;
        if (isValid(pointId)) {
            applyIntent(new RemovePointIntent(pointId, false));
            result = true;
        }
        return result;
    }

    @Override
    public boolean movePoint(int pointId, SimpleVector pos) {
        boolean result = false;
        if (isValid(pointId)) {
            if (activeFrame == -1) { // move real point
                applyIntent(new MovePointIntent(pointId, pos, false));
            } else { // move frame point
                applyIntent(new PlaceFramePointIntent(pointId, pos, activeFrame, false));
            }
            result = true;
        }
        return result;
    }

    @Override
    public boolean areConnected(int id1, int id2) {
        return lines.containsKey(Math.min(id1, id2) + "_" + Math.max(id1, id2));
    }

    @Override
    public boolean connect(int id1, int id2) {
        boolean result = false;
        if (isValid(id1) && isValid(id2) && !areConnected(id1, id2)) {
            applyIntent(new ConnectIntent(id1, id2, false));
            result = true;
        }
        return result;
    }

    @Override
    public boolean clearA() {
        boolean result = false;
        if (points.size() > 0) {
            applyIntent(new ClearAIntent(false));
            result = true;
        }
        return result;
    }

    @Override
    public boolean disconnect(int id1, int id2) {
        boolean result = false;
        if (isValid(id1) && isValid(id2) && areConnected(id1, id2)) {
            applyIntent(new DisconnectIntent(id1, id2, false));
            result = true;
        }
        return result;
    }

    @Override
    public ExtendedVector getPoint(int pointId) {
        if (activeFrame != -1) { // return frame point if defined
            ExtendedVector point = frames.get(activeFrame).getPoint(pointId);
            if (point != null) {
                return point;
            }
        }
        return points.get(pointId);
    }

    ExtendedVector[] pointBuffer = new ExtendedVector[]{};
    boolean pointBufferValid = false;
    @Override
    public ExtendedVector[] getPoints() {
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

    ExtendedVector[][] lineBuffer = new ExtendedVector[][]{};
    boolean lineBufferValid = false;
    @Override
    public ExtendedVector[][] getLines() {
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
    public void undoA() {
        applyPreviousHistory();
    }

    @Override
    public void redoA() {
        applyNextHistory();
    }

    @Override
    public boolean canUndoA() {
        return (historyPosition > -1);
    }

    @Override
    public boolean canRedoA() {
        return (history.size() > historyPosition + 1);
    }

    @Override
    public boolean selectFrame(int frameId) {
        boolean result = false;
        if (frames.containsKey(frameId) || frameId == -1) {
            applyIntent(new SetActiveFrameIntent(frameId, false));
            result = true;
        }
        return result;
    }

    @Override
    public int getSelectedFrame() {
        return activeFrame;
    }

    @Override
    public int createFrame(String frameName) {
        int frameId = getFreeFrameId();
        applyIntent(new CreateFrameIntent(frameId, frameName, false));
        return frameId;
    }

    @Override
    public boolean deleteFrame(int frameId) {
        boolean result = false;
        if (frames.containsKey(frameId)) {
            applyIntent(new DeleteFrameIntent(frameId, false));
            result = true;
        }
        return result;
    }

    @Override
    public boolean renameFrame(int frameId, String newName) {
        boolean result = false;
        if (frames.containsKey(frameId)) {
            applyIntent(new RenameFrameIntent(frameId, newName, false));
            result = true;
        }
        return result;
    }

    Integer[] frameBuffer = new Integer[]{};
    boolean frameBufferValid = false;
    @Override
    public Integer[] getFrames() {
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
    public boolean resetFrame(int frameId) {
        boolean result = false;
        if (frames.containsKey(frameId)) {
            applyIntent(new ResetFrameIntent(frameId, false));
            result = true;
        }
        return result;
    }

    @Override
    public String getFrameName(int frameId) {
        if (frames.containsKey(frameId)) {
            return frames.get(frameId).getName();
        }
        return null;
    }
}