package com.pixelatedgames.es.components;

/**
 * User: J
 * Date: 3/28/12
 * Time: 4:33 PM
 */
public class PositionComponent implements IComponent {
    private long _x;
    private long _y;

    public PositionComponent(long x, long y) {
        _x = x;
        _y = y;
    }

    @Override
    public int getType() {
        return 1; // unique identifier
    }
}
