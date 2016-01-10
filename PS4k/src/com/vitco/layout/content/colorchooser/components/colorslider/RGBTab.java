package com.vitco.layout.content.colorchooser.components.colorslider;

import com.vitco.layout.content.colorchooser.components.ColorSliderPrototype;
import com.vitco.layout.content.colorchooser.components.NumberBox;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

/**
 * rgb tab
 */
public class RGBTab extends TabPrototype {

    // the sliders
    private final HorizontalColorSlider rSlider = new HorizontalColorSlider(0, 255, 0);
    private final HorizontalColorSlider gSlider = new HorizontalColorSlider(0, 255, 0);
    private final HorizontalColorSlider bSlider = new HorizontalColorSlider(0, 255, 0);

    public RGBTab() {
        NumberBox rBox = new NumberBox(0, 255, 0);
        NumberBox gBox = new NumberBox(0, 255, 0);
        NumberBox bBox = new NumberBox(0, 255, 0);
        init(
                new String[]{"R", "G", "B"},
                new ColorSliderPrototype[]{rSlider, gSlider, bSlider},
                new NumberBox[]{rBox, gBox, bBox}
        );
    }

    @Override
    protected void onSliderChange(int id, ChangeEvent e) {
        JSlider source = ((JSlider) e.getSource());
        update(new Color(
                        id == 0 ? source.getValue() : color.getRed(),
                        id == 1 ? source.getValue() : color.getGreen(),
                        id == 2 ? source.getValue() : color.getBlue()),
                false, !source.getValueIsAdjusting());
    }

    @Override
    protected void onTextFieldChange(int id, NumberBox source) {
        update(new Color(
                        id == 0 ? source.getValue() : color.getRed(),
                        id == 1 ? source.getValue() : color.getGreen(),
                        id == 2 ? source.getValue() : color.getBlue()),
                false, true);
    }

    @Override
    protected void refreshUI() {
        // repaint the slider
        rSlider.setLeftColor(new Color(0, color.getGreen(), color.getBlue()));
        rSlider.setRightColor(new Color(255, color.getGreen(), color.getBlue()));
        rSlider.repaint();

        gSlider.setLeftColor(new Color(color.getRed(), 0, color.getBlue()));
        gSlider.setRightColor(new Color(color.getRed(), 255, color.getBlue()));
        gSlider.repaint();

        bSlider.setLeftColor(new Color(color.getRed(), color.getGreen(), 0));
        bSlider.setRightColor(new Color(color.getRed(), color.getGreen(), 255));
        bSlider.repaint();

        // set the values
        setValues(new int[] {color.getRed(), color.getGreen(), color.getBlue()});
    }

    @Override
    protected void notifyColorChange(Color color) {
        // nothing to do here
    }
}

