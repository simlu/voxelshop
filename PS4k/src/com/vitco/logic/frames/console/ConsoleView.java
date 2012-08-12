package com.vitco.logic.frames.console;

import javax.swing.*;

/**
 * Displays console content and buttons to user.
 */
public class ConsoleView implements ConsoleViewInterface {
    // var & setter
    private ConsoleInterface console;
    @Override
    public void setConsole(ConsoleInterface console) {
        this.console = console;
    }

    @Override
    public void addLine(String text) {
        console.addLine(text);
    }

    @Override
    public JComponent buildConsole() {
        final JTextArea textArea = new JTextArea();
        //textArea.setFocusable(false);
        //textArea.setHighlighter(null); // do not show selection
        textArea.setEditable(false); // hide the caret

        console.addConsoleListener(new ConsoleListener() {
            @Override
            public void lineAdded(String line) {
                textArea.insert(line, 0);
            }
        });

        return textArea;
    }

}
