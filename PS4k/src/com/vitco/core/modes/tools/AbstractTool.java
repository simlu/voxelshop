package com.vitco.core.modes.tools;

import com.vitco.core.container.DrawContainer;
import com.vitco.core.data.Data;
import com.vitco.core.data.notification.DataChangeAdapter;
import com.vitco.manager.action.ActionManager;
import com.vitco.manager.async.AsyncAction;
import com.vitco.manager.async.AsyncActionManager;
import com.vitco.manager.lang.LangSelectorInterface;
import com.vitco.manager.pref.PrefChangeListener;
import com.vitco.manager.pref.PreferencesInterface;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.misc.ColorTools;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Abstract of a basic tool that can be used to interact with a file.
 */
public abstract class AbstractTool {

    // set the data connector
    protected static Data data;
    public static void setData(final Data data) {
        AbstractTool.data = data;

        // know visibility of layer
        data.addDataChangeListener(new DataChangeAdapter() {
            @Override
            public void onLayerStateChanged() {
                currentLayer = data.getSelectedLayer();
                layerVisible = data.getLayerVisible(currentLayer);
            }
        });
        currentLayer = data.getSelectedLayer();
        layerVisible = data.getLayerVisible(currentLayer);
    }

    // var & setter
    protected static ActionManager actionManager;
    public static void setActionManager(ActionManager actionManager) {
        if (AbstractTool.actionManager == null) {
            AbstractTool.actionManager = actionManager;
        }
    }

    // var & setter
    protected static LangSelectorInterface langSelector;
    public static void setLangSelector(LangSelectorInterface langSelector) {
        if (AbstractTool.langSelector == null) {
            AbstractTool.langSelector = langSelector;
        }
    }

    // set the preference connector
    protected static PreferencesInterface preferences;
    public static void setPreferences(PreferencesInterface preferences) {
        if (AbstractTool.preferences == null) {
            AbstractTool.preferences = preferences;
            // know current color
            preferences.addPrefChangeListener("currently_used_color", new PrefChangeListener() {
                @Override
                public void onPrefChange(Object newValue) {
                    currentColor = (float[])newValue;
                }
            });
            // know visibility of bounding box
            preferences.addPrefChangeListener("use_bounding_box", new PrefChangeListener() {
                @Override
                public void onPrefChange(Object newValue) {
                    useBoundingBox = (Boolean) newValue;
                }
            });
        }
    }

    // holds keyboard event listener that are currently active (only active tool listens to keyboard events)
    private static final ArrayList<ActiveEvent> keyboardEventListener = new ArrayList<ActiveEvent>();

    // set the async manager
    protected static AsyncActionManager asyncActionManager;
    public static void setAsyncActionManager(final AsyncActionManager asyncActionManager) {
        if (AbstractTool.asyncActionManager == null) {
            AbstractTool.asyncActionManager = asyncActionManager;

            // Note: Event Propagation: The events are all first entering these global manager
            // and are then propagated to the elements.

            // handle keyboard events (global)
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(final KeyEvent e) {
                    asyncActionManager.addAsyncAction(new AsyncAction() {
                        @Override
                        public void performAction() {
                            if (ctrlDown != e.isControlDown() || altDown != e.isAltDown() || shiftDown != e.isShiftDown()) {
                                ctrlDown = e.isControlDown();
                                altDown = e.isAltDown();
                                shiftDown = e.isShiftDown();
                                synchronized(keyboardEventListener) {
                                    for (ActiveEvent event : keyboardEventListener) {
                                        event.dispatch();
                                    }
                                }
                            }
                        }
                    });
                    return false;
                }
            });

