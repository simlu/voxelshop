package com.vitco.util.colors;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * A prototype for the all color chooser.
 */
abstract class ColorChooserPrototype extends JPanel {
    private final ArrayList<ColorChangeListener> listeners = new ArrayList<ColorChangeListener>();

    protected final void notifyListeners(Color color) {
        for (ColorChangeListener ccl : listeners) {
            ccl.colorChanged(color);
        }
    }

    public final void addColorChangeListener(ColorChangeListener ccl) {
        listeners.add(ccl);
    }

    public final void removeColorChangeListener(ColorChangeListener ccl) {
        listeners.remove(ccl);
    }
}
