package com.vitco.app.layout.content.layer;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Text-field that only allows hex numbers and has an easy way to retrieve the current color
 *  also allows for notification listen (and change "onChange")
 */
public class SaveTextArea extends JTextArea {

    // filter to allow only certain chars in textarea and notify on change
    // also remembers the current string
    private static class AxisJTextFilter extends DocumentFilter {
        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException
        {
            StringBuilder sb = new StringBuilder();
            sb.append(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.insert(offset, text);
            if(invalidContent(sb.toString())) return;
            fb.insertString(offset, text, attr);
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException
        {
            StringBuilder sb = new StringBuilder();
            sb.append(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.replace(offset, offset + length, text);
            if(invalidContent(sb.toString())) return;
            fb.replace(offset, length, text, attr);
        }

        @Override
        public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException
        {
            super.remove(fb, offset, length);
        }

        private final Pattern pattern = Pattern.compile("^[A-Za-z0-9 \\Q!~@#$%^&*()-_=+[]{}\\|;:'\",.<>/?\\E]{0,30}$");

        public boolean invalidContent(String text)
        {
            Matcher matcher = pattern.matcher(text);
            boolean isMatch = matcher.matches();
            return !isMatch;
        }
    }

    public SaveTextArea(String value) {
        super(value);
        ((AbstractDocument)this.getDocument()).setDocumentFilter(new AxisJTextFilter());
    }
}

