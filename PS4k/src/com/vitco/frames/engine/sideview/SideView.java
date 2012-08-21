package com.vitco.frames.engine.sideview;

import com.jidesoft.action.CommandMenuBar;
import com.vitco.frames.ViewPrototype;
import com.vitco.frames.engine.data.animationdata.AnimationDataInterface;
import com.vitco.res.VitcoSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Can mainview an AnimationData and provides means to alter the data.
 */
public class SideView extends ViewPrototype implements SideViewInterface {

    private AnimationDataInterface animationData;
    public void setAnimationData(AnimationDataInterface animationData) {
        this.animationData = animationData;
    }

    // internal - help creating a view
    public JPanel createView(final int side) {
        // holds the menu and the draw panel
        final JPanel viewPanel = new JPanel();
        viewPanel.setLayout(new BorderLayout());

        // create menu
        final CommandMenuBar menuPanel = new CommandMenuBar() {};
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/frames/engine/sideview/toolbar" + (side + 1) + ".xml");
        menuPanel.setBorder(
                // the last menu gets a bottom border
                BorderFactory.createMatteBorder(0, 1, side == 2 ? 1 : 0, 1, VitcoSettings.ANIMATION_BORDER_COLOR)
        );

        // create the VPanel
        final VPanel panel = new VPanel(animationData, actionManager, langSelector, side, viewPanel, menuPanel);

        // add to this view
        viewPanel.add(menuPanel, BorderLayout.SOUTH);
        viewPanel.add(panel, BorderLayout.CENTER);

        return viewPanel;
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
        northPane.add(createView(0));
        northPane.add(createView(1));
        northPane.add(createView(2));

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
