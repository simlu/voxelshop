package com.vitco.frames.engine.data2;

import com.threed.jpct.SimpleVector;
import com.vitco.frames.engine.data2.container.ExtendedLine;
import com.vitco.frames.engine.data2.container.ExtendedVector;
import com.vitco.frames.engine.data2.container.Frame;
import com.vitco.frames.engine.data2.listener.DataChangeListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implements all functions defined in the AnimationDataInterface
 */
public abstract class AnimationData extends ListenerData implements AnimationDataInterface {
    // make sure we know when we need to rebuild the buffer
    protected AnimationData() {
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
    protected final ArrayList<AnimationData.AnimationIntent> history = new ArrayList<AnimationData.AnimationIntent>();
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

    // ###################### PRIVATE HELPER
    // an action that can be applied or un-applied
    protected enum ATYPE {
        ADD_POINT, REMOVE_POINT, MOVE_POINT_REPLACE,
        CONNECT, DISCONNECT, CLEAR,
        CREATE_FRAME, DELETE_FRAME,
        RESET_FRAME, RENAME_FRAME,
        PLACE_FRAME_POINT,
        REMOVE_FRAME_POINT,
        SET_ACTIVE_FRAME
    }
    protected final class AnimationIntent {
        public ATYPE type = null; // the type of this intent
        private boolean first_call = true; // true iff apply is called for the first time
        private boolean attach = false; // if true this undo is attached to surrounding undo(s)

        // apply this intent
        public void apply() {
            switch (type) {
                case ADD_POINT:  // add point
                    points.put(point1.id, point1);
                    break;
                case REMOVE_POINT:  // remove point
                    if (first_call) {
                        // dc all lines that include this point
                        if (pointsToLines.containsKey(id1)) {
                            ExtendedLine[] lines = new ExtendedLine[pointsToLines.get(id1).size()];
                            pointsToLines.get(id1).toArray(lines);
                            for (ExtendedLine line : lines) {
                                AnimationIntent intent = new AnimationIntent();
                                intent.disconnect(line.point1, line.point2);
                                applyIntent(intent, true);
                            }
                        }
                        // delete this points in all frames
                        for (Integer frameId : frames.keySet()) {
                            if (frames.get(frameId).getPoint(id1) != null) {
                                AnimationIntent intent = new AnimationIntent();
                                intent.removeFramePoint(frameId, id1);
                                applyIntent(intent, true);
                            }
                        }
                        // store point
                        point1 = points.get(id1);
                    }
                    pointsToLines.remove(id1);
                    points.remove(id1);
                    break;
                case MOVE_POINT_REPLACE:  // move point
                    if (first_call) {
                        point2 = points.get(id1);
                    }
                    points.put(id1, point1);
                    break;
                case CONNECT: { // connect points
                    ExtendedLine line = new ExtendedLine(id1, id2);
                    lines.put(id1 + "_" + id2, line);
                    // make sure the pointToLines is initialized
                    if (!pointsToLines.containsKey(id1)) {
                        pointsToLines.put(id1, new ArrayList<ExtendedLine>());
                    }
                    if (!pointsToLines.containsKey(id2)) {
                        pointsToLines.put(id2, new ArrayList<ExtendedLine>());
                    }
                    pointsToLines.get(id1).add(line);
                    pointsToLines.get(id2).add(line);
                    break;
                }
                case DISCONNECT: { // disconnect points
                    ExtendedLine line = lines.get(id1 + "_" + id2);
                    lines.remove(id1 + "_" + id2);
                    // make sure the pointToLines is initialized
                    if (!pointsToLines.containsKey(id1)) {
                        pointsToLines.put(id1, new ArrayList<ExtendedLine>());
                    }
                    if (!pointsToLines.containsKey(id2)) {
                        pointsToLines.put(id2, new ArrayList<ExtendedLine>());
                    }
                    pointsToLines.get(id1).remove(line);
                    pointsToLines.get(id2).remove(line);
                    break;
                }
                case CLEAR:
                    // remove all points (attach)
                    if (first_call) { // delete all points of this frame
                        Integer[] pointIds = new Integer[points.size()];
                        points.keySet().toArray(pointIds);
                        for (Integer pointId : pointIds) {
                            AnimationIntent intent = new AnimationIntent();
                            intent.removePoint(pointId);
                            applyIntent(intent, true);
                        }
                    }
                    break;
                case CREATE_FRAME:
                    frames.put(frameId, new Frame(frameName));
                    break;
                case DELETE_FRAME: {
                    if (first_call) {
                        // delete all points of this frame
                        Frame frameRef = frames.get(frameId);
                        for (Integer pointId : frameRef.getPoints()) {
                            AnimationIntent intent = new AnimationIntent();
                            intent.removeFramePoint(frameId, pointId);
                            applyIntent(intent, true);
                        }
                        if (activeFrame == frameId) { // make sure the frameId is still valid
                            AnimationIntent intent = new AnimationIntent();
                            intent.setActiveFrame(activeFrame, -1);
                            applyIntent(intent, true);
                        }
                    }
                    frames.remove(frameId);
                    break;
                }
                case RESET_FRAME: {
                    if (first_call) {
                        Frame frameRef = frames.get(frameId);
                        for (Integer pointId : frameRef.getPoints()) {
                            AnimationIntent intent = new AnimationIntent();
                            intent.removeFramePoint(frameId, pointId);
                            applyIntent(intent, true);
                        }
                    }
                    break;
                }
                case RENAME_FRAME:
                    frames.get(frameId).setName(frameName);
                    break;
                case PLACE_FRAME_POINT: {
                    Frame frameRef = frames.get(frameId);
                    if (first_call) {
                        point2 = frameRef.getPoint(point1.id);
                    }
                    frameRef.setPoint(point1.id, point1);
                    break;
                }
                case REMOVE_FRAME_POINT:
                    Frame frameRef = frames.get(frameId);
                    if (first_call) {
                        point1 = frameRef.getPoint(id1);
                    }
                    frameRef.removePoint(id1);
                    break;
                case SET_ACTIVE_FRAME:
                    activeFrame = frameId;
                    break;
            }

            first_call = false;
        }

        // remove this intent (undo)
        public void unapply() {
            switch (type) {
                case ADD_POINT:  // add
                    points.remove(point1.id);
                    pointsToLines.remove(point1.id);
                    break;
                case REMOVE_POINT:  // remove
                    points.put(point1.id, point1);
                    break;
                case MOVE_POINT_REPLACE:  // move
                    if (point2 != null) {
                        points.put(id1, point2);
                    } else {
                        points.remove(id1);
                    }
                    break;
                case CONNECT: { // connect points
                    ExtendedLine line = lines.get(id1 + "_" + id2);
                    lines.remove(id1 + "_" + id2);
                    // make sure the pointToLines is initialized
                    if (!pointsToLines.containsKey(id1)) {
                        pointsToLines.put(id1, new ArrayList<ExtendedLine>());
                    }
                    if (!pointsToLines.containsKey(id2)) {
                        pointsToLines.put(id2, new ArrayList<ExtendedLine>());
                    }
                    pointsToLines.get(id1).remove(line);
                    pointsToLines.get(id2).remove(line);
                    break;
                }
                case DISCONNECT: { // disconnect points
                    ExtendedLine line = new ExtendedLine(id1, id2);
                    lines.put(id1 + "_" + id2, line);
                    // make sure the pointToLines is initialized
                    if (!pointsToLines.containsKey(id1)) {
                        pointsToLines.put(id1, new ArrayList<ExtendedLine>());
                    }
                    if (!pointsToLines.containsKey(id2)) {
                        pointsToLines.put(id2, new ArrayList<ExtendedLine>());
                    }
                    pointsToLines.get(id1).add(line);
                    pointsToLines.get(id2).add(line);
                    break;
                }
                case CLEAR:
                    // nothing to do
                    break;
                case CREATE_FRAME:
                    frames.remove(frameId);
                    break;
                case DELETE_FRAME:
                    frames.put(frameId, new Frame(frameName));
                    break;
                case RESET_FRAME:
                    // nothing to do here
                    break;
                case RENAME_FRAME:
                    frames.get(frameId).setName(frameOldName);
                    break;
                case PLACE_FRAME_POINT:
                    if (point2 != null) {
                        frames.get(frameId).setPoint(point2.id, point2);
                    } else {
                        frames.get(frameId).removePoint(point1.id);
                    }
                    break;
                case REMOVE_FRAME_POINT:
                    frames.get(frameId).setPoint(point1.id, point1);
                    break;
                case SET_ACTIVE_FRAME:
                    activeFrame = oldFrameId;
                    break;
            }
        }

        // add point
        private ExtendedVector point1 = null;
        private void addPoint(ExtendedVector point) {
            this.type = ATYPE.ADD_POINT;
            this.point1 = point;
        }

        // remove a point
        private void removePoint(int pointId) {
            this.type = ATYPE.REMOVE_POINT;
            id1 = pointId;
        }

        // move point
        private ExtendedVector point2 = null;
        private void movePoint(Integer pointId, SimpleVector pos) {
            this.type = ATYPE.MOVE_POINT_REPLACE;
            this.id1 = pointId;
            this.point1 = new ExtendedVector(pos.x, pos.y, pos.z, pointId);
        }

        // connect, disconnect
        private int id1 = -1;
        private int id2 = -1;
        private void connect(int id1, int id2) {
            this.type = ATYPE.CONNECT;
            this.id1 = Math.min(id1, id2);
            this.id2 = Math.max(id1, id2);
        }
        private void disconnect(int id1, int id2) {
            this.type = ATYPE.DISCONNECT;
            this.id1 = Math.min(id1, id2);
            this.id2 = Math.max(id1, id2);
        }

        // clear data
        private void clearAll() {
            this.type = ATYPE.CLEAR;
        }

        // create frame
        private String frameName;
        private int frameId = -1;
        private void createFrame(int frameId, String frameName) {
            this.type = ATYPE.CREATE_FRAME;
            this.frameName = frameName;
            this.frameId = frameId;
        }

        // delete Frame
        private void deleteFrame(int frameId) {
            this.type = ATYPE.DELETE_FRAME;
            this.frameId = frameId;
        }

        // rename frame
        private String frameOldName;
        private void renameFrame(int frameId, String frameName) {
            this.type = ATYPE.RENAME_FRAME;
            this.frameName = frameName;
            this.frameOldName = frames.get(frameId).getName();
            this.frameId = frameId;
        }

        // reset frame
        private void resetFrame(int frameId) {
            this.type = ATYPE.RESET_FRAME;
            this.frameId = frameId;
        }

        // add/move a frame point
        private void placeFramePoint(int frameId, ExtendedVector point) {
            this.type = ATYPE.PLACE_FRAME_POINT;
            this.frameId = frameId;
            this.point1 = point;
        }

        // remove a frame point
        private void removeFramePoint(int frameId, int pointId) {
            this.type = ATYPE.REMOVE_FRAME_POINT;
            this.frameId = frameId;
            this.id1 = pointId;
        }

        // sets the active frame
        int oldFrameId = -1;
        private void setActiveFrame(int oldFrameId, int newFrameId) {
            this.type = ATYPE.SET_ACTIVE_FRAME;
            this.oldFrameId = oldFrameId;
            this.frameId = newFrameId;
        }
    }

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
    private void applyIntent(AnimationIntent animationIntent, boolean attach) {
        // delete all "re-dos"
        while (history.size() > historyPosition + 1) {
            history.remove(historyPosition + 1);
        }
        // apply the intent
        animationIntent.apply();
        historyPosition++;
        // set attach
        animationIntent.attach = attach;
        history.add(animationIntent);
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
    public void debug() {
        int i = -1;
        for (AnimationIntent ai : history) {
            i++;
            System.out.println(ai.type + " @ " + ai.attach + (i == historyPosition ? " XXX " : ""));
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
        AnimationIntent intent = new AnimationIntent();
        intent.addPoint(point);
        applyIntent(intent, false);
        return pointId;
    }

    @Override
    public boolean removePoint(int pointId) {
        boolean result = false;
        if (isValid(pointId)) {
            AnimationIntent intent = new AnimationIntent();
            intent.removePoint(pointId);
            applyIntent(intent, false);
            result = true;
        }
        return result;
    }

    @Override
    public boolean movePoint(SimpleVector pos, int pointId) {
        boolean result = false;
        if (isValid(pointId)) {
            // delete last history position if it moved the same point
            if (history.size() > 0) {
                AnimationIntent lastHistory = history.get(history.size()-1);
                if (lastHistory.type == ATYPE.MOVE_POINT_REPLACE &&
                        lastHistory.point1.id == pointId &&
                        lastHistory.frameId == activeFrame) {
                    applyPreviousHistory();
                }
            }
            if (activeFrame == -1) { // move real point
                AnimationIntent intent = new AnimationIntent();
                intent.movePoint(pointId, pos);
                applyIntent(intent, false);
            } else { // move frame point
                AnimationIntent intent = new AnimationIntent();
                ExtendedVector point = new ExtendedVector(pos.x, pos.y, pos.z, pointId);
                intent.placeFramePoint(activeFrame, point);
                applyIntent(intent, false);
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
            AnimationIntent intent = new AnimationIntent();
            intent.connect(id1, id2);
            applyIntent(intent, false);
            result = true;
        }
        return result;
    }

    @Override
    public boolean clearA() {
        boolean result = false;
        if (points.size() > 0) {
            result = true;
            AnimationIntent intent = new AnimationIntent();
            intent.clearAll();
            applyIntent(intent, false);
        }
        return result;
    }

    @Override
    public boolean disconnect(int id1, int id2) {
        boolean result = false;
        if (isValid(id1) && isValid(id2) && areConnected(id1, id2)) {
            AnimationIntent intent = new AnimationIntent();
            intent.disconnect(id1, id2);
            applyIntent(intent, false);
            result = true;
        }
        return result;
    }

    @Override
    public ExtendedVector getPoint(int pointId) {
        if (activeFrame != -1) {
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
        return pointBuffer;
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
        return lineBuffer;
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
            AnimationIntent intent = new AnimationIntent();
            intent.setActiveFrame(activeFrame, frameId);
            applyIntent(intent, false);
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
        AnimationIntent intent = new AnimationIntent();
        int frameId = getFreeFrameId();
        intent.createFrame(frameId, frameName);
        applyIntent(intent, false);
        return frameId;
    }

    @Override
    public boolean deleteFrame(int frameId) {
        boolean result = false;
        if (frames.containsKey(frameId)) {
            AnimationIntent intent = new AnimationIntent();
            intent.deleteFrame(frameId);
            applyIntent(intent, false);
            result = true;
        }
        return result;
    }

    @Override
    public boolean renameFrame(int frameId, String newName) {
        boolean result = false;
        if (frames.containsKey(frameId)) {
            AnimationIntent intent = new AnimationIntent();
            intent.renameFrame(frameId, newName);
            applyIntent(intent, false);
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
        return frameBuffer;
    }

    @Override
    public boolean resetFrame(int frameId) {
        boolean result = false;
        if (frames.containsKey(frameId)) {
            AnimationIntent intent = new AnimationIntent();
            intent.resetFrame(frameId);
            applyIntent(intent, false);
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
