package com.vitco.frames.engine.mainview;

import com.jidesoft.action.CommandMenuBar;
import com.threed.jpct.*;
import com.vitco.frames.engine.*;
import com.vitco.frames.engine.data.listener.DataChangeListener;
import com.vitco.res.VitcoSettings;
import com.vitco.util.action.types.StateActionPrototype;

import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 8/20/12
 * Time: 12:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class MainView extends EngineInteractionPrototype implements MainViewInterface {

    @Override
    public JPanel build() {

        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());

        CommandMenuBar menuPanel = new CommandMenuBar();
        menuPanel.setOrientation(1); // top down orientation
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/frames/engine/mainview/toolbar.xml");
        wrapper.add(menuPanel, BorderLayout.EAST);
        wrapper.add(container, BorderLayout.CENTER);
        wrapper.setBorder(BorderFactory.createLineBorder(VitcoSettings.DEFAULT_BORDER_COLOR));
        menuPanel.setBorder(BorderFactory.createMatteBorder(0,1,0,0,VitcoSettings.DEFAULT_BORDER_COLOR));

        container.addMouseMotionListener(animationAdapter);
        container.addMouseListener(animationAdapter);

        // enable snap
        animationAdapter.setVoxelSnap(true);

        // camera settings
        camera.setZoomLimits(VitcoSettings.MAIN_VIEW_ZOOM_IN_LIMIT, VitcoSettings.MAIN_VIEW_ZOOM_OUT_LIMIT);
        camera.setView(VitcoSettings.MAIN_VIEW_CAMERA_POSITION); // camera initial position

        // lighting
        world.setAmbientLight(50, 50, 50);
        WorldUtil.addLight(world, new SimpleVector(-200, -1300, -200), 3);
        WorldUtil.addLight(world, new SimpleVector(200, 1300, 200), 3);
        WorldUtil.addLight(world, new SimpleVector(1300, 200, 200), 1);
        WorldUtil.addLight(world, new SimpleVector(-1300, -200, -200), 1);

        // for testing
        Random rand = new Random();
        for (int i = -1; i < 2; i+=2) {
            for (int j = -1; j < 2; j+=2) {
                for (int k = -1; k < 2; k+=2) {
                    Color col = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
                    WorldUtil.addBox(world, new SimpleVector(i * 50, k*50, j * 50), 10f, col);
                }
            }
        }

//        // for testing
//        //Random rand = new Random();
//        for (int i = -30; i <= 30; i+=1) {
//            for (int j = -30; j <= 30; j+=1) {
//                for (int k = -30; k <= 30; k+=1) {
//                    if (rand.nextInt(5000) == 0) {
//                        Color col = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
//                        WorldUtil.addBox(world, new SimpleVector(i * 1, k * 1 , j * 1), 0.5f, col);
//                    }
//                }
//            }
//        }

        // add the world plane (ground)
        Object3D plane = Primitives.getPlane(1, 200f);
        plane.setCulling(false); //show from both sides
        plane.setTransparency(0);
        plane.setAdditionalColor(VitcoSettings.MAIN_VIEW_GROUND_PLANE_COLOR);
        plane.setOrigin(new SimpleVector(0,100f,0));
        plane.rotateX((float)Math.PI/2);
        world.addObject(plane);

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

        // register voxel "snap" for bone joints
        if (preferences.contains("mainview_voxel_snap_enabled")) { // load previous settings
            animationAdapter.setVoxelSnap(preferences.loadBoolean("mainview_voxel_snap_enabled"));
        }
        actionManager.registerAction("mainview_toggle_voxel_snap", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                animationAdapter.setVoxelSnap(!animationAdapter.getVoxelSnap());
            }

            @Override
            public boolean getStatus() {
                return animationAdapter.getVoxelSnap();  //To change body of implemented methods use File | Settings | File Templates.
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

        // register redraw on animation data change
        animationData.addDataChangeListener(new DataChangeListener() {
            @Override
            public void onAnimationDataChanged() {
                container.skipNextWorldRender(); // no need to re-render scene
                animationAdapter.refresh2DIndex(); // refresh 2D index when data changes
                container.repaint();
            }

            @Override
            public void onAnimationSelectionChanged() {
                container.skipNextWorldRender(); // no need to re-render scene
                container.repaint();
            }

            @Override
            public void onFrameDataChanged() {
                //...
            }

            @Override
            public void onVoxelDataChanged() {
                //...
            }
        });

        // refresh the container
        container.repaint();

        return wrapper;
    }

    @PreDestroy
    public void finish() {
        preferences.storeBoolean("mainview_voxel_snap_enabled", animationAdapter.getVoxelSnap());
    }

}
