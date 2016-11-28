package com.vitco.app.core.modes;

import com.vitco.app.core.container.DrawContainer;
import com.vitco.app.core.modes.tools.AbstractTool;
import com.vitco.app.manager.async.AsyncAction;
import com.vitco.app.manager.async.AsyncActionManager;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Defines the abstract adapter that can be used to select certain tools and
 * handle the switching between them.
 */
public abstract class AbstractAdapter extends MouseAdapter {

    // setter
    private static AsyncActionManager asyncActionManager;
    public static void setAsyncActionManager(AsyncActionManager asyncActionManager) {
        AbstractAdapter.asyncActionManager = asyncActionManager;
    }

    // the container this adapter works on
    private final DrawContainer container;
    // constructor
    protected AbstractAdapter(DrawContainer container) { // constructor
        this.container = container;
    }

    // ---------------------

    // the currently active tool
    private AbstractTool tool = null;
    private boolean adapterActive = false;
    // set a certain tool
    public final void setTool(AbstractTool tool) {
        if (this.tool != null) {
            this.tool.deactivate();
        }
        if (tool != null) {
            this.tool = tool;
            if (adapterActive) {
                this.tool.activate();
            }
        }
    }

    // basic init for tools
    public final void activate() {
        this.tool.activate();
        adapterActive = true;
    }

    // basic destruct for tool
    public final void deactivate() {
        this.tool.deactivate();
        adapterActive = false;
    }

    // replay the previous hover event
    public final void replayHover() {
        if (adapterActive && container.isActive()) {
            tool.replayHover();
        }
    }

    // ----------------------

    @Override
    public void mousePressed(final MouseEvent e) {
        asyncActionManager.addAsyncAction(new AsyncAction() {
            @Override
            public void performAction() {
                if (adapterActive) {
                    tool.onMousePressed(e);
                }
            }
        });
    }
    @Override
    public void mouseReleased(final MouseEvent e) {
        asyncActionManager.addAsyncAction(new AsyncAction() {
            @Override
            public void performAction() {
                if (adapterActive) {
                    tool.onMouseReleased(e);
                }
            }
        });
    }
    @Override
    public void mouseEntered(final MouseEvent e) {
        asyncActionManager.addAsyncAction(new AsyncAction() {
            @Override
            public void performAction() {
                if (adapterActive) {
                    container.activate();
                    tool.onMouseEntered(e);
                }
            }
        });
    }
    @Override
    public void mouseExited(final MouseEvent e) {
        asyncActionManager.addAsyncAction(new AsyncAction() {
            @Override
            public void performAction() {
                if (adapterActive) {
                    container.deactivate();
                    tool.onMouseExited(e);
                }
            }
        });
    }
    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        asyncActionManager.addAsyncAction(new AsyncAction() {
            @Override
            public void performAction() {
                if (adapterActive) {
                    tool.onMouseWheelMoved(e);
                }
            }
        });
    }
    @Override
    public void mouseDragged(final MouseEvent e) {
        asyncActionManager.addAsyncAction(new AsyncAction() {
            @Override
            public void performAction() {
                if (adapterActive) {
                    tool.onMouseDragged(e);
                }
            }
        });
    }
    @Override
    public void mouseMoved(final MouseEvent e) {
        asyncActionManager.addAsyncAction(new AsyncAction() {
            @Override
            public void performAction() {
                if (adapterActive) {
                    tool.onMouseMoved(e);
                }
            }
        });
    }

}
