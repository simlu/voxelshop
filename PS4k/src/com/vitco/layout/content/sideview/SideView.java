package com.vitco.layout.content.sideview;

import com.jidesoft.action.CommandMenuBar;
import com.jidesoft.swing.JideButton;
import com.threed.jpct.Config;
import com.threed.jpct.SimpleVector;
import com.vitco.core.EngineInteractionPrototype;
import com.vitco.core.data.container.Voxel;
import com.vitco.low.hull.HullManager;
import com.vitco.manager.async.AsyncAction;
import com.vitco.manager.pref.PrefChangeListener;
import com.vitco.settings.VitcoSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Creates one side view instance (one perspective) and the specific user interaction.
 */
public class SideView extends EngineInteractionPrototype implements SideViewInterface {

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
            default: break;
        }
        camera.setFOVLimits(VitcoSettings.SIDE_VIEW_ZOOM_FOV, VitcoSettings.SIDE_VIEW_ZOOM_FOV);
        camera.setFOV(VitcoSettings.SIDE_VIEW_ZOOM_FOV);
    }

    // constructor
    public SideView(Integer side) {
        super(side);
        this.side = side;
        resetView();
    }

    // the current depth of the plane that is shown
    private int currentplane = -1;
    private int prevcurrentplane = -1; // the previous current plane

    // --------------
    // define ghost overlay that we draw

    private boolean voxelOutlineOutdated = false;
    SimpleVector[][] voxelOutlineData = new SimpleVector[0][];
    private HullManager<Voxel> voxelOutlineManager = new HullManager<Voxel>();

    @Override
    protected SimpleVector[][] getGhostOverlay() {
        if (voxelOutlineOutdated) {
            voxelOutlineOutdated = false;
            voxelOutlineData = voxelOutlineManager.getOutline(side);
        }
        return voxelOutlineData;
    }

    @Override
    protected boolean updateGhostOverlay() {
        boolean result = false;

        Voxel[][] changedVoxel = data.getNewSideVoxel("side" + side, side, prevcurrentplane);

        if (changedVoxel[0] == null) {
            voxelOutlineManager.clear();
            voxelOutlineOutdated = true;
            result = true;
        } else {
            // remove voxels
            for (Voxel remove : changedVoxel[0]) {
                voxelOutlineManager.clearPosition(remove.posId);
            }
            // update has changed
            if (changedVoxel[0].length > 0) {
                voxelOutlineOutdated = true;
                result = true;
            }
        }

        // add new voxels
        for (Voxel add : changedVoxel[1]) {
            voxelOutlineManager.update(add.posId, add);
        }

        // update has changed
        if (changedVoxel[1].length > 0) {
            voxelOutlineOutdated = true;
            result = true;
        }

        return result;
    }
    // --------------

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
            default: break;
        }
        return voxels;
    }

    @Override
    protected Voxel[][] getChangedVoxels() {
        return data.getNewSideVoxel("side" + side, side, currentplane);
    }

    // index to keep it to "one voxel per position"
    private final HashMap<String, Voxel> selectedAtPos = new HashMap<String, Voxel>();
    private final HashMap<String, Integer> selectedCountAtPos = new HashMap<String, Integer>();
    // list of known voxel
    // Note: Not really necessary, but might save some trouble in the future
    // when selection and deselection of a voxel occurs withing the same frame
    private final HashSet<String> liveVoxel = new HashSet<String>();

    @Override
    protected Voxel[][] getChangedSelectedVoxels() {
        // logic to keep it to "one voxel per position" (e.g. there is no need
        // to add many voxel "in depth" since the view can't rotate)
        Voxel[][] changed = data.getNewSelectedVoxel("side" + side);
        ArrayList<Voxel> toRemove = new ArrayList<Voxel>();
        ArrayList<Voxel> toAdd = new ArrayList<Voxel>();
        if (changed[0] == null) { // rebuild
            selectedAtPos.clear();
            selectedCountAtPos.clear();
            liveVoxel.clear();
            toRemove = null;
        } else {
            // remove individual voxel
            for (Voxel remove : changed[0]) {
                String strId = null;
                switch (side) {
                    case 0:
                        strId = remove.x + "_" + remove.y;
                        break;
                    case 1:
                        strId = remove.x + "_" + remove.z;
                        break;
                    case 2:
                        strId = remove.y + "_" + remove.z;
                        break;
                    default: break;
                }
                Integer count = selectedCountAtPos.get(strId);
                if (count != null) {
                    count -= 1;
                    if (liveVoxel.remove(remove.getPosAsString())) {
                        if (count == 0) {
                            selectedCountAtPos.remove(strId);
                            toRemove.add(selectedAtPos.remove(strId));
                        } else {
                            selectedCountAtPos.put(strId, count);
                        }
                    }
                }
            }
        }
        for (Voxel added : changed[1]) {
            String strId = null;
            int[] pos = new int[3];
            switch (side) {
                case 0:
                    strId = added.x + "_" + added.y;
                    pos[0] = added.x;
                    pos[1] = added.y;
                    break;
                case 1:
                    strId = added.x + "_" + added.z;
                    pos[0] = added.x;
                    pos[2] = added.z;
                    break;
                case 2:
                    strId = added.y + "_" + added.z;
                    pos[1] = added.y;
                    pos[2] = added.z;
                    break;
                default: break;
            }
            Integer count = selectedCountAtPos.get(strId);
            if (liveVoxel.add(added.getPosAsString())) {
                if (count == null) {
                    selectedCountAtPos.put(strId, 1);
                    Voxel voxel = new Voxel(-1, pos, added.getColor(), false, null, 0);
                    selectedAtPos.put(strId, voxel);
                    toAdd.add(voxel);
                } else {
                    selectedCountAtPos.put(strId, count+1);
                }
            }
        }
        Voxel[][] result = new Voxel[][]{
                toRemove == null ? null : new Voxel[toRemove.size()],
                new Voxel[toAdd.size()]
        };
        if (toRemove != null) {
            toRemove.toArray(result[0]);
        }
        toAdd.toArray(result[1]);
        return result;
    }

    @Override
    public final JPanel build() {

        // draw the ghost voxels (outline)
        container.setDrawGhostOverlay(true);

        // make sure we can see into the distance
        world.setClippingPlanes(Config.nearPlane,VitcoSettings.SIDE_VIEW_MAX_ZOOM*2);
        selectedVoxelsWorld.setClippingPlanes(Config.nearPlane,VitcoSettings.SIDE_VIEW_MAX_ZOOM*2);

        // set initial current plane
        preferences.storeObject("currentplane_sideview" + (side + 1), 0);
        // register change of current plane of this sideview
        preferences.addPrefChangeListener("currentplane_sideview" + (side + 1), new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                prevcurrentplane = currentplane;
                currentplane = (Integer)o;
                // update the container information
                container.setPlane(currentplane);
                // invalidate this buffers (as the plane has changed)
                data.invalidateSideViewBuffer("side" + side, side, currentplane);
                data.invalidateSideViewBuffer("side" + side, side, prevcurrentplane);
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
                voxelAdapter.replayHover();
            }
        });
        actionManager.registerAction("sideview_move_plane_out" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                preferences.storeObject("currentplane_sideview" + (side + 1), currentplane+1);
                voxelAdapter.replayHover();
            }
        });
        actionManager.registerAction("sideview_set_plane_to_zero" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                preferences.storeObject("currentplane_sideview" + (side + 1), 0);
                voxelAdapter.replayHover();
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
                        ig.dispose();
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
            public void mouseWheelMoved(final MouseWheelEvent e) {
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        int rotation = e.getWheelRotation();
                        if (rotation < 0) {
                            camera.zoomIn(Math.abs(rotation) * VitcoSettings.SIDE_VIEW_FINE_ZOOM_SPEED);
                        } else {
                            camera.zoomOut(rotation * VitcoSettings.SIDE_VIEW_FINE_ZOOM_SPEED);
                        }
                        voxelAdapter.replayHover();
                        animationAdapter.mouseMoved(e); // keep selection refreshed (zoom ~ mouse move)
                        container.doNotSkipNextWorldRender();
                        forceRepaint();
                    }
                });
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
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        preferences.storeObject("engine_view_voxel_preview_plane", side*2);
                    }
                });
            }
            @Override
            public void mouseExited(MouseEvent e) {
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        preferences.storeObject("engine_view_voxel_preview_plane", -1);
                    }
                });
            }

            // shifting
            // =======================
            private Point mouse_down_point = null;

            @Override
            public void mousePressed(final MouseEvent e) {
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        if ((e.getModifiers() & (MouseEvent.BUTTON1_MASK | MouseEvent.BUTTON2_MASK)) != 0) {
                            mouse_down_point = e.getPoint();
                        } else {
                            mouse_down_point = null;
                        }
                    }
                });
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        mouse_down_point = null;
                    }
                });
            }

            @Override
            public void mouseDragged(final MouseEvent e) {
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        if (mouse_down_point != null) {
                            if (camera.isEnabled()) {
                                // keep speed the same for different container sizes (uses shift2D!)
                                camera.shift2D(150 * (float) (e.getX() - mouse_down_point.getX()) / container.getWidth(),
                                        150 * (float) (e.getY() - mouse_down_point.getY()) / container.getHeight(),
                                        VitcoSettings.SIDE_VIEW_SIDE_MOVE_FACTOR);
                                mouse_down_point = e.getPoint();
                                voxelAdapter.replayHover();
                                container.doNotSkipNextWorldRender();
                                forceRepaint();
                            }
                        }
                    }
                });
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

        // draw bounding box (to display the 2D outline)
        preferences.addPrefChangeListener("use_bounding_box", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                boolean useBoundingBox = (Boolean)o;
                // overlay part
                container.setDrawBoundingBox(useBoundingBox);
                // redraw container
                container.doNotSkipNextWorldRender();
                forceRepaint();
            }
        });

        // create menu
        CommandMenuBar menuPanel = new CommandMenuBar();
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/layout/content/sideview/toolbar" + (side + 1) + ".xml");
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
