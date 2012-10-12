package com.vitco.logic.sideview;

import com.jidesoft.action.CommandMenuBar;
import com.jidesoft.swing.JideButton;
import com.threed.jpct.Config;
import com.threed.jpct.SimpleVector;
import com.vitco.engine.EngineInteractionPrototype;
import com.vitco.engine.data.container.VOXELMODE;
import com.vitco.engine.data.container.Voxel;
import com.vitco.res.VitcoSettings;
import com.vitco.util.action.ComplexActionManager;
import com.vitco.util.pref.PrefChangeListener;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

/**
 * Creates one side view instance (one perspective) and the specific user interaction.
 */
public class SideView extends EngineInteractionPrototype implements SideViewInterface {

    // var & setter
    private ComplexActionManager complexActionManager;
    @Autowired
    public final void setComplexActionManager(ComplexActionManager complexActionManager) {
        this.complexActionManager = complexActionManager;
    }

    private final int side;

    // resets the view for this side
    private void resetView() {
        switch (side) {
            case 0:
                camera.setView(VitcoSettings.SIDE_VIEW1_CAMERA_POSITION);
                break;
            case 1:
                camera.setView(VitcoSettings.SIDE_VIEW2_CAMERA_POSITION);
                break;
            case 2:
                camera.setView(VitcoSettings.SIDE_VIEW3_CAMERA_POSITION);
                break;
        }
        camera.setFOVLimits(VitcoSettings.SIDE_VIEW_ZOOM_FOV,VitcoSettings.SIDE_VIEW_ZOOM_FOV);
        camera.setFOV(VitcoSettings.SIDE_VIEW_ZOOM_FOV);
    }

    // constructor
    public SideView(Integer side) {
        super(side);
        this.side = side;
        resetView();
    }

    @Override
    protected final int[] voxelPosForHoverPos(Point point) {
        // calculate position
        SimpleVector nPos = convert2D3D((int)Math.round(point.getX()), (int)Math.round(point.getY()),
                new SimpleVector(
                        side == 2 ? currentplane : 0,
                        side == 1 ? currentplane : 0,
                        side == 0 ? currentplane : 0
                )
        );
        int[] pos = new int[]{
                side == 2 ? currentplane : Math.round(nPos.x/VitcoSettings.VOXEL_SIZE),
                side == 1 ? currentplane : Math.round(nPos.y/VitcoSettings.VOXEL_SIZE),
                side == 0 ? currentplane : Math.round(nPos.z/VitcoSettings.VOXEL_SIZE)
        };
        // nullify if we didn't find a voxel (and not in draw mode)
        if (voxelMode != VOXELMODE.DRAW) {
            if (data.searchVoxel(pos, false) == null) {
                pos = null;
            }
        }
        return pos;
    }

    // the current depth of the plane that is shown
    private int currentplane = 0;

    // get the voxels to render
    @Override
    protected Voxel[] getVoxels() {
        // get the current voxels
        Voxel[] voxels = null;
        switch (side) {
            case 0:
                voxels = data.getVoxelsXY(currentplane);
                break;
            case 1:
                voxels = data.getVoxelsXZ(currentplane);
                break;
            case 2:
                voxels = data.getVoxelsYZ(currentplane);
                break;
        }
        return voxels;
    }

    @Override
    protected Voxel[][] getChangedVoxels() {
        return data.getNewSideVoxel("side" + side, side, currentplane);
    }

    @Override
    protected Voxel[][] getChangedSelectedVoxels() {
        return data.getNewSelectedVoxel("side" + side);
    }

    // get the reference point depending on the selected layer
    @Override
    protected SimpleVector getRefPoint() {
        return new SimpleVector(
                side == 2 ? currentplane*VitcoSettings.VOXEL_SIZE : 0,
                side == 1 ? currentplane*VitcoSettings.VOXEL_SIZE : 0,
                side == 0 ? currentplane*VitcoSettings.VOXEL_SIZE : 0
        );
    }

