package com.vitco.app.low.hull;

import org.junit.Test;

/**
 * Test the functionality.
 */
public class MinMaxTrackerTest {
    @Test
    public void test() throws Exception {
        MinMaxTracker tracker = new MinMaxTracker();

        assert tracker.getMin() == Short.MAX_VALUE;
        assert tracker.getMax() == Short.MIN_VALUE;

        tracker.add((short) 1);
        tracker.add((short) 2);
        tracker.add((short) 3);
        tracker.add((short) 4);

        assert tracker.getMin() == 1;
        assert tracker.getMax() == 4;

        tracker.remove((short) 2);
        tracker.remove((short) 3);

        assert tracker.getMin() == 1;
        assert tracker.getMax() == 4;

        tracker.add((short) 2);
        tracker.add((short) 2);
        tracker.remove((short) 4);
        tracker.remove((short) 1);

        assert tracker.getMin() == 2;
        assert tracker.getMax() == 2;

        tracker.remove((short) 2);

        assert tracker.getMin() == 2;
        assert tracker.getMax() == 2;

        tracker.remove((short) 2);

        assert tracker.getMin() == Short.MAX_VALUE;
        assert tracker.getMax() == Short.MIN_VALUE;

    }
}
