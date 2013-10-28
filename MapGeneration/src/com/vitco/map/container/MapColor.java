package com.vitco.map.container;

import org.apache.commons.math3.ml.clustering.Clusterable;

import java.awt.*;

/**
 * Clusterable color
 */
public class MapColor implements Clusterable {
    private final double[] rgb;
    private final int[] pos;

    public MapColor(Color color, int[] pos) {
        rgb = new double[] {
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        };
        this.pos = pos;
    }

    public int getColorAsInt() {
        return new Color((int)rgb[0], (int)rgb[1], (int)rgb[2]).getRGB();
    }

    public int[] getPos() {
        return pos;
    }

    @Override
    public double[] getPoint() {
        return rgb;
    }
}