    @Override
    public final JPanel build() {

        // set the simple view mode
        //setSimpleVoxelMode(true, side);

        // make sure we can see into the distance
        world.setClippingPlanes(Config.nearPlane,VitcoSettings.SIDE_VIEW_MAX_ZOOM*2);
        selectedVoxelsWorld.setClippingPlanes(Config.nearPlane,VitcoSettings.SIDE_VIEW_MAX_ZOOM*2);

        // set initial current plane
        preferences.storeObject("currentplane_sideview" + (side + 1), 0);
        // register change of current plane of this sideview
        preferences.addPrefChangeListener("currentplane_sideview" + (side + 1), new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                currentplane = (Integer)o;
                // invalidate this buffer (as the plane has changed)
                data.invalidateSideViewBuffer("side" + side, side, currentplane);
                container.doNotSkipNextWorldRender();
                invalidateVoxels();
                forceRepaint();
            }
        });

        // register clip buttons
        actionManager.registerAction("sideview_move_plane_in" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                preferences.storeObject("currentplane_sideview" + (side + 1), currentplane-1);
            }
        });
        actionManager.registerAction("sideview_move_plane_out" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                preferences.storeObject("currentplane_sideview" + (side + 1), currentplane+1);
            }
        });
        actionManager.registerAction("sideview_set_plane_to_zero" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                preferences.storeObject("currentplane_sideview" + (side + 1), 0);
            }
        });
        // complex action for repainting the icon with number
        complexActionManager.registerActionIsUsed("sideview_set_plane_to_zero_button" + (side + 1));
        complexActionManager.performWhenActionIsReady("sideview_set_plane_to_zero_button" + (side + 1), new Runnable() {
            @Override
            public void run() {
                preferences.addPrefChangeListener("currentplane_sideview" + (side + 1), new PrefChangeListener() {
                    private JideButton button = null;
                    private BufferedImage originalBG = null;
                    @Override
                    public void onPrefChange(Object o) {
                        if (button == null || originalBG == null) { // remember the original icon
                            button = ((JideButton) complexActionManager.getAction("sideview_set_plane_to_zero_button" + (side + 1)));
                            Icon icon = button.getIcon();
                            originalBG = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                            icon.paintIcon(null, originalBG.getGraphics(), 0, 0);
                        }

                        // print the number
                        BufferedImage image = new BufferedImage(originalBG.getWidth(), originalBG.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        image.getGraphics().drawImage(originalBG,0,0, null);

                        Graphics2D ig = (Graphics2D)image.getGraphics();
                        // Anti-alias
                        ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
                        ig.setColor(Color.GRAY);
                        ig.setFont(new Font(ig.getFont().getName(), Font.PLAIN, 9));
                        ig.drawString(String.valueOf(o), 4, 16);
                        ImageIcon icon = new ImageIcon();
                        icon.setImage(image);
                        button.setIcon(icon);
                    }
                });
            }
        });


        // register zoom buttons
        actionManager.registerAction("sideview_zoom_in_tb" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoomIn(VitcoSettings.SIDE_VIEW_COARSE_ZOOM_SPEED);
                forceRepaint();
            }
        });
        actionManager.registerAction("sideview_zoom_out_tb" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoomOut(VitcoSettings.SIDE_VIEW_COARSE_ZOOM_SPEED);
                forceRepaint();
            }
        });

        // register the reset view action
        actionManager.registerAction("sideview_reset_view" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetView();
                forceRepaint();
            }
        });

        // register zoom (mouse wheel)
        camera.setZoomLimits(VitcoSettings.SIDE_VIEW_MIN_ZOOM, VitcoSettings.SIDE_VIEW_MAX_ZOOM);
        container.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() == -1) {
                    camera.zoomIn(VitcoSettings.SIDE_VIEW_FINE_ZOOM_SPEED);
                } else {
                    camera.zoomOut(VitcoSettings.SIDE_VIEW_FINE_ZOOM_SPEED);
                }
                animationAdapter.mouseMoved(e); // keep selection refreshed (zoom ~ mouse move)
                forceRepaint();
            }
        });

        // make sure there is no plane selected
        preferences.storeObject("engine_view_voxel_preview_plane", -1);

        // register shifting and preview plane
        MouseAdapter shiftingMouseAdapter = new MouseAdapter() {
            // preview plane
            // =======================
            @Override
            public void mouseEntered(MouseEvent e) {
                preferences.storeObject("engine_view_voxel_preview_plane", side*2);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                preferences.storeObject("engine_view_voxel_preview_plane", -1);
            }

            // shifting
            // =======================
            private Point mouse_down_point = null;

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 1) {
                    mouse_down_point = e.getPoint();
                } else {
                    mouse_down_point = null;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouse_down_point = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouse_down_point != null) {
                    // keep speed the same for different container sizes
                    camera.shift(150*(float)(e.getX() - mouse_down_point.getX())/container.getWidth(),
                            150*(float)(e.getY() - mouse_down_point.getY())/container.getHeight(),
                            VitcoSettings.SIDE_VIEW_SIDE_MOVE_FACTOR);
                    mouse_down_point = e.getPoint();
                    forceRepaint();
                }
            }
        };
        container.addMouseMotionListener(shiftingMouseAdapter);
        container.addMouseListener(shiftingMouseAdapter);

        // holds the menu and the draw panel (container)
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());

        // prevent "flickering" when swapping windows
        preferences.addPrefChangeListener("engine_view_bg_color", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                wrapper.setBackground((Color) o);
            }
        });

        // create menu
        CommandMenuBar menuPanel = new CommandMenuBar();
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/logic/sideview/toolbar" + (side + 1) + ".xml");
        menuPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        // so the background doesn't show
        menuPanel.setOpaque(true);

        // add menu and container
        wrapper.add(menuPanel, BorderLayout.SOUTH);
        wrapper.add(container, BorderLayout.CENTER);

        return wrapper;
    }

    @Override
    public int getSide() {
        return side;
    }
}
