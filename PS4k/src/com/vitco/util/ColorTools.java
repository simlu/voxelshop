package com.vitco.util;

import java.awt.*;

/**
 * Basic color conversion tools
 */
public class ColorTools {
    public final static float[] colorToHSB(Color color) {
        return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    }

    public final static Color hsbToColor(float[] hsb) {
        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }

    public final static double perceivedBrightness(Color color) {
        return 0.299*color.getRed() + 0.587*color.getGreen() + 0.114*color.getBlue();
    }

    public final static double perceivedBrightness(float[] hsb) {
        return perceivedBrightness(hsbToColor(hsb));
    }

}
