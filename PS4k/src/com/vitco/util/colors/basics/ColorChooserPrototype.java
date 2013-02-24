package com.vitco.util.colors.basics;

import javax.swing.*;
import java.util.ArrayList;

/**
 * A prototype for the all color chooser.
 */
public abstract class ColorChooserPrototype extends JPanel {
    private final ArrayList<ColorChangeListener> listeners = new ArrayList<ColorChangeListener>();

    // notify all listeners
    protected final void notifyListeners(float[] hsb) {
        for (ColorChangeListener ccl : listeners) {
            ccl.colorChanged(hsb);
        }
    }

    // add a listener
    public final void addColorChangeListener(ColorChangeListener ccl) {
        listeners.add(ccl);
    }

    // remove a listener
    public final void removeColorChangeListener(ColorChangeListener ccl) {
        listeners.remove(ccl);
    }
}
