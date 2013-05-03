package com.vitco.logic.console;

import com.jidesoft.action.CommandMenuBar;
import com.vitco.layout.frames.FrameLinkagePrototype;
import com.vitco.logic.ViewPrototype;
import com.vitco.res.VitcoSettings;
import com.vitco.util.action.types.StateActionPrototype;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Displays console content and buttons to user.
 */
public class ConsoleView extends ViewPrototype implements ConsoleViewInterface {

    @Override
    public JComponent buildConsole(final FrameLinkagePrototype frame) {
        // panel that holds everything
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        // the console
        final JTextArea textArea = new JTextArea();
        // set layout
        textArea.setForeground(VitcoSettings.DEFAULT_TEXT_COLOR);
        textArea.setBackground(VitcoSettings.DEFAULT_BG_COLOR);
        // load the previous console data
        ArrayList<String> consoleData = console.getConsoleData();
        for (String line : consoleData) {
          textArea.append(line);
        }
        textArea.setEditable(false); // hide the caret
        // to be able to handle auto show, auto scroll and
        // tmp disable scroll for this textarea
        class ScrollPane extends JScrollPane {
            public boolean autoScroll = true; // true iff auto scrolling is enabled
            public boolean autoShow = true; // true iff auto showing is enabled
            public boolean tempScrollStop = false; // true iff user scrolls (disable auto scroll!)
            public ScrollPane(JComponent component) {
                super(component);
            }
        }
        final ScrollPane scrollPane = new ScrollPane(textArea);
        // load the stored preferences for this
        if (preferences.contains("console_auto_scroll_status")) {
            scrollPane.autoScroll = preferences.loadBoolean("console_auto_scroll_status");
        }
        if (preferences.contains("console_auto_show_status")) {
            scrollPane.autoShow = preferences.loadBoolean("console_auto_show_status");
        }
        // only scroll when the user is not scrolling
        scrollPane.getVerticalScrollBar().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                scrollPane.tempScrollStop = true;
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                scrollPane.tempScrollStop = false;
            }
        });
        // handle console events
        console.addConsoleListener(new ConsoleListener() {
            @Override
            public void lineAdded(String line) {
                // show if necessary
                if (scrollPane.autoShow && (!frame.isVisible() || // not visible
                        (frame.isAutohide() && !frame.isAutohideShowing()))) { // visible but not showing (side)
                    frame.setVisible(true); // show the console whenever text is added
                    panel.repaint();
                }
                // add line
                textArea.append(line);
                // make sure there are not too many lines in the textarea
                while (textArea.getLineCount() - 1 > Console.LINE_BUFFER_COUNT) {
                    try {
                        // remove first line
                        Element root = textArea.getDocument().getDefaultRootElement();
                        Element first = root.getElement(0);
                        textArea.getDocument().remove(first.getStartOffset(), first.getEndOffset());
                    } catch (BadLocationException e) {
                        errorHandler.handle(e);
                    }
                }
                // scroll to bottom if wanted
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (!scrollPane.tempScrollStop && scrollPane.autoScroll) {
                            // when the text changes, scroll down
                            scrollPane.validate();
                            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                        }
                    }
                });
            }
        });

        // the console itself is in the middle
        panel.add(scrollPane, BorderLayout.CENTER);
        // create menu bar to the left
        CommandMenuBar menuPanel = new CommandMenuBar();
        menuPanel.setOrientation(1); // top down orientation
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/logic/console/toolbar.xml");
        panel.add(menuPanel, BorderLayout.WEST);

        // register toggle actions (auto show / auto scroll)
        StateActionPrototype toggleAutoShow = new StateActionPrototype() {
            @Override
            public boolean getStatus() {
                return scrollPane.autoShow;
            }

            @Override
            public void action(ActionEvent e) {
                scrollPane.autoShow = !scrollPane.autoShow;
                preferences.storeBoolean("console_auto_show_status", scrollPane.autoShow); // store in pref
                console.addLine(
                        "Console Auto Show is " + (scrollPane.autoShow ? "enabled." : "disabled.")
                );
            }
        };
        StateActionPrototype toggleAutoScroll = new StateActionPrototype() {
            @Override
            public boolean getStatus() {
                return scrollPane.autoScroll;
            }

            @Override
            public void action(ActionEvent e) {
                scrollPane.autoScroll = !scrollPane.autoScroll;
                preferences.storeBoolean("console_auto_scroll_status", scrollPane.autoScroll); // store in pref
                console.addLine(
                        "Console Auto Scroll is " + (scrollPane.autoScroll ? "enabled." : "disabled.")
                );
            }
        };
        actionManager.registerAction("console_toggle_auto_show", toggleAutoShow);
        actionManager.registerAction("console_toggle_auto_scroll", toggleAutoScroll);

        // register clear action
        AbstractAction clearConsole = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                console.clear(); // the console
                textArea.setText(""); // what we display
            }
        };
        actionManager.registerAction("console_action_clear", clearConsole);

        return panel;
    }

}
