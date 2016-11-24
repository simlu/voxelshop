package com.vitco.app.util.components.dialog.components;

import com.jidesoft.swing.JideLabel;
import com.vitco.app.layout.content.console.ConsoleInterface;
import com.vitco.app.util.components.dialog.BlankDialogModule;
import com.vitco.app.util.misc.UrlUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LinkButton extends BlankDialogModule {
    private final ConsoleInterface console;

    public LinkButton(ConsoleInterface console, String identifier, String caption, final String url) {
        super(identifier);
        this.console = console;

        JideLabel label = new JideLabel();
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setText("<HTML><span color=\"#000099\">" + caption + "</span></HTML>");
        label.setToolTipText(url);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                open(url);
            }
        });
        this.setLayout(new BorderLayout());
        this.add(label, BorderLayout.CENTER);
    }

    private void open(String url) {
        UrlUtil.openURL(this.console, url);
    }
}
