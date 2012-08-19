package com.vitco.frames.engine.controller.animationdata;

import com.vitco.frames.engine.controller.listener.DataChangeListener;
import com.vitco.util.RTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private static final float[] ZEROS = new float[] {0, 0, 0};

    protected final ArrayList<DataChangeListener> listeners = new ArrayList<DataChangeListener>();

    private final Map<Integer, int[]> points = new HashMap<Integer, int[]>();
    private final Map<String, int[]> lines = new HashMap<String, int[]>();

    // indexes (helper)
    private final RTree<Integer> indexedPoints = new RTree<Integer>(50, 2, 3);
    private final Map<Integer, ArrayList<String>> pointToLine = new HashMap<Integer, ArrayList<String>>();

    private int lastAddedId = -1; // the last id that was added ("to get free id")

    // internal - rebuild the r tree index for "near" point access
    private void rebuildIndex() {
        indexedPoints.clear();
        for (Map.Entry<Integer, int[]> entry : points.entrySet()) {
            int[] point = entry.getValue();
            indexedPoints.insert(new float[]{point[0], point[1], point[2]}, ZEROS, entry.getKey());
        }
    }

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

    // returns a nearest point if there are any in the radius (Voxel!)
    @Override
    public int getNearPoint(int x, int y, int z, float[] radius) {
      // todo create test
      List<Integer> search =
              indexedPoints.search(
                      new float[] {x - radius[0], y - radius[1], z - radius[2]},
                      new float[] {radius[0]*2, radius[1]*2, radius[2]*2}
              );
      if (search.size() == 0) {
          return -1;
      } else {
          return search.get(0);
      }
    }

    @Override
    public int addPoint(int x, int y, int z) {
        int id = getFreeId();
        pointToLine.put(id, new ArrayList<String>()); // this point is connected with zero lines
        points.put(id, new int[]{x, y, z});
        indexedPoints.insert(new float[] {x, y, z}, ZEROS, id); // index
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
            int[][] p = getPoint(id);
            if (points.remove(id) != null) {
                if (indexedPoints.delete(new float[] {p[0][0], p[0][1], p[0][2]}, ZEROS, id)) { // index
                    notifyAnimationDataChangeListener();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean movePoint(int id, int x, int y, int z) {
        boolean result = false;
        if (isValid(id)) {
            int[][] p = getPoint(id);
            if (indexedPoints.delete(new float[] {p[0][0], p[0][1], p[0][2]}, ZEROS, id)) { // delete old index
                points.put(id, new int[]{x, y, z});
                indexedPoints.insert(new float[] {x, y, z}, ZEROS, id); // index
                notifyAnimationDataChangeListener();
                result = true;
            }
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
        indexedPoints.clear();
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
    public int[][] getPoint(int id) {
        if (isValid(id)) {
            int[] point = points.get(id);
            return new int[][] {point, new int[]{id}};
        } else {
            return null;
        }
    }

    private int[][][] pointBuffer = new int[][][]{};
    private boolean pointBufferValid = false;
    @Override
    public int[][][] getPoints() {
        if (!pointBufferValid) {
            if (pointBuffer.length != points.size()) {
                pointBuffer = new int[points.size()][][];
            }
            int i = 0;
            for (int key : points.keySet()) {
                pointBuffer[i++] = getPoint(key);
            }
            pointBufferValid = true;
        }
        return pointBuffer.clone();
    }

    private int[][][][] lineBuffer = new int[][][][]{};
    private boolean lineBufferValid = false;
    @Override
    public int[][][][] getLines() {
        if (!lineBufferValid) {
            if (lineBuffer.length != lines.size()) {
               lineBuffer = new int[lines.size()][2][][];
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
