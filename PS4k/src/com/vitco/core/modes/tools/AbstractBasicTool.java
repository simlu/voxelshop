package com.vitco.core.modes.tools;

import com.vitco.core.container.DrawContainer;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Implements basic functionality (ctrl ~ camera, shift ~ special functionality, ...)
 */
public abstract class AbstractBasicTool extends AbstractTool {

   // constructor
    public AbstractBasicTool(DrawContainer container, int side) {
        super(container, side);
    }

    // true if a drag event is happening
    private boolean eventDrag = false;

    // true if a drag has occured
    private boolean wasDrag = false;

    // shift state that was detected on mouse press event
    // (before drag event)
    private boolean shiftDown = false;

    // abstract events
    protected abstract void move(MouseEvent e);
    protected abstract void press(MouseEvent e);
    protected abstract void release(MouseEvent e);
    protected abstract void drag(MouseEvent e);
    protected abstract void click(MouseEvent e);
    protected abstract void singleClick(MouseEvent e);

    protected abstract void shiftMove(MouseEvent e);
    protected abstract void shiftPress(MouseEvent e);
    protected abstract void shiftRelease(MouseEvent e);
    protected abstract void shiftDrag(MouseEvent e);
    protected abstract void shiftClick(MouseEvent e);
    protected abstract void singleShiftClick(MouseEvent e);

    protected abstract void key();

    // to be implemented by more specific class
    protected abstract boolean allowDrag();
    protected abstract void softCleanUp();

    @Override
    protected final void onKeyEvent() {
        key();
    }

    @Override
    protected final void onActivate() {}

    @Override
    protected final void onDeactivate() {
        if (container.isActive()) {
            softCleanUp();
        }
    }

    @Override
    protected final void mousePressed(MouseEvent e) {
        // control is used for camera
        if (!isCtrlDown()) {
            wasDrag = false;
            if (isShiftDown()) {
                shiftClick(e);
            } else {
                click(e);
            }
            if (allowDrag()) {
                eventDrag = true;
                container.enableCamera(false);
                shiftDown = isShiftDown();
                if (shiftDown) {
                    shiftPress(e);
                } else {
                    press(e);
                }
            }
        }
    }

    @Override
    protected final void mouseReleased(MouseEvent e) {
        if (eventDrag) {
            eventDrag = false;
            if (shiftDown) {
                shiftRelease(e);
                shiftDown = false;
            } else {
                release(e);
            }
            mouseMoved(e); // make this also a move event
            container.enableCamera(true);
            // do a single click (release) without drag
            if (!wasDrag) {
                if (isShiftDown()) {
                    singleShiftClick(e);
                } else {
                    singleClick(e);
                }
            }
        }
    }

    @Override
    protected final void mouseEntered(MouseEvent e) {}

    @Override
    protected final void mouseExited(MouseEvent e) {
        softCleanUp();
    }

    @Override
    protected final void mouseWheelMoved(MouseWheelEvent e) {}

    @Override
    protected final void mouseDragged(MouseEvent e) {
        if (eventDrag) {
            wasDrag = true;
            if (shiftDown) {
                shiftDrag(e);
            } else {
                drag(e);
            }
        }
    }

    @Override
    protected final void mouseMoved(MouseEvent e) {
        if (!isCtrlDown()) {
            if (isShiftDown()) {
                shiftMove(e);
            } else {
                move(e);
            }
        } else {
            softCleanUp();
        }
    }
}
