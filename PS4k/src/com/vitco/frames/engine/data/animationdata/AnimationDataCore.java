package com.vitco.frames.engine.data.animationdata;

import com.vitco.frames.engine.data.listener.DataChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Main Container for a file that we work on.
 *
 * Holds basic animation data:
 * points, lines, color blocks (attached to lines), new frames (points moving from original frame)
 * Also provides means to add/remove/change the data
 * and stores history.
 */
public class AnimationDataCore implements AnimationDataCoreInterface {

    // todo implement undo / redo

    // todo add r tree and voxel support

    // todo add frame support and voxel support

    private static final long serialVersionUID = -6244506065643584549L;

    protected final ArrayList<DataChangeListener> listeners = new ArrayList<DataChangeListener>();

    private final Map<Integer, float[]> points = new HashMap<Integer, float[]>();
    private final Map<String, int[]> lines = new HashMap<String, int[]>();

    // indexes (helper)
    private final Map<Integer, ArrayList<String>> pointToLine = new HashMap<Integer, ArrayList<String>>();

    private int lastAddedId = -1; // the last id that was added ("to get free id")

    // internal - to notify the listeners
    private void notifyAnimationDataChangeListener() {
        // invalidate the buffer
        lineBufferValid = false;
        pointBufferValid = false;
        // notify
        for (DataChangeListener dcl : listeners) {
            dcl.onAnimationDataChanged();
        }
    }

    // internal - returns true if a point id is valid
    protected boolean isValid(int id) {
        return points.containsKey(id);
    }

    // internal - returns a free id for points map
    private int getFreeId() {
        do {
            lastAddedId++;
        }
        while (points.containsKey(lastAddedId)) ;
        return lastAddedId;
    }

    @Override
    public int addPoint(float x, float y, float z) {
        int id = getFreeId();
        pointToLine.put(id, new ArrayList<String>()); // this point is connected with zero lines
        points.put(id, new float[]{x, y, z});
        notifyAnimationDataChangeListener();
        return id;
    }

    @Override
    public boolean removePoint(int id) {
        // no line associated and able to remove
        if (isValid(id) && pointToLine.containsKey(id)) {
            if ((pointToLine.get(id).size() != 0)) { // remove all lines
                ArrayList<String> tmp = pointToLine.get(id);
                while (tmp.size() > 0) {
                    String[] string_ids = tmp.get(0).split("_");
                    int id1 = Integer.valueOf(string_ids[0]);
                    int id2 = Integer.valueOf(string_ids[1]);

                    String name1 = id1 + "_" + id2;
                    String name2 = id2 + "_" + id1;

                    if ((lines.remove(name1) != null) ||
                            (lines.remove(name2) != null)) {
                        pointToLine.get(id1).remove(name1);
                        pointToLine.get(id1).remove(name2);
                        pointToLine.get(id2).remove(name1);
                        pointToLine.get(id2).remove(name2);
                    }
                }
            }
            if (points.remove(id) != null) {
                notifyAnimationDataChangeListener();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean movePoint(int id, float x, float y, float z) {
        boolean result = false;
        if (isValid(id)) {
            points.put(id, new float[]{x, y, z});
            notifyAnimationDataChangeListener();
            result = true;
        }
        return result;
    }

    @Override
    public boolean areConnected(int id1, int id2) {
        return lines.containsKey(id1 + "_" + id2) || lines.containsKey(id2 + "_" + id1);
    }

    @Override
    public boolean connect(int id1, int id2) {
        String name1 = id1 + "_" + id2;
        String name2 = id2 + "_" + id1;

        if (isValid(id1) && isValid(id2)
                && !lines.containsKey(name1) && !lines.containsKey(name2)
                && id1 != id2) {
            int[] line = new int[] {id1, id2};
            lines.put(name1, line);
            // these points have now (another) link
            pointToLine.get(id1).add(name1);
            pointToLine.get(id2).add(name1);
            notifyAnimationDataChangeListener();
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        points.clear();
        lines.clear();
        pointToLine.clear();
        notifyAnimationDataChangeListener();
    }

    @Override
    public boolean disconnect(int id1, int id2) {
        String name1 = id1 + "_" + id2;
        String name2 = id2 + "_" + id1;
        if ((lines.remove(name1) != null) ||
                (lines.remove(name2) != null)) {
            // these points have one link less
            pointToLine.get(id1).remove(name1);
            pointToLine.get(id1).remove(name2);
            pointToLine.get(id2).remove(name1);
            pointToLine.get(id2).remove(name2);
            notifyAnimationDataChangeListener();
            return true;
        }
        return false;
    }

    // returns the formatted point for a key ([x, y, z], key)
    @Override
    public float[][] getPoint(int id) {
        if (isValid(id)) {
            float[] point = points.get(id);
            return new float[][] {point, new float[]{id}};
        } else {
            return null;
        }
    }

    private float[][][] pointBuffer = new float[][][]{};
    private boolean pointBufferValid = false;
    @Override
    public float[][][] getPoints() {
        if (!pointBufferValid) {
            if (pointBuffer.length != points.size()) {
                pointBuffer = new float[points.size()][][];
            }
            int i = 0;
            for (int key : points.keySet()) {
                pointBuffer[i++] = getPoint(key);
            }
            pointBufferValid = true;
        }
        return pointBuffer.clone();
    }

    private float[][][][] lineBuffer = new float[][][][]{};
    private boolean lineBufferValid = false;
    @Override
    public float[][][][] getLines() {
        if (!lineBufferValid) {
            if (lineBuffer.length != lines.size()) {
               lineBuffer = new float[lines.size()][2][][];
            }
            int i = 0;
            for (int[] value : lines.values()) {
                lineBuffer[i][0] = getPoint(value[0]);
                lineBuffer[i][1] = getPoint(value[1]);
                i++;
            }
            lineBufferValid = true;
        }
        return lineBuffer.clone();
    }

    @Override
    public void addDataChangeListener(DataChangeListener dcl) {
        listeners.add(dcl);
    }

    @Override
    public void removeDataChangeListener(DataChangeListener dcl) {
        listeners.remove(dcl);
    }
}
