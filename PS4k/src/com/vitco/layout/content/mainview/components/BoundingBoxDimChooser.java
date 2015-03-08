package com.vitco.layout.content.mainview.components;

import com.vitco.layout.content.colorchooser.components.NumberBox;
import com.vitco.layout.content.colorchooser.components.TextChangeListener;
import com.vitco.manager.lang.LangSelectorInterface;
import com.vitco.settings.VitcoSettings;

import javax.swing.*;
import java.awt.*;

/**
 * Interface to set three dimension sizes of the bounding box
 */
public abstract class BoundingBoxDimChooser extends JPanel {

    // constructor
    public BoundingBoxDimChooser(int X, int Y, int Z, LangSelectorInterface langSelector) {

        // create border and background
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        this.setBackground(VitcoSettings.DEFAULT_BG_COLOR);

        // set the layout and add header text
        this.setLayout(new BorderLayout());
        JLabel label = new JLabel(langSelector.getString("text_resize_bounding_box"));
        label.setForeground(VitcoSettings.DEFAULT_TEXT_COLOR);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setBorder(BorderFactory.createEmptyBorder(0,0,4,0));
        this.add(label, BorderLayout.NORTH);

        // generate the panel for adjusting
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(VitcoSettings.DEFAULT_BG_COLOR);

        // one number box for each dimension
        final NumberBox bX = new NumberBox(1, 128, X);
        final NumberBox bY = new NumberBox(1, 128, Y);
        final NumberBox bZ = new NumberBox(1, 128, Z);

        // listen to changes
        bX.addTextChangeListener(new TextChangeListener() {
            @Override
            public void onChange() {
                onXChange(bX.getValue());
            }
        });
        bY.addTextChangeListener(new TextChangeListener() {
            @Override
            public void onChange() {
                onYChange(bY.getValue());
            }
        });
        bZ.addTextChangeListener(new TextChangeListener() {
            @Override
            public void onChange() {
                onZChange(bZ.getValue());
            }
        });

        // add label and text fields to layout
        mainPanel.setLayout(new GridLayout(3, 2));
        mainPanel.add(new OutlineLabel("X " + langSelector.getString("text_bounding_box_size_dimension") + ": ", VitcoSettings.ANIMATION_AXIS_COLOR_X, Color.BLACK));
        mainPanel.add(bX);
        mainPanel.add(new OutlineLabel("Y " + langSelector.getString("text_bounding_box_size_dimension") + ": ", VitcoSettings.ANIMATION_AXIS_COLOR_Y, Color.BLACK));
        mainPanel.add(bY);
        mainPanel.add(new OutlineLabel("Z " + langSelector.getString("text_bounding_box_size_dimension") + ": ", VitcoSettings.ANIMATION_AXIS_COLOR_Z, Color.BLACK));
        mainPanel.add(bZ);

        // add main panel to top layout
        this.add(mainPanel, BorderLayout.CENTER);
    }

    // notified when the sizes change
    public abstract void onXChange(int newVal);
    public abstract void onYChange(int newVal);
    public abstract void onZChange(int newVal);


}
