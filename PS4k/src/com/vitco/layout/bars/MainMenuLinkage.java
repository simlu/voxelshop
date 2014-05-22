package com.vitco.layout.bars;

import com.jidesoft.action.CommandMenuBar;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * the main menu, uses menu generator to load content from file
 *
 * defines interactions
 */
public class MainMenuLinkage extends BarLinkagePrototype {

    private final Border resizableBorder = BorderFactory.createLineBorder(Color.BLACK, 3);

    @Override
    public CommandMenuBar buildBar(String key, final Frame frame) {
        final CommandMenuBar bar = new CommandMenuBar(key);

        // build the menu
        menuGenerator.buildMenuFromXML(bar, "com/vitco/layout/bars/main_menu.xml");

        // make borderless
        frame.setUndecorated(true);

        // make menu bar draggable
        new MoveMouseAdapter(bar, frame);

        // listen to border events
        //((JFrame)frame).addMouse

        // add listener to react to MAXIMIZED changes
        frame.addComponentListener(
                new ComponentAdapter() {
                    public void componentResized(ComponentEvent e) {
                        if (frame.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                            ((JFrame)frame).getRootPane().setBorder(BorderFactory.createEmptyBorder());
                        } else {
                            ((JFrame)frame).getRootPane().setBorder(resizableBorder);
                        }
                    }
                }
        );

        // register the logic for this menu
        menuLogic.registerLogic(frame);

        return bar;
    }

    // helper class to drag frame
    private final class MoveMouseAdapter extends MouseAdapter {
        // the frame that is dragged
        private final Frame frame;
        // the component that is used for dragging
        private JComponent target;
        // locations
        private Point start_drag;
        private Point start_loc;

        // constructor
        public MoveMouseAdapter(JComponent target, Frame frame) {
            this.target = target;
            this.frame = frame;
            // add listener
            target.addMouseListener(this);
            target.addMouseMotionListener(this);
        }

        // --------

        // get current location on screen
        private Point getScreenLocation(MouseEvent e) {
            Point cursor = e.getPoint();
            Point target_location = this.target.getLocationOnScreen();
            return new Point((int) (target_location.getX() + cursor.getX()),
                    (int) (target_location.getY() + cursor.getY()));
        }

        // initialize locations
        public void mousePressed(MouseEvent e) {
            this.start_drag = this.getScreenLocation(e);
            this.start_loc = frame.getLocation();
            // change maximized state on dbl click
            if (e.getClickCount()%2 == 0) {
                if ((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                    frame.setExtendedState(JFrame.NORMAL);
                } else {
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
            }
        }

        // update frame
        public void mouseDragged(MouseEvent e) {
            // no longer maximised
            if ((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            // update position
            Point current = this.getScreenLocation(e);
            Point offset = new Point(
                    current.x - start_drag.x,
                    current.y - start_drag.y
            );
            Point new_location = new Point(
                    this.start_loc.x + offset.x,
                    this.start_loc.y + offset.y
            );
            frame.setLocation(new_location);
        }
    }

}
