package com.vitco.app.layout.content.colorchooser;

import com.vitco.app.layout.content.colorchooser.basic.ColorChooserPrototype;
import com.vitco.app.layout.content.colorchooser.basic.Settings;
import com.vitco.app.layout.content.colorchooser.components.HexBox;
import com.vitco.app.layout.content.colorchooser.components.TextChangeListener;
import com.vitco.app.settings.VitcoSettings;
import com.vitco.app.util.misc.ColorTools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * A color chooser that uses hex value input to determine the color.
 */
public class HexColorChooser extends ColorChooserPrototype {

    // todo recheck code

    private boolean focused = false;
    private Color color = Color.BLACK;
    private final HexBox input = new HexBox(color);

    // constructor
    public HexColorChooser() {
        input.addTextChangeListener(new TextChangeListener() {
            @Override
            public void onChange() {
                color = input.getValue();
                notifyListeners(ColorTools.colorToHSB(color));
            }
        });
        input.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                input.setValueWithoutRefresh(color);
            }

            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
            }
        });

        // add the components
        setLayout(new BorderLayout());
        JLabel label = new JLabel("#");
        add(input, BorderLayout.CENTER);
        add(label, BorderLayout.WEST);

        // style this window and the components
        setBorder(BorderFactory.createLineBorder(VitcoSettings.DEFAULT_BORDER_COLOR, 1));
        setBackground(Settings.BG_COLOR);

        Font font = input.getFont();
        font = new Font(font.getName(), Font.BOLD, font.getSize()+30);
        input.setFont(font);
        input.setBorder(BorderFactory.createEmptyBorder());
        label.setFont(font);
        label.setBackground(Settings.TEXTAREA_BG_COLOR);
        label.setForeground(Settings.TEXTAREA_TEXT_COLOR);
    }

    public final void setColor(float[] hsb) {
        color = ColorTools.hsbToColor(hsb);
        if (!focused) {
            input.setValueWithoutRefresh(color);
        }
    }
}
