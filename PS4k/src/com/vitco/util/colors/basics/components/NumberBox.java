package com.vitco.util.colors.basics.components;

import com.vitco.res.VitcoSettings;
import com.vitco.util.colors.basics.Settings;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Text-field that only allows numbers and has an easy way to retrieve the current one
 *  also allows for notification listen (and change "onChange")
*/
public class NumberBox extends JTextField {

    // setter that does not notify the listeners
    public void setValueWithoutRefresh(int value) {
        int croppedValue = cropValue(value);
        if (getValue() != croppedValue) {
            blockNotify = true;
            super.setText(String.valueOf(croppedValue));
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
        if (!blockNotify) {
            for (TextChangeListener tcl : listener) {
                tcl.onChange();
            }
        }
    }

    // filter to allow only numbers in textarea and notify on change
    // also remembers the current string
    private class AxisJTextFilter extends DocumentFilter {
        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException
        {
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

        private final Pattern pattern = Pattern.compile("\\d{0," + (String.valueOf(MAX).length() + 1) + "}?");

        public boolean invalidContent(String text)
        {
            Matcher matcher = pattern.matcher(text);
            boolean isMatch = matcher.matches();
            return !text.equals("") && !isMatch;
        }
    }

    // link to this field (for nested reference)
    private final NumberBox thisField = this;

    // conversion helper
    private int cropValue(int value)  {
        return Math.min(MAX,Math.max(MIN,value));
    }

    // get value (range 0-255)
    public final int getValue() {
        int result = 0;
        try {
            result = cropValue(Integer.valueOf(currentString));
        } catch (NumberFormatException ignored) {}
        return result;
    }

    private final int MIN;
    private final int MAX;

    // constructor
    public NumberBox(int min, int max, int current) {
        super(String.valueOf(current), 4);
        MIN = min;
        MAX = max;
        setForeground(Settings.TEXTAREA_TEXT_COLOR);
        setOpaque(true);
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

