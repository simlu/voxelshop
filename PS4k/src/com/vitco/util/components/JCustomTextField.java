package com.vitco.util.components;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.ArrayList;

/**
 * A text field that allows for attaching a listener.
 *
 * By default it is restricted to the characters a-z, A-Z, 0-9, "-", "_", and "."
 */
public class JCustomTextField extends JTextField {

    // list of valid characters
    private final int[] validChars;

    // constructor with default valid chars
    public JCustomTextField() {
        this(new int[] {
                // a-z
                97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107,
                108, 109, 110, 111, 112, 113, 114, 115, 116, 117,
                118, 119, 120, 121, 122,
                // A-Z
                65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77,
                78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90,
                // 0-9
                48, 49, 50, 51, 52, 53, 54, 55, 56, 57,
                // "-", "_", "."
                45, 95, 46
        });
    }

    // constructor (image and the action)
    public JCustomTextField(final int[] validChars) {
        super();
        this.validChars = validChars.clone();
        // set the filter for this textarea
        ((AbstractDocument)this.getDocument()).setDocumentFilter(new JCustomTextField.TextFilter());
    }

    // ==================
    // listener events
    // ==================

    // the current string used
    private String currentString = "";

    // retrieve the current string
    // Note: This needs to be overwritten to avoid modification exception
    @Override
    public final String getText() {
        return currentString;
    }

    // --------------

    // holds the listeners
    private final ArrayList<TextChangeListener> listener = new ArrayList<TextChangeListener>();

    // add a listener
    public final void addTextChangeListener(TextChangeListener tcl) {
        listener.add(tcl);
    }

    // remove a listener
    public final void removeTextChangeListener(TextChangeListener tcl) {
        listener.remove(tcl);
    }

    // notify listeners
    private void notifyListeners(String newCurrentString) {
        if (!currentString.equals(newCurrentString)) {
            currentString = newCurrentString;
            for (TextChangeListener tcl : listener) {
                tcl.onChange();
            }
        }
    }

    // -----------

    // filter to allow only hex values in textarea and notify on change
    // also remembers the current string
    private class TextFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
            StringBuilder sb = new StringBuilder();
            sb.append(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.insert(offset, text);
            if(invalidContent(sb.toString())) return;
            fb.insertString(offset, text, attr);
            notifyListeners(sb.toString());
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException {
            StringBuilder sb = new StringBuilder();
            sb.append(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.replace(offset, offset + length, text);
            if(invalidContent(sb.toString())) return;
            fb.replace(offset, length, text, attr);
            notifyListeners(sb.toString());
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            super.remove(fb, offset, length);
            notifyListeners(fb.getDocument().getText(0, fb.getDocument().getLength()));
        }

        private boolean invalidContent(String text) {
            for (int i = 0, len = text.length(); i < len; i++) {
                char aChar = text.charAt(i);
                int intVal = (int)aChar;
                boolean contained = false;
                for (int v : validChars) {
                    if (v == intVal) {
                        contained = true;
                        break;
                    }
                }
                if (!contained) {
                    return true;
                }
            }
            // all characters are contained
            return false;
        }
    }

}
