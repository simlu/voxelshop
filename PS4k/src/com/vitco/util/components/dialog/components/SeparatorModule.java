package com.vitco.util.components.dialog.components;

import com.vitco.util.components.dialog.BlankDialogModule;
import com.vitco.util.misc.FontUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SeparatorModule extends BlankDialogModule {

    public SeparatorModule(String text) {
        super("hr");
        this.setLayout(new BorderLayout());

        JLabel jlabel = new JLabel(text);
        jlabel.setFont(FontUtil.getTitleFont(jlabel.getFont()));
        jlabel.setOpaque(true);

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        panel.setLayout(new BorderLayout());
        panel.add(jlabel, BorderLayout.WEST);

        this.add(panel, BorderLayout.CENTER);
    }

    // used to draw the border "manually"
    private final TitledBorder titledBorder = BorderFactory.createTitledBorder("");

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        titledBorder.paintBorder(this, g, -100, 7, getWidth() + 200, 300);
    }
}
