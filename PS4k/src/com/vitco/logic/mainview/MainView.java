package com.vitco.logic.mainview;

import com.jidesoft.action.CommandMenuBar;
import com.threed.jpct.SimpleVector;
import com.vitco.engine.EngineInteractionPrototype;
import com.vitco.engine.data.container.Voxel;
import com.vitco.res.VitcoSettings;
import com.vitco.util.WorldUtil;
import com.vitco.util.action.ChangeListener;
import com.vitco.util.action.types.StateActionPrototype;

import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Creates the main view instance and attaches the specific user interaction.
 */
public class MainView extends EngineInteractionPrototype implements MainViewInterface {

    @Override
    protected Voxel[] getVoxels() {
        return data.getVisibleLayerVoxel();
    }

    @Override
    public final JPanel build() {

        // camera settings
        camera.setFOVLimits(VitcoSettings.MAIN_VIEW_ZOOM_FOV,VitcoSettings.MAIN_VIEW_ZOOM_FOV);
        camera.setFOV(VitcoSettings.MAIN_VIEW_ZOOM_FOV);
        camera.setZoomLimits(VitcoSettings.MAIN_VIEW_ZOOM_IN_LIMIT, VitcoSettings.MAIN_VIEW_ZOOM_OUT_LIMIT);
        camera.setView(VitcoSettings.MAIN_VIEW_CAMERA_POSITION); // camera initial position

        world.setAmbientLight(0, 0, 0); // compensate a bit for the lights
        WorldUtil.addLight(world, new SimpleVector(-200, -1300, -200), 3);
        WorldUtil.addLight(world, new SimpleVector(200, 1300, 200), 3);
        WorldUtil.addLight(world, new SimpleVector(1300, 200, 200), 1);
        WorldUtil.addLight(world, new SimpleVector(-1300, -200, -200), 1);

        // add ground plane
        WorldUtil.addPlane(
                world,
                new SimpleVector(0, VitcoSettings.VOXEL_GROUND_DISTANCE, 0),
                new SimpleVector(0, 0, 0),
                VitcoSettings.VOXEL_GROUND_PLANE_SIZE,
                VitcoSettings.VOXEL_GROUND_PLANE_COLOR,
                0
        );

        // user mouse input - change camera position
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) { // scroll = zoom in and out
                if (e.getWheelRotation() == -1) {
                    camera.zoomIn(VitcoSettings.MAIN_VIEW_ZOOM_SPEED_SLOW);
                } else {
                    camera.zoomOut(VitcoSettings.MAIN_VIEW_ZOOM_SPEED_SLOW);
                }
                container.repaint();
            }

            private Point leftMouseDown = null;
            private Point rightMouseDown = null;

            @Override
            public void mousePressed(MouseEvent e) {
                switch (e.getButton()) {
                    case 1: leftMouseDown = e.getPoint(); break;
                    case 3: rightMouseDown = e.getPoint(); break;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                switch (e.getButton()) {
                    case 1: leftMouseDown = null; break;
                    case 3: rightMouseDown = null; break;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (leftMouseDown != null) {
                    camera.rotate(e.getX() - leftMouseDown.x, e.getY() - leftMouseDown.y);
                    leftMouseDown.x = e.getX();
                    leftMouseDown.y = e.getY();
                    container.repaint();
                } else if (rightMouseDown != null) {
                    camera.shift(e.getX() - rightMouseDown.x, e.getY() - rightMouseDown.y, VitcoSettings.MAIN_VIEW_SIDE_MOVE_FACTOR);
                    rightMouseDown.x = e.getX();
                    rightMouseDown.y = e.getY();
                    container.repaint();
                }
            }
        };
        container.addMouseWheelListener(mouseAdapter);
        container.addMouseMotionListener(mouseAdapter);
        container.addMouseListener(mouseAdapter);

        // register zoom buttons
        actionManager.registerAction("mainview_zoom_in", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoomIn(VitcoSettings.MAIN_VIEW_ZOOM_SPEED_FAST);
                container.repaint();
            }
        });
        actionManager.registerAction("mainview_zoom_out", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoomOut(VitcoSettings.MAIN_VIEW_ZOOM_SPEED_FAST);
                container.repaint();
            }
        });

        // register reset action
        actionManager.registerAction("reset_main_view_camera", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.setView(VitcoSettings.MAIN_VIEW_CAMERA_POSITION);
                container.repaint();
            }
        });

        // holds menu and render area (container)
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());

        // create menu
        CommandMenuBar menuPanel = new CommandMenuBar();
        menuPanel.setOrientation(1); // top down orientation
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/logic/mainview/toolbar.xml");
        menuPanel.setBorder(BorderFactory.createMatteBorder(1,0,1,1,VitcoSettings.DEFAULT_BORDER_COLOR));

        // add to wrapper
        wrapper.add(menuPanel, BorderLayout.EAST);
        wrapper.add(container, BorderLayout.CENTER);

        return wrapper;
    }

}
