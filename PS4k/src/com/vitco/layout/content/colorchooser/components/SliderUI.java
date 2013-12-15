package com.vitco.layout.content.colorchooser.components;

/**
 * Extends Basic slider UI and gives access to some internal variables and functions.
 */

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;

public class SliderUI extends BasicSliderUI {
    public SliderUI(JSlider b) {
        super(b);
    }

    public final JSlider getSlider() {
        return slider;
    }

    public final Rectangle getContentRect() {
        return contentRect;
    }

    public final int getXPositionForValue(int value) {
        return xPositionForValue(value);
    }

    public final int getValueForXPosition(int value) {
        return valueForXPosition(value);
    }

    public final int getYPositionForValue(int value) {
        return yPositionForValue(value);
    }

    public final int getValueForYPosition(int value) {
        return valueForYPosition(value);
    }
}
