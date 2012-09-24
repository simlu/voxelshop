package com.vitco.logic.sideview;

import com.vitco.logic.ViewPrototype;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Helper to sets up and arranges the different Side Views in the Side View Panel
 */
public class SideViews extends ViewPrototype implements SideViewsInterface {

    private SideViewInterface[] sideViews;
    public final void setSideViews(SideViewInterface[] sideViews) {
        this.sideViews = sideViews.clone();
    }

    @Override
    public JPanel buildSides(final JPanel topPane) {

        topPane.setLayout(new BorderLayout());

        // makes this stick to the left side
        final JPanel westPane = new JPanel();
        westPane.setLayout(new BorderLayout());

        // makes this stick to the top side
        final JPanel northPane = new JPanel();
        northPane.setLayout(new BoxLayout(northPane, BoxLayout.PAGE_AXIS));

        // create three different side views
        northPane.add(sideViews[0].build());
        northPane.add(sideViews[1].build());
        northPane.add(sideViews[2].build());

        // resize the container if the top container changes size
        topPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int size = Math.min(e.getComponent().getWidth(), e.getComponent().getHeight() / 3);
                westPane.setPreferredSize(new Dimension(size, size * 3)); // for width
                northPane.setPreferredSize(new Dimension(size, size * 3)); // for height
                topPane.invalidate(); // force a refresh
            }
        });

        // add container
        westPane.add(northPane, BorderLayout.NORTH);
        topPane.add(westPane, BorderLayout.WEST);

        return topPane;
    }

}
