package com.vitco.layout.content.colorchooser.components.colorslider;

import com.vitco.layout.content.colorchooser.components.ColorSliderPrototype;
import com.vitco.layout.content.colorchooser.components.NumberBox;
import com.vitco.util.misc.ColorTools;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

/**
 * hsb tab
 */
public class HSBTab extends TabPrototype {

    private final static int HUE_STEPCOUNT = 360;
    private final static int STEPCOUNT = 100;

    private final HorizontalColorSlider sSlider = new HorizontalColorSlider(0, STEPCOUNT, 0);
    private final HorizontalColorSlider bSlider = new HorizontalColorSlider(0, STEPCOUNT, 0);

    public HSBTab() {
        NumberBox hBox = new NumberBox(0, HUE_STEPCOUNT, 0);
        NumberBox sBox = new NumberBox(0, STEPCOUNT, 0);
        NumberBox bBox = new NumberBox(0, STEPCOUNT, 0);
        HorizontalHueSlider hSlider = new HorizontalHueSlider(0, HUE_STEPCOUNT, 0);
        init(
                new String[]{"H", "S", "B"},
                new ColorSliderPrototype[]{hSlider, sSlider, bSlider},
                new NumberBox[]{hBox, sBox, bBox}
        );
    }

    @Override
    protected void onSliderChange(int id, ChangeEvent e) {
        JSlider source = ((JSlider) e.getSource());
        hsb = new float[]{
                id == 0 ? (float) source.getValue() / HUE_STEPCOUNT : hsb[0],
                id == 1 ? (float) source.getValue() / STEPCOUNT : hsb[1],
                id == 2 ? (float) source.getValue() / STEPCOUNT : hsb[2]};
        update(ColorTools.hsbToColor(hsb), false, !source.getValueIsAdjusting());
    }

    @Override
    protected void onTextFieldChange(int id, NumberBox source) {
        hsb = new float[]{
                id == 0 ? (float) source.getValue() / HUE_STEPCOUNT : hsb[0],
                id == 1 ? (float) source.getValue() / STEPCOUNT : hsb[1],
                id == 2 ? (float) source.getValue() / STEPCOUNT : hsb[2]};
        update(ColorTools.hsbToColor(hsb), false, true);
    }

    @Override
    protected void refreshUI() {
        sSlider.setLeftColor(ColorTools.hsbToColor(new float[] {hsb[0], 0, hsb[2]}));
        sSlider.setRightColor(ColorTools.hsbToColor(new float[] {hsb[0], 1, hsb[2]}));
        sSlider.repaint();

        bSlider.setLeftColor(ColorTools.hsbToColor(new float[] {hsb[0], hsb[1], 0}));
        bSlider.setRightColor(ColorTools.hsbToColor(new float[] {hsb[0], hsb[1], 1}));
        bSlider.repaint();

        // set the values
        setValues(new int[] {
                Math.round(hsb[0] * HUE_STEPCOUNT),
                Math.round(hsb[1] * STEPCOUNT),
                Math.round(hsb[2] * STEPCOUNT)
        });
    }

    // internal variable
    private float[] hsb = new float[3];

    @Override
    protected void notifyColorChange(Color color) {
        hsb = ColorTools.colorToHSB(color);
    }
}
