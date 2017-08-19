package com.vitco.app.core.data.container;

import java.io.Serializable;

/**
 * A line that connects two ExtendedVectors (points).
 *
 * Reference to the points is by id.
 */
public class ExtendedLine implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int point1;
    public final int point2;
    public ExtendedLine(int point1, int point2) {
        this.point1 = point1;
        this.point2 = point2;
    }
}
