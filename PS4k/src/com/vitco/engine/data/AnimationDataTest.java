package com.vitco.engine.data;

import com.threed.jpct.SimpleVector;
import com.vitco.engine.data.container.ExtendedVector;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Extensive testing of the functions implemented in AnimationData
 */
@SuppressWarnings({"AssertWithSideEffects", "ConstantConditions"})
public final class AnimationDataTest {

    private final Data data = new Data();

    // ************ helper functions

    private int addRandomPoint() {
       Random rand = new Random();
       return data.addPoint(new SimpleVector(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
    }

    // ************ basic tests

    @Test
    public void testIsValid() throws Exception {
        assert !data.isValid(30);
        int id1 = data.addPoint(new SimpleVector(1,2,3));
        assert data.isValid(id1);
        int id2 = data.addPoint(new SimpleVector(3,2,1));
        assert data.isValid(id2);
        data.undoA();
        assert !data.isValid(id2);
        data.redoA();
        int frameId1 = data.createFrame("Frame1");
        data.selectFrame(frameId1);
        data.movePoint(id1, new SimpleVector(5, 6, 3));
        data.selectFrame(-1);
        data.removePoint(id1);
        assert !data.isValid(id1);
        data.undoA();
        assert data.isValid(id1);
        data.selectFrame(frameId1);
        assert data.getPoint(id1).x == 5;
        int frameId2 = data.createFrame("Frame2");
        data.selectFrame(frameId2);
        assert data.getPoint(id1).x == 1;
        data.deleteFrame(frameId1);
        assert !data.selectFrame(frameId1);
        data.clearA();
        assert !data.isValid(id1);
        data.undoA();
        assert data.isValid(id1);
        data.undoA();
        assert data.selectFrame(frameId1);
        assert data.getPoint(id1).x == 5;
        data.clearA();
        assert !data.isValid(id1);
        data.undoA();
        assert data.isValid(id1);
    }

    @Test
    public void testAddRemovePoint() throws Exception {
        int[] ids = new int[20];
        int i = 0;
        for(int t : ids) {
            ids[i] = data.addPoint(new SimpleVector(i, i*2, i*3));
            i++;
        }
        int frameId = data.createFrame("Frame1");
        data.selectFrame(frameId);
        int[] remove = new int[] {3,6,8,5,10,17,13,16,19,1,0,9};
        for (int c : remove) {
            data.removePoint(ids[c]);
        }
        ExtendedVector[] points = data.getPoints();
        assert points.length == 8;
        for (ExtendedVector point : points) {
            for (int k : ids) {
                if (ids[k] == point.id) {
                    assert point.y == k*2;
                }
            }
        }
        for (int k : remove) {
            data.undoA();
        }
        points = data.getPoints();
        assert points.length == 20;
        for (ExtendedVector point : points) {
            for (int k : ids) {
                if (ids[k] == point.id) {
                    assert point.y == k*2;
                }
            }
        }
        for (int k : remove) {
            data.redoA();
        }
        points = data.getPoints();
        assert points.length == 8;
        for (ExtendedVector point : points) {
            for (int k : ids) {
                if (ids[k] == point.id) {
                    assert point.y == k*2;
                }
            }
        }
    }

    @Test
    public void testMovePoint() throws Exception {
        class Util {
            public SimpleVector getPointV1(int i) {
                return new SimpleVector(Math.pow(i*3,5)%29, Math.pow(i*5,7)%11, Math.pow(i*7,3)%13);
            }
            public SimpleVector getPointV2(int i) {
                return new SimpleVector(Math.pow(i*7,5)%19, Math.pow(i*5,7)%37, Math.pow(i*7,3)%67);
            }
        }
        int[] ids = new int[50];
        int i = 0;
        for(int t : ids) {
            ids[i] = data.addPoint(new Util().getPointV1(i));
            i++;
        }
        int[] move = new int[]{20,40,21,3,10,33,21,5,23,9,19};
        for (int t : move) {
            data.movePoint(ids[t], new Util().getPointV2(t));
        }
        for (int t: ids) {
            boolean contains = false;
            for (int j : move) {
                if (j == t) {
                    contains = true;
                }
            }
            assert (contains
                    ? data.getPoint(ids[t]).x == new Util().getPointV2(t).x
                    : data.getPoint(ids[t]).x == new Util().getPointV1(t).x);
        }
        for (int aMove : move) {
            data.undoA();
        }
        for (int t: ids) {
            assert data.getPoint(ids[t]).x == new Util().getPointV1(t).x;
        }
    }

    @Test
    public void testConnectAndAreConnected() throws Exception {
        int id1 = data.addPoint(new SimpleVector(1,2,3));
        int id2 = data.addPoint(new SimpleVector(5,6,7));
        assert !data.areConnected(id1,id2);
        data.addPoint(new SimpleVector(5,7,2));
        assert !data.areConnected(id1,id2);
        data.connect(id1, id2);
        assert data.areConnected(id1,id2);
        assert !data.connect(id1, id2);
        data.addPoint(new SimpleVector(6,9,3));
        assert data.areConnected(id1,id2);
        data.undoA();
        assert data.areConnected(id1,id2);
        data.undoA();
        assert !data.areConnected(id1,id2);
        while (data.canUndoA()) {
            data.undoA();
        }
        assert !data.areConnected(id1, id2);
        while (data.canRedoA()) {
            data.redoA();
        }
        assert data.areConnected(id1, id2);
        while (data.canUndoA()) {
            data.undoA();
        }
        data.redoA();
        data.redoA();
        data.redoA();
        assert !data.areConnected(id1, id2);
        data.addPoint(new SimpleVector(6,7,8));
        assert !data.areConnected(id1, id2);
        assert !data.canRedoA();
        data.redoA();
        assert !data.areConnected(id1, id2);
    }

    @Test
    public void testClearA() throws Exception {
        int id1 = addRandomPoint();
        int id2 = addRandomPoint();
        data.connect(id1, id2);
        int fid1 = data.createFrame("Frame1");
        data.selectFrame(fid1);
        data.movePoint(id1, new SimpleVector(2,5,6));
        data.clearA();
        assert !data.isValid(id1);
        assert !data.isValid(id2);
        addRandomPoint();
        addRandomPoint();
        data.undoA();
        data.undoA();
        data.undoA();
        assert data.isValid(id1);
        assert data.isValid(id2);
        assert data.areConnected(id1, id2);
        while (data.canUndoA()) {
            data.undoA();
        }
        assert !data.isValid(id1);
        assert !data.isValid(id2);
        while (data.canRedoA()) {
            data.redoA();
        }
        assert !data.isValid(id1);
        assert !data.isValid(id2);
    }

    @Test
    public void testDisconnect() throws Exception {
        int id1 = addRandomPoint();
        int id2 = addRandomPoint();
        addRandomPoint();
        data.connect(id1,id2);
        assert data.areConnected(id1,id2);
        int id3 = addRandomPoint();
        assert !data.connect(id1, id2);
        assert !data.disconnect(id2,id3);
        assert data.disconnect(id1, id2);
        data.connect(id2, id3);
        assert data.areConnected(id2, id3);
        assert !data.areConnected(id1, id2);
        data.undoA();
        assert !data.areConnected(id2, id3);
        data.undoA();
        assert data.areConnected(id1, id2);
        while (data.canUndoA()) {
            data.undoA();
        }
        assert !data.areConnected(id1, id2);
        assert !data.areConnected(id2, id3);
        while (data.canRedoA()) {
            data.redoA();
        }
        assert !data.areConnected(id1, id2);
        assert data.areConnected(id2, id3);
    }

    @Test
    public void testGetPoint() throws Exception {
        int id1 = data.addPoint(new SimpleVector(1,2,3));
        assert data.getPoint(id1).x == 1;
        data.undoA();
        assert data.getPoint(id1) == null;
        data.redoA();
        assert data.getPoint(id1).z == 3;
    }

    @Test
    public void testGetPoints() throws Exception {
        int id1 = addRandomPoint();
        int id2 = addRandomPoint();
        assert data.getPoints().length == 2;
        addRandomPoint();
        assert data.getPoints().length == 3;
        data.connect(id1, id2);
        assert data.getPoints().length == 3;
        data.undoA();
        assert data.getPoints().length == 3;
        data.undoA();
        assert data.getPoints().length == 2;
        data.undoA();
        assert data.getPoints().length == 1;
        data.undoA();
        assert data.getPoints().length == 0;
    }

    @Test
    public void testGetLines() throws Exception {
        int id1 = addRandomPoint();
        int id2 = addRandomPoint();
        int id3 = addRandomPoint();
        int id4 = addRandomPoint();
        assert data.getLines().length == 0;
        data.connect(id1, id2);
        assert data.getLines()[0][0].equals(data.getPoint(id1));
        assert data.getLines()[0][1].equals(data.getPoint(id2));
        data.connect(id3, id4);
        assert data.getLines().length == 2;
        data.undoA();
        assert data.getLines().length == 1;
        data.redoA();
        data.disconnect(id1, id2);
        assert data.getLines().length == 1;
        while (data.canUndoA()) {
            data.undoA();
        }
        assert data.getLines().length == 0;
        while (data.canRedoA()) {
            data.redoA();
        }
        assert data.getLines().length == 1;
    }

    @Test
    public void testCanUndoACanRedoA() throws Exception {
        assert !data.canRedoA();
        assert !data.canUndoA();
        addRandomPoint();
        assert data.canUndoA();
        assert !data.canRedoA();
        data.undoA();
        assert !data.canUndoA();
        assert data.canRedoA();
    }

    @Test
    public void testSetFrame() throws Exception {
        data.addPoint(new SimpleVector(1,2,3));
        int fid1 = data.createFrame("Frame1");
        data.selectFrame(fid1);
        int id2 = data.addPoint(new SimpleVector(3,4,5));
        data.selectFrame(-1);
        data.addPoint(new SimpleVector(7,8,9));
        int fid2 = data.createFrame("Frame2");
        data.selectFrame(fid2);
        assert data.getFrames().length == 2;
        data.movePoint(id2, new SimpleVector(5,6,7));
        data.selectFrame(-1);
        assert data.getPoints().length == 3;
        assert data.getPoint(id2).x == 3;
        data.selectFrame(fid1);
        assert data.getPoint(id2).x == 3;
        data.selectFrame(fid2);
        assert data.getPoint(id2).x == 5;
        while (data.canUndoA()) {
            data.undoA();
        }
        assert !data.isValid(id2);
        while (data.canRedoA()) {
            data.redoA();
        }
        assert data.getPoint(id2).x == 5;
        data.selectFrame(-1);
        data.movePoint(id2, new SimpleVector(10,10,10));
        data.selectFrame(fid1);
        assert data.getPoint(id2).x == 10;
        data.selectFrame(fid2);
        assert data.getPoint(id2).x == 5;
        data.selectFrame(-1);
        assert data.getPoint(id2).x == 10;
        while(data.canUndoA()) {
            data.undoA();
        }
        data.redoA();
        data.redoA();
        data.redoA();
        data.redoA();
        assert data.getPoint(id2).x == 3;
    }

    @Test
    public void testCreateDeleteFrame() throws Exception {
        int[] frameIds = new int[20];
        int[] pointIds = new int[20];
        for (int i = 0; i < frameIds.length; i++) {
            frameIds[i] = data.createFrame("Frame" + i);
            data.selectFrame(frameIds[i]);
            pointIds[i] = data.addPoint(new SimpleVector(i,i*2,i*3));
            if (i > 0) {
                data.movePoint(pointIds[i-1], new SimpleVector(i*3, i*4, i*5));
            }
        }
        for (int i = 0; i < frameIds.length - 1; i++) {
            data.selectFrame(frameIds[i+1]);
            assert data.getPoint(pointIds[i]).x == (i+1)*3;
        }
        assert data.getPoint(pointIds[pointIds.length-1]).x == pointIds.length - 1;
        data.selectFrame(-1);
        for (int i = 0; i < frameIds.length - 1; i++) {
            assert data.getPoint(pointIds[i]).x == i;
        }
        ArrayList<Integer> remove = new ArrayList<Integer>();
        remove.addAll(Arrays.asList(3,5,6,8,10,2,16,17));
        for (int rem : remove) {
            data.deleteFrame(frameIds[rem]);
        }
        for (int i = 0; i < frameIds.length - 1; i++) {
            if (remove.contains(frameIds[i]) || i == 0) {
                data.selectFrame(-1);
                assert data.getPoint(pointIds[i]).x == i;
            } else {
                assert data.selectFrame(frameIds[i]);
                assert data.getPoint(pointIds[i-1]).x == (i) * 3;
            }
        }
        while(data.canUndoA()) {
            data.undoA();
        }
        while (data.canRedoA()) {
            data.redoA();
        }
        for (int i = 0; i < frameIds.length - 1; i++) {
            if (remove.contains(frameIds[i]) || i == 0) {
                data.selectFrame(-1);
                assert data.getPoint(pointIds[i]).x == i;
            } else {
                assert data.selectFrame(frameIds[i]);
                assert data.getPoint(pointIds[i-1]).x == (i) * 3;
            }
        }
    }

    @Test
    public void testRenameFrame() throws Exception {
        int fid1 = data.createFrame("Frame1");
        assert data.getFrameName(fid1).equals("Frame1");
        data.renameFrame(fid1, "Frame2");
        assert data.getFrameName(fid1).equals("Frame2");
        data.undoA();
        assert data.getFrameName(fid1).equals("Frame1");
        data.undoA();
        assert data.getFrameName(fid1) == null;
        data.redoA();
        data.redoA();
        assert data.getFrameName(fid1).equals("Frame2");
    }

    @Test
    public void testGetFrames() throws Exception {
        assert data.getFrames().length == 0;
        data.createFrame("Frame1");
        assert data.getFrames().length == 1;
        data.createFrame("Frame2");
        assert data.getFrames().length == 2;
        data.undoA();
        assert data.getFrames().length == 1;
        data.undoA();
        assert data.getFrames().length == 0;
        data.redoA();
        data.redoA();
        assert data.getFrames().length == 2;
        data.undoA();
        assert data.getFrameName(data.getFrames()[0]).equals("Frame1");
    }

    @Test
    public void testResetFrame() throws Exception {
        int id1 = addRandomPoint();
        int id2 = addRandomPoint();
        addRandomPoint();
        int fid1 = data.createFrame("Frame1");
        data.movePoint(id1, new SimpleVector(4,5,6));
        int id4  = addRandomPoint();
        int fid2 = data.createFrame("Frame2");
        data.movePoint(id4, new SimpleVector(1,2,3));
        data.movePoint(id2, new SimpleVector(10,20,30));
        data.selectFrame(fid2);
        data.movePoint(id1, new SimpleVector(0,0,0));
        assert data.getPoint(id1).x == 0;
        data.undoA();
        assert data.getPoint(id1).x == 4;
        data.redoA();
        assert data.getPoint(id1).x == 0;
        data.selectFrame(-1);
        assert data.getPoint(id1).x == 4;
        data.selectFrame(fid1);
        assert data.getPoint(id1).x == 4;
        data.selectFrame(fid2);
        assert data.getPoint(id1).x == 0;
        data.resetFrame(data.getSelectedFrame());
        assert data.getPoint(id1).x == 4;
        data.undoA();
        assert data.getPoint(id1).x == 0;
        data.redoA();
        assert data.getPoint(id1).x == 4;
        data.resetFrame(fid1);
        assert data.getPoint(id1).x == 4;
        while (data.canUndoA()) {
            data.undoA();
        }
        data.redoA();
        data.redoA();
        data.redoA();
        data.redoA();
        data.redoA();
        data.selectFrame(fid1);
        assert data.getPoint(id1).x == 4;
        data.undoA();
        assert data.getPoint(id1).x == 4;
    }

    // ************ some more tests

    @Test
    public void randomMess() {
        @SuppressWarnings({"EmptyMethod", "UnusedParameters"})
        class Util {
            private final Random rand;
            public Util(int seed) {
                rand = new Random(seed);
            }

            public float getFloat() {
                return rand.nextFloat();
            }

            public int getInt(int range) {
                return rand.nextInt(range);
            }

            public void addPoint() {
                data.addPoint(new SimpleVector(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
            }

            public void removePoint() {
                ExtendedVector[] points = data.getPoints();
                if (points.length > 0) {
                    int rem = rand.nextInt(points.length);
                    data.removePoint(points[rem].id);
                }
            }

            public void undo() {
                if (data.canUndoA()) {
                    data.undoA();
                }
            }

            public void redo() {
                if (data.canRedoA()) {
                    data.redoA();
                }
            }

            public void connect() {
                ExtendedVector[] points = data.getPoints();
                if (points.length > 1) {
                    int p1 = rand.nextInt(points.length);
                    int p2 = rand.nextInt(points.length-1);
                    if (p1 == p2) {
                        p2++;
                    }
                    if (!data.areConnected(points[p1].id, points[p2].id)) {
                        data.connect(points[p1].id, points[p2].id);
                    }

                }
            }
            public void disconnect() {
                ExtendedVector[][] lines = data.getLines();
                if (lines.length > 0) {
                    int rem = rand.nextInt(lines.length);
                    data.disconnect(lines[rem][0].id, lines[rem][1].id);
                }
            }

            public void movePoint() {
                ExtendedVector[] points = data.getPoints();
                if (points.length > 0) {
                    int rem = rand.nextInt(points.length);
                    data.movePoint(points[rem].id, new SimpleVector(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
                }
            }

            public void clearA() {
                data.clearA();
            }

            public void createFrame() {
                data.createFrame(new BigInteger(130, rand).toString(32));
            }

            public void deleteFrame() {
                Integer[] frames = data.getFrames();
                if (frames.length > 0) {
                    int rem = rand.nextInt(frames.length);
                    data.deleteFrame(frames[rem]);
                }
            }

            public void renameFrame() {
                Integer[] frames = data.getFrames();
                if (frames.length > 0) {
                    int rem = rand.nextInt(frames.length);
                    data.renameFrame(frames[rem], new BigInteger(130, rand).toString(32));
                }
            }

            public void resetFrame() {
                Integer[] frames = data.getFrames();
                if (frames.length > 0) {
                    int rem = rand.nextInt(frames.length);
                    data.resetFrame(frames[rem]);
                }
            }

            public void selectFrame() {
                Integer[] frames = data.getFrames();
                if (frames.length > 0) {
                    int rem = rand.nextInt(frames.length);
                    data.selectFrame(frames[rem]);
                }
            }

            public void debug(String text, int k, int l) {
//                if (k > 99900 && l == 38635) {
//                    System.out.println(text);
//                    ExtendedVector[] points = data.getPoints();
//                    for (int i = 0; i < points.length; i++){
//                        System.out.println(points[i]);
//                    }
//                }
            }
        }

        // do the test
        for (int seed = 42000; seed < 42001; seed ++) {
            Util util = new Util(seed);
            float[] prob = new float[13];
            for (int k = 0; k < prob.length; k++) {
                prob[k] = util.getFloat();
            }
            for (int k = 0; k < 100000; k++) {
                switch (util.getInt(13) + 1) {
                    case 1:
                        if (util.getFloat() < prob[0]) {
                            util.addPoint();
                            util.debug("add", k, seed);
                        }
                        break;
                    case 2:
                        if (util.getFloat() < prob[1]) {
                            util.removePoint();
                            util.debug("rem", k, seed);
                        }
                        break;
                    case 3:
                        if (util.getFloat() < prob[2]) {
                            util.undo();
                            util.debug("undo", k, seed);
                        }
                        break;
                    case 4:
                        if (util.getFloat() < prob[3]) {
                            util.redo();
                            util.debug("redo", k, seed);
                        }
                        break;
                    case 5:
                        if (util.getFloat() < prob[4]) {
                            util.connect();
                            util.debug("connect", k, seed);
                        }
                        break;
                    case 6:
                        if (util.getFloat() < prob[5]) {
                            util.disconnect();
                            util.debug("disconnect", k, seed);
                        }
                        break;
                    case 7:
                        if (util.getFloat() < prob[6]) {
                            util.movePoint();
                            util.debug("movePoint", k, seed);
                        }
                        break;
                    case 8:
                        if (util.getFloat() < prob[7]) {
                            util.clearA();
                            util.debug("clearA", k, seed);
                        }
                        break;
                    case 9:
                        if (util.getFloat() < prob[8]) {
                            util.createFrame();
                            util.debug("createFrame", k, seed);
                        }
                        break;
                    case 10:
                        if (util.getFloat() < prob[9]) {
                            util.deleteFrame();
                            util.debug("deleteFrame", k, seed);
                        }
                        break;
                    case 11:
                        if (util.getFloat() < prob[10]) {
                            util.renameFrame();
                            util.debug("renameFrame", k, seed);
                        }
                        break;
                    case 12:
                        if (util.getFloat() < prob[11]) {
                            util.resetFrame();
                            util.debug("resetFrame", k, seed);
                        }
                        break;
                    case 13:
                        if (util.getFloat() < prob[12]) {
                            util.selectFrame();
                            util.debug("selectFrame", k, seed);
                        }
                        break;
                }
            }
            System.out.println(seed);

            // wind forward
            while (data.canRedoA()) {
                data.redoA();
            }

            // store
            ExtendedVector[] points = data.getPoints();
            ExtendedVector[][] lines = data.getLines();
            Integer[] frames = data.getFrames();

            // wind backwards
            while (data.canUndoA()) {
                data.undoA();
            }

            assert data.getPoints().length == 0;
            assert data.getLines().length == 0;
            assert data.getFrames().length == 0;

            // wind forward
            while (data.canRedoA()) {
                data.redoA();
            }

            // store new version
            ExtendedVector[] points2 = data.getPoints();
            ExtendedVector[][] lines2 = data.getLines();
            Integer[] frames2 = data.getFrames();

            // basic assertion
            //data.debug();
            assert points.length == points2.length;
            assert lines.length == lines2.length;
            assert frames.length == frames2.length;

            // sort points and compare
            Arrays.sort(points, new Comparator<ExtendedVector>() {
                @Override
                public int compare(ExtendedVector o1, ExtendedVector o2) {
                    return o1.id - o2.id;
                }
            });
            Arrays.sort(points2, new Comparator<ExtendedVector>() {
                @Override
                public int compare(ExtendedVector o1, ExtendedVector o2) {
                    return o1.id - o2.id;
                }
            });
            for (int i = 0; i < Math.max(points2.length, points.length); i++) {
                //System.out.println((points.length > i ? points[i].id : "?") + " & " + (points2.length > i ? points2[i].id : "?") +
                //        (points.length > i && points2.length > i && points2[i].id == points[i].id ? "" : " fail"));
                assert points[i].id == points2[i].id;
                assert points[i].x == points2[i].x;
                assert points[i].y == points2[i].y;
                assert points[i].z == points2[i].z;
            }

            // sort lines and compare
            Arrays.sort(lines, new Comparator<ExtendedVector[]>() {
                @Override
                public int compare(ExtendedVector[] o1, ExtendedVector[] o2) {
                    return o1[0].id != o2[0].id ? o1[0].id - o2[0].id : o1[1].id - o2[1].id;
                }
            });
            Arrays.sort(lines2, new Comparator<ExtendedVector[]>() {
                @Override
                public int compare(ExtendedVector[] o1, ExtendedVector[] o2) {
                    return o1[0].id != o2[0].id ? o1[0].id - o2[0].id : o1[1].id - o2[1].id;
                }
            });
            for (int i = 0; i < Math.max(lines2.length, lines.length); i++) {
                assert lines[i][0].id == lines2[i][0].id;
                assert lines[i][1].id == lines2[i][1].id;
                assert lines[i][0].x == lines2[i][0].x;
                assert lines[i][1].x == lines2[i][1].x;
                assert lines[i][0].y == lines2[i][0].y;
                assert lines[i][1].y == lines2[i][1].y;
                assert lines[i][0].z == lines2[i][0].z;
                assert lines[i][1].z == lines2[i][1].z;
            }

            // sort frames and compare
            Arrays.sort(frames, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1 - o2;
                }
            });
            Arrays.sort(frames2, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1 - o2;
                }
            });
            for (int i = 0; i < Math.max(frames.length, frames2.length); i++) {
                assert frames[i].equals(frames2[i]);
                assert data.getFrameName(frames[i]) != null;
            }

            // wind backwards again
            while (data.canUndoA()) {
                data.undoA();
            }

            assert data.getPoints().length == 0;
            assert data.getLines().length == 0;
            assert data.getFrames().length == 0;
        }

    }

    @Test
    public void testSimpleFrameInteraction() {
        int id1 = data.addPoint(new SimpleVector(1,2,3));
        int frameId1 = data.createFrame("Frame1");
        assert data.getFrames().length == 1;
        data.selectFrame(frameId1);
        data.movePoint(id1, new SimpleVector(2,4,2));
        data.selectFrame(-1);
        assert data.getPoint(id1).x == 1;
        data.selectFrame(frameId1);
        assert data.getPoint(id1).x == 2;
        data.undoA();
        assert data.getPoint(id1).x == 1;
    }

    @Test
    public void testUndoRedo() {
        int id1 = data.addPoint(new SimpleVector(1,2,3));
        int id2 = data.addPoint(new SimpleVector(6,5,4));
        int id3 = data.addPoint(new SimpleVector(6,5,4));
        int id4 = data.addPoint(new SimpleVector(6,5,4));
        data.removePoint(id1);
        data.removePoint(id3);
        assert !data.connect(id1, id4);
        data.connect(id2, id4);
        data.clearA();
        data.undoA();
        data.undoA();
        data.undoA();
        data.undoA();
        data.redoA();
        data.redoA();
        data.redoA();
        data.redoA();
        data.undoA();
        data.undoA();
        data.undoA();
        data.undoA();
        data.redoA();
        data.redoA();
        data.redoA();
        assert data.areConnected(id2, id4);
        assert !data.isValid(id1);
        data.redoA();
        assert !data.canRedoA();
        assert !data.areConnected(id2, id4);
    }

    @Test
    public void testRemoveConnectedPointIntense() {
        int id1 = data.addPoint(new SimpleVector(1,2,3));
        int id2 = data.addPoint(new SimpleVector(6,5,4));
        int id3 = data.addPoint(new SimpleVector(6,5,4));
        int id4 = data.addPoint(new SimpleVector(6,5,4));
        data.connect(id1, id2);
        data.connect(id2, id3);
        data.connect(id3, id4);
        data.removePoint(id2);
        int id5 = data.addPoint(new SimpleVector(4,5,6));
        assert !data.areConnected(id1, id2);
        assert data.areConnected(id3, id4);
        assert data.isValid(id5);
        data.undoA();
        assert !data.areConnected(id1, id2);
        assert data.areConnected(id3, id4);
        data.undoA();
        assert data.areConnected(id1, id2);
        assert data.areConnected(id3, id4);
        data.undoA();
        assert data.areConnected(id1, id2);
        assert !data.areConnected(id3, id4);
        data.undoA();
        assert data.areConnected(id1, id2);
        assert !data.areConnected(id3, id4);
        data.undoA();
        assert !data.areConnected(id1, id2);
        assert !data.areConnected(id3, id4);
        data.undoA();
        assert !data.isValid(id4);
        data.redoA();
        data.redoA();
        data.redoA();
        data.redoA();
        data.redoA();
        assert !data.areConnected(id1, id2);
        assert data.areConnected(id3, id4);
        assert data.canRedoA();
        assert !data.isValid(id5);
        data.redoA();
        assert data.isValid(id5);
        data.clearA();
    }

    @Test
    public void testRemoveConnectedPoint() {
        int id1 = data.addPoint(new SimpleVector(1,2,3));
        int id2 = data.addPoint(new SimpleVector(6,5,4));
        data.connect(id1, id2);
        data.removePoint(id1);
        assert !data.areConnected(id1, id2);
        assert data.getPoint(id1) == null;
        data.undoA();
        assert data.getPoint(id1) != null;
        assert data.areConnected(id1, id2);
        data.redoA();
        assert !data.areConnected(id1, id2);
        assert data.getPoint(id1) == null;
        data.clearA();
    }

    @Test
    public void testPointInteractions() throws Exception {
        // test adding of point
        int id = data.addPoint(new SimpleVector(3,2,1));
        assert !data.canRedoA();
        assert data.canUndoA();
        assert data.getPoint(id) != null;
        data.undoA();
        assert data.canRedoA();
        assert !data.canUndoA();
        assert data.getPoint(id) == null;
        data.redoA();
        assert !data.canRedoA();
        assert data.canUndoA();
        assert data.getPoint(id) != null;
        assert data.getPoint(id).x == 3;
        assert data.getPoint(id).y == 2;
        assert data.getPoint(id).z == 1;
        // test removal of point
        data.removePoint(id);
        assert data.getPoint(id) == null;
        assert !data.canRedoA();
        assert data.canUndoA();
        data.redoA();
        assert data.getPoint(id) == null;
        assert !data.canRedoA();
        assert data.canUndoA();
        int id2 = data.addPoint(new SimpleVector(6,5,4));
        data.undoA();
        data.undoA();
        assert data.getPoint(id) != null;
        data.undoA();
        assert data.getPoint(id) == null;
        assert !data.canUndoA();
        assert data.canRedoA();
        data.redoA();
        data.redoA();
        assert data.getPoint(id2) == null;
        assert data.canRedoA();
        assert data.canUndoA();
        data.redoA();
        assert data.getPoint(id2) != null;
        assert !data.canRedoA();
        // test moving of point
        data.movePoint(id2, new SimpleVector(9,8,7));
        assert data.getPoint(id2).x == 9;
        data.undoA();
        assert data.getPoint(id2).x == 6;
        data.redoA();
        assert data.getPoint(id2).x == 9;
        // test move replace of point
        data.movePoint(id2, new SimpleVector(12,11,10));
        assert data.getPoint(id2).x == 12;
        data.undoA();
        assert data.getPoint(id2).x == 9;
        data.undoA();
        data.undoA();
        data.undoA();
        data.undoA();
        assert !data.canUndoA();
        // try clearing redos
        id = data.addPoint(new SimpleVector(1,2,3));
        data.movePoint(id, new SimpleVector(3,4,5));
        data.movePoint(id, new SimpleVector(4,5,6));
        assert data.getPoint(id).x == 4;
        assert !data.canRedoA();
        data.undoA();
        assert data.getPoint(id).x == 3;
        data.undoA();
        assert data.getPoint(id).x == 1;
        data.undoA();
        assert data.getPoint(id) == null;
        // test clear
        data.redoA();
        assert data.getPoint(id).x == 1;
        data.clearA();
        assert data.getPoint(id) == null;
        data.undoA();
        assert data.getPoint(id).x == 1;
        data.addPoint(new SimpleVector(4,6,7));
        data.clearA();
        assert data.getPoint(id) == null;
        data.undoA();
        assert data.getPoint(id).x == 1;
    }

}