            // handle mouse events (global)
            Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
                public void eventDispatched (final AWTEvent event) {
                    asyncActionManager.addAsyncAction(new AsyncAction() {
                        @Override
                        public void performAction() {
                            if (event instanceof MouseEvent) {
                                MouseEvent e = (MouseEvent)event;
                                mouse1Down = (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK)
                                        == InputEvent.BUTTON1_DOWN_MASK;
                                mouse2Down = (e.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK)
                                        == InputEvent.BUTTON2_DOWN_MASK;
                                mouse3Down = (e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK)
                                        == InputEvent.BUTTON3_DOWN_MASK;
                            }
                        }
                    });
                }
            }, AWTEvent.MOUSE_EVENT_MASK);
        }
    }


    // container this tool is working on
    protected final DrawContainer container;
    // side this tool is working on
    protected final int side;
    // constructor
    public AbstractTool(DrawContainer container, int side) {
        this.container = container;
        this.side = side;

        // enable/disable snap
        preferences.addPrefChangeListener("voxel_snap_enabled", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object newValue) {
                AbstractTool.this.container.setVoxelSnap((Boolean) newValue);
            }
        });

    }

    // -----------------

    // preference/setting variables
    private static float[] currentColor = ColorTools.colorToHSB(VitcoSettings.INITIAL_CURRENT_COLOR);
    private static boolean useBoundingBox = false;
    private static boolean layerVisible = false;
    private static int currentLayer = 0;
    public static boolean isLayerVisible() {
        return layerVisible;
    }
    protected static float[] getCurrentColor() {
        return currentColor;
    }
    public static boolean isUseBoundingBox() {
        return useBoundingBox;
    }
    public static int getCurrentLayer() {
        return currentLayer;
    }

    // -----------------

    // called when there is an event to process
    protected abstract void onKeyEvent();
    protected abstract void onActivate();
    protected abstract void onDeactivate();
    protected abstract void mousePressed(MouseEvent e);
    protected abstract void mouseReleased(MouseEvent e);
    protected abstract void mouseEntered(MouseEvent e);
    protected abstract void mouseExited(MouseEvent e);
    protected abstract void mouseWheelMoved(MouseWheelEvent e);
    protected abstract void mouseDragged(MouseEvent e);
    protected abstract void mouseMoved(MouseEvent e);

    public final void onMousePressed(MouseEvent e) {
        if (active) {
            replayHover();
        }
        setLastEvent(e);
        mousePressed(e);
    }
    public final void onMouseReleased(MouseEvent e) {
        if (active) {
            replayHover();
        }
        setLastEvent(e);
        mouseReleased(e);
    }
    public final void onMouseEntered(MouseEvent e) {
        mouseEntered(e);
    }
    public final void onMouseExited(MouseEvent e) {
        mouseExited(e);
    }
    public final void onMouseWheelMoved(MouseWheelEvent e) {
        mouseWheelMoved(e);
    }
    public final void onMouseDragged(MouseEvent e) {
        setLastEvent(e);
        mouseDragged(e);
    }
    public final void onMouseMoved(MouseEvent e) {
        setLastEvent(e);
        mouseMoved(e);
    }

    // the dispatch is triggered when a key is pressed/released
    private final ActiveEvent keyEvent = new ActiveEvent() {
        @Override
        public void dispatch() {
            if (active) {
                onKeyEvent();
                replayHover();
            }
        }
    };

    // indicated whether this tool is active
    private boolean active = false;
    public final void activate() {
        active = true;
        synchronized(keyboardEventListener) {
            keyboardEventListener.add(keyEvent);
        }
        replayHover();
        onActivate();
    }
    public final void deactivate() {
        active = false;
        synchronized(keyboardEventListener) {
            keyboardEventListener.remove(keyEvent);
        }
        // release the mouse
        if (AbstractTool.lastEvent != null && isMouseDown()) {
            mouseReleased(AbstractTool.lastEvent);
        }
        // handle tool specific action
        onDeactivate();
    }

    // the previous executed event
    private static MouseEvent lastEvent = null;
    public static void setLastEvent(MouseEvent lastEvent) {
        AbstractTool.lastEvent = lastEvent;
    }

    // called when the previous event should be replayed
    // replay the previous hover event
    public final void replayHover() {
        if (container.isActive()) {
            if (AbstractTool.lastEvent != null) {
                if (isMouseDown()) {
                    mouseDragged(AbstractTool.lastEvent);
                } else {
                    mouseMoved(AbstractTool.lastEvent);
                }
            }
        }
    }

    // #####################################
    // keyboard and mouse state variables (global)
    // #####################################

    private static boolean ctrlDown = false;
    private static boolean altDown = false;
    private static boolean shiftDown = false;
    protected static boolean isCtrlDown() {
        return ctrlDown;
    }
    protected static boolean isAltDown() {
        return altDown;
    }
    protected static boolean isShiftDown() {
        return shiftDown;
    }

    private static boolean mouse1Down = false;
    private static boolean mouse2Down = false;
    private static boolean mouse3Down = false;
    protected static boolean isMouse1Down() {
        return mouse1Down;
    }
    protected static boolean isMouse2Down() {
        return mouse2Down;
    }
    protected static boolean isMouse3Down() {
        return mouse3Down;
    }
    protected static boolean isMouseDown() {
        return mouse1Down || mouse2Down || mouse3Down;
    }

}
