package com.vitco.util.colors.basics.components;

import com.vitco.res.VitcoSettings;
import com.vitco.util.colors.basics.Settings;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Text-field that only allows hex numbers and has an easy way to retrieve the current color
 *  also allows for notification listen (and change "onChange")
 */
public class HexBox extends JTextField {

    // todo recheck code

    // conversion between hex and integer
    private static String toHex(Color color) {
        String rgb = Integer.toHexString(color.getRGB());
        return rgb.substring(2, rgb.length()).toUpperCase();
    }
    private static Color fromHex(String input) {
        // expand the values correctly
        switch (input.length()) {
            case 3:
                input = String.valueOf(input.charAt(0)) + String.valueOf(input.charAt(0)) +
                        String.valueOf(input.charAt(1)) + String.valueOf(input.charAt(1)) +
                        String.valueOf(input.charAt(2)) + String.valueOf(input.charAt(2));
                break;
            default:
                input = String.format("%1$6s", input).replace(' ', '0');
                break;
        }
        return Color.decode('#' + input);
    }

    // setter that does not notify the listeners
    public void setValueWithoutRefresh(Color value) {
        if (getValue() != value) {
            blockNotify = true;
            super.setText(toHex(value));
            blockNotify = false;
        }
    }

    // the current string used
    private String currentString = "";

    // holds the listeners
    private final ArrayList<TextChangeListener> listener = new ArrayList<TextChangeListener>();

    // add a listener
    public final void addTextChangeListener(TextChangeListener tcl) {
        listener.add(tcl);
    }

    // notify listeners
    private boolean blockNotify = false;
    private void notifyListeners() {
        if (!blockNotify && getValue() != null) {
            for (TextChangeListener tcl : listener) {
                tcl.onChange();
            }
        }
    }

    // filter to allow only hex values in textarea and notify on change
    // also remembers the current string
    private class AxisJTextFilter extends DocumentFilter {
        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException
        {
            text = text.replace("#","");
            StringBuilder sb = new StringBuilder();
            sb.append(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.insert(offset, text);
            if(invalidContent(sb.toString())) return;
            fb.insertString(offset, text, attr);
            currentString = sb.toString();
            notifyListeners();
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException
        {
            text = text.replace("#","");
            StringBuilder sb = new StringBuilder();
            sb.append(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.replace(offset, offset + length, text);
            if(invalidContent(sb.toString())) return;
            fb.replace(offset, length, text, attr);
            currentString = sb.toString();
            notifyListeners();
        }

        @Override
        public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException
        {
            super.remove(fb, offset, length);
            currentString = fb.getDocument().getText(0, fb.getDocument().getLength());
            notifyListeners();
        }

        private final Pattern pattern = Pattern.compile("^[A-Fa-f0-9]{0,6}$");

        public boolean invalidContent(String text)
        {
            Matcher matcher = pattern.matcher(text);
            boolean isMatch = matcher.matches();
            return !isMatch;
        }
    }

    // link to this field (for nested reference)
    private final HexBox thisField = this;

    // get value
    public final Color getValue() {
        Color result = Color.BLACK; // default color
        try {
            result = fromHex(currentString);
        } catch (NumberFormatException ignored) {}
        return result;
    }

    public HexBox(Color current) {
        super(toHex(current), 6);
        setForeground(Settings.TEXTAREA_TEXT_COLOR);
        setBackground(Settings.TEXTAREA_BG_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Settings.TEXTAREA_BORDER_COLOR),
                BorderFactory.createEmptyBorder(0, 3, 0, 3)
        ));
        ((AbstractDocument)this.getDocument()).setDocumentFilter(new AxisJTextFilter());
        // handle highlight on focus
        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                setForeground(Settings.TEXTAREA_TEXT_COLOR_FOCUS);
                setBackground(Settings.TEXTAREA_BG_COLOR_FOCUS);
            }

            @Override
            public void focusLost(FocusEvent e) {
                thisField.setText(String.valueOf(getValue()));
                setForeground(Settings.TEXTAREA_TEXT_COLOR);
                setBackground(Settings.TEXTAREA_BG_COLOR);
            }
        });
        // handle update on return (only visual)
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                synchronized (VitcoSettings.SYNCHRONIZER) {
                    if (e.getKeyCode() == 10) {
                        thisField.setText(String.valueOf(getValue()));
                        // remove focus from this component
                        thisField.transferFocusBackward();
                    }
                }
            }
        });
    }
}
