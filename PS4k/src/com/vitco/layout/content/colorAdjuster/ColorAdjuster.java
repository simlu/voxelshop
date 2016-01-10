package com.vitco.layout.content.colorAdjuster;

import com.vitco.layout.content.colorchooser.components.colorslider.HSBTab;

import javax.swing.*;
import java.awt.*;

/**
 * Content for the color adjuster frame.
 */
public class ColorAdjuster implements ColorAdjusterInterface {
    @Override
    public JComponent build(Frame frame) {
        return new HSBTab();
    }
}
