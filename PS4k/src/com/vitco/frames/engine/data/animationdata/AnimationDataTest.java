package com.vitco.frames.engine.data.animationdata;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * test for Animation Data
 */
@SuppressWarnings("ALL")
public class AnimationDataTest {
    private AnimationData ad;

    @Before
    public void setUp() throws Exception {
        ad = new AnimationData();
    }

    @After
    public void tearDown() throws Exception {
        ad.clear();
    }

    @Test
    public void testAddRemovePoint() throws Exception {
        int id = ad.addPoint(5,7,9);
        assert ad.getPoints().length == 1;
        assert ad.getPoints()[0][0][0] == 5;
        assert ad.getPoints()[0][0][1] == 7;
        assert ad.getPoints()[0][0][2] == 9;
        ad.removePoint(id);
        assert ad.getPoints().length == 0;
    }

    @Test
    public void testClear() throws Exception {
        ad.connect(ad.addPoint(1,2,3), ad.addPoint(3,4,5));
        assert ad.getPoints().length == 2;
        assert ad.getLines().length == 1;
        ad.clear();
        assert ad.getPoints().length == 0;
        assert ad.getPoints().length == 0;
    }

    @Test
    public void testMovePoint() throws Exception {
        ad.addPoint(1, 2, 3);
        int id = ad.addPoint(3,1,3);
        ad.movePoint(id, 4, 5, 6);
        assert ad.getPoint(id)[0][0] == 4;
        assert ad.getPoint(id)[0][1] == 5;
        assert ad.getPoint(id)[0][2] == 6;
        ad.removePoint(id);
        assert ad.getPoints().length == 1;
        ad.clear();
    }

    @Test
    public void testConnectAndDisconnect() throws Exception {
        int id1 = ad.addPoint(1,2,3);
        int id2 = ad.addPoint(3,4,5);
        int id3 = ad.addPoint(3,4,6);
        assert id1 == 0;
        assert id2 == 1;
        assert id3 == 2;

        assert !ad.connect(id1, id1);

        assert ad.connect(id1, id2);
        assert !ad.connect(id1, id2);
        assert !ad.disconnect(id3, id2);
        assert ad.connect(id3, id2);
        assert !ad.connect(id3, id2);
        assert !ad.connect(id2, id3);
        assert !ad.disconnect(id1, id3);
        assert ad.disconnect(id1, id2);
        assert !ad.disconnect(id1, id2);
        assert ad.removePoint(id1);
        assert !ad.removePoint(id1);
        assert ad.disconnect(id2, id3);
        assert ad.removePoint(id2);
        assert ad.removePoint(id3);

        assert ad.getLines().length == 0;
        assert ad.getPoints().length == 0;
    }

    @Test
    public void testGetPointsAndLines() throws Exception {
        int id1 = ad.addPoint(1,2,3);
        int id2 = ad.addPoint(4,5,6);
        int id3 = ad.addPoint(7,8,9);
        int[][][] points = ad.getPoints();
        assert points[0][1][0] == id1;
        assert points[0][0][0] == 1;
        assert points[0][0][1] == 2;
        assert points[0][0][2] == 3;
        ad.connect(id2, id3);
        int[][][][] lines = ad.getLines();
        assert lines[0][0][1][0] == id2;
        assert lines[0][1][1][0] == id3;
        ad.connect(id1, id3);
        lines = ad.getLines();
        assert lines[1][0][1][0] == id1;
        assert lines[1][1][1][0] == id3;
        assert ad.removePoint(id2);
        assert !ad.removePoint(id2);
        assert ad.getLines().length == 1;

    }
}
