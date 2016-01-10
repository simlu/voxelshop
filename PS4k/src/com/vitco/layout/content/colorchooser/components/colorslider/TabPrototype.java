package com.vitco.layout.content.colorchooser.components.colorslider;

import com.vitco.layout.content.colorchooser.basic.ColorChooserPrototype;
import com.vitco.layout.content.colorchooser.basic.Settings;
import com.vitco.layout.content.colorchooser.components.ColorSliderPrototype;
import com.vitco.layout.content.colorchooser.components.NumberBox;
import com.vitco.layout.content.colorchooser.components.TextChangeListener;
import com.vitco.layout.content.colorchooser.components.ValueChangeListener;
import com.vitco.util.misc.ColorTools;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

/**
 * tab prototype
 */
abstract class TabPrototype extends ColorChooserPrototype {
    private ColorSliderPrototype[] sliders;
    private NumberBox[] fields;
    protected Color color = Color.WHITE;

    protected abstract void onSliderChange(int id, ChangeEvent e);
    protected abstract void onTextFieldChange(int id, NumberBox source);
    protected abstract void refreshUI();
    protected abstract void notifyColorChange(Color color);

    private boolean hasChanged = false;
    public void update(Color newColor, boolean externalChange, boolean publishIfChanged) {
        boolean changed = !color.equals(newColor);
        if (changed || externalChange) {
            // set the color
            color = newColor;
            if (changed) {
                hasChanged = true;
            }
            if (externalChange) {
                notifyColorChange(newColor);
            }
            refreshUI();
        }
        // notify the listeners
        if (hasChanged && publishIfChanged) {
            hasChanged = false;
            notifyListeners(ColorTools.colorToHSB(color));
        }
    }

    public void setColor(Color color) {
        update(color, true, false);
    }

    // update displayed values
    protected final void setValues(int[] values) {
        for (int i = 0; i < values.length; i++) {
            sliders[i].setValueWithoutRefresh(values[i]);
            fields[i].setValueWithoutRefresh(values[i]);
        }
    }

    protected final void init(String[] values, ColorSliderPrototype[] sliders, final NumberBox[] fields) {
        // store internal
        this.sliders = sliders;
        this.fields = fields;

        // update color when this component is shown
        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) == HierarchyEvent.SHOWING_CHANGED) {
                    if (isShowing()) {
                        update(color, true, false);
                    }
                }
            }
        });

        // register slider events
        for (int id = 0; id < sliders.length; id++) {
            final int finalId = id;
            sliders[id].addValueChangeListener(new ValueChangeListener() {
                @Override
                public void onChange(ChangeEvent e) {
                    onSliderChange(finalId, e);
                }
            });
        }

        // register textfield events
        for (int id = 0; id < fields.length; id++) {
            final int finalId = id;
            fields[id].addTextChangeListener(new TextChangeListener() {
                @Override
                public void onChange() {
                    onTextFieldChange(finalId, fields[finalId]);
                }
            });
        }

        // construct the layout
        GridBagLayout gbl = new GridBagLayout();
        // define that only the slider streches
        gbl.columnWeights = new double[]{0.0f, 1.0f, 0.0f};
        // set outside border (left right)
        setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));

        setLayout(gbl);
        setBackground(Settings.BG_COLOR);
        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3,3,3,3);
        c.fill = GridBagConstraints.BOTH;

        // labels
        c.gridx = 0;
        c.gridy = 0;
        for (String value : values) {
            JLabel label = new JLabel(value);
            label.setForeground(Settings.TEXT_COLOR);
            add(label, c);
            c.gridy++;
        }

        // slider
        c.gridx = 1;
        c.gridy = 0;
        for (ColorSliderPrototype slider : sliders) {
            //slider.setPreferredSize(new Dimension(150, 20));
            slider.setPreferredSize(new Dimension(50, 20));
            slider.setHeight(20);
            add(slider, c);
            c.gridy++;
        }

        // text fields
        c.gridx = 2;
        c.gridy = 0;
        for (NumberBox field : fields) {
            add(field, c);
            c.gridy++;
        }
    }
}
