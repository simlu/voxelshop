package com.vitco.layout.content.colorchooser.components.colorslider;

import com.vitco.layout.content.colorchooser.components.ColorSliderPrototype;
import com.vitco.layout.content.colorchooser.components.NumberBox;
import com.vitco.util.misc.ColorTools;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

/**
 * cmyk tab
 */
public class CMYKTab extends TabPrototype {

    private final static int STEPCOUNT = 100;

    // the sliders
    private final HorizontalColorSlider cSlider = new HorizontalColorSlider(0, STEPCOUNT, 0);
    private final HorizontalColorSlider mSlider = new HorizontalColorSlider(0, STEPCOUNT, 0);
    private final HorizontalColorSlider ySlider = new HorizontalColorSlider(0, STEPCOUNT, 0);
    private final HorizontalColorSlider kSlider = new HorizontalColorSlider(0, STEPCOUNT, 0);

    public CMYKTab() {
        NumberBox cBox = new NumberBox(0, STEPCOUNT, 0);
        NumberBox mBox = new NumberBox(0, STEPCOUNT, 0);
        NumberBox yBox = new NumberBox(0, STEPCOUNT, 0);
        NumberBox kBox = new NumberBox(0, STEPCOUNT, 0);
        init(
                new String[]{"C", "M", "Y", "K"},
                new ColorSliderPrototype[]{cSlider, mSlider, ySlider, kSlider},
                new NumberBox[]{cBox, mBox, yBox, kBox}
        );
    }

    @Override
    protected void onSliderChange(int id, ChangeEvent e) {
        JSlider source = ((JSlider) e.getSource());
        cmyk = new float[]{
                id == 0 ? (float) source.getValue() / STEPCOUNT : cmyk[0],
                id == 1 ? (float) source.getValue() / STEPCOUNT : cmyk[1],
                id == 2 ? (float) source.getValue() / STEPCOUNT : cmyk[2],
                id == 3 ? (float) source.getValue() / STEPCOUNT : cmyk[3]};
        update(ColorTools.cmykToColor(cmyk), false, !source.getValueIsAdjusting());
    }

    @Override
    protected void onTextFieldChange(int id, NumberBox source) {
        cmyk = new float[]{
                id == 0 ? (float) source.getValue() / STEPCOUNT : cmyk[0],
                id == 1 ? (float) source.getValue() / STEPCOUNT : cmyk[1],
                id == 2 ? (float) source.getValue() / STEPCOUNT : cmyk[2],
                id == 3 ? (float) source.getValue() / STEPCOUNT : cmyk[3]};
        update(ColorTools.cmykToColor(cmyk), false, true);
    }

    @Override
    protected void refreshUI() {
        cSlider.setLeftColor(ColorTools.cmykToColor(new float[]{0, cmyk[1], cmyk[2], cmyk[3]}));
        cSlider.setRightColor(ColorTools.cmykToColor(new float[]{1, cmyk[1], cmyk[2], cmyk[3]}));
        cSlider.repaint();

        mSlider.setLeftColor(ColorTools.cmykToColor(new float[]{cmyk[0], 0, cmyk[2], cmyk[3]}));
        mSlider.setRightColor(ColorTools.cmykToColor(new float[]{cmyk[0], 1, cmyk[2], cmyk[3]}));
        mSlider.repaint();

        ySlider.setLeftColor(ColorTools.cmykToColor(new float[]{cmyk[0], cmyk[1], 0, cmyk[3]}));
        ySlider.setRightColor(ColorTools.cmykToColor(new float[]{cmyk[0], cmyk[1], 1, cmyk[3]}));
        ySlider.repaint();

        kSlider.setLeftColor(ColorTools.cmykToColor(new float[]{cmyk[0], cmyk[1], cmyk[2], 0}));
        kSlider.setRightColor(ColorTools.cmykToColor(new float[]{cmyk[0], cmyk[1], cmyk[2], 1}));
        kSlider.repaint();

        // set the values
        setValues(new int[] {
                Math.round(cmyk[0] * STEPCOUNT),
                Math.round(cmyk[1] * STEPCOUNT),
                Math.round(cmyk[2] * STEPCOUNT),
                Math.round(cmyk[3] * STEPCOUNT)
        });
    }

    // internal variable
    private float[] cmyk = new float[4];

    @Override
    protected void notifyColorChange(Color color) {
        cmyk = ColorTools.colorToCMYK(color);
    }
}
