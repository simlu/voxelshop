package com.vitco.engine;

import com.threed.jpct.Camera;
import com.threed.jpct.SimpleVector;
import com.vitco.res.VitcoSettings;

import java.util.ArrayList;

/**
 * Basic camera interaction.
 */
public class CCamera extends Camera {
    private final float[] amountShifted = new float[2]; // current shift distance
    private final float[] amountShifted2D = new float[2]; // current shift distance
    private float zoom = 400; // current zoom distance
    private final float[] amountRotated = new float[2];
    private SimpleVector resetViewLookAt = new SimpleVector(1,1,1).normalize();

    // the zoom limit
    private float ZOOM_OUT_MAX = -1;
    private float ZOOM_IN_MAX = -1;

    public final void setZoomLimits(float zoom_in_max, float zoom_out_max) {
        ZOOM_OUT_MAX = zoom_out_max;
        ZOOM_IN_MAX = zoom_in_max;
    }

    // to disable all camera interaction
    private boolean enableCamera = true;
    public final void setEnabled(boolean b) {
        enableCamera = b;
    }

    // camera change listener
    private final ArrayList<CameraChangeListener> listener = new ArrayList<CameraChangeListener>();
    // add listener
    public void addCameraChangeListener(CameraChangeListener ccl) {
        listener.add(ccl);
    }
    // remove listener
    public void removeCameraChangeListener(CameraChangeListener ccl) {
        listener.remove(ccl);
    }
    // notify all listeners
    private void notifyListener() {
        for (CameraChangeListener ccl : listener) {
            ccl.onCameraChange();
        }
    }

    // get the current origin
    private SimpleVector getOrigin() {
        return new SimpleVector(0, -amountShifted[1], 0);
    }

    public void zoomIn(float speed) {
        if (enableCamera && speed != 0) {
            speed = Math.abs(speed);
            if (zoom - speed > ZOOM_IN_MAX || ZOOM_IN_MAX == -1) {
                zoom -= speed;
            } else {
                zoom = ZOOM_IN_MAX;
            }
            update();
        }
    }

    public void zoomOut(float speed) {
        if (enableCamera && speed != 0) {
            speed = Math.abs(speed);
            if (zoom + speed < ZOOM_OUT_MAX || ZOOM_OUT_MAX == -1) {
                zoom += speed;
            } else {
                zoom = ZOOM_OUT_MAX;
            }
            update();
        }
    }

    public void rotate(float amountX, float amountY) {
        if (enableCamera && (amountX != 0 || amountY != 0)) {
            amountRotated[0] += amountX;
            amountRotated[1] += amountY;
            update();
        }
    }

    public void shift(float amountX, float amountY, float factor) {
        if (enableCamera && (amountX != 0 || amountY != 0)) {
            amountShifted[0] += amountX*factor;
            amountShifted[1] += amountY*factor;
            update();
        }
    }

    // shift view (to current perspective)
    public void shift2D(float amountX, float amountY, float factor) {
        if (enableCamera && (amountX != 0 || amountY != 0)) {
            amountShifted2D[0] += amountX*factor;
            amountShifted2D[1] += amountY*factor;
            update();
        }
    }

    public final void setView(SimpleVector pos) {
        if (enableCamera) {
            resetViewLookAt = pos.normalize();
            resetViewLookAt.scalarMul(-1);
            amountRotated[0] = 0;
            amountRotated[1] = 0;
            amountShifted[0] = 0;
            amountShifted[1] = 0;
            amountShifted2D[0] = 0;
            amountShifted2D[1] = 0;
            zoom = pos.distance(getOrigin()); // needs to go last
            update();
        }
    }

    private void update() {
        // set camera position and look at our inverted view pos point
        SimpleVector origin = getOrigin();
        this.setPosition(origin);
        origin.add(resetViewLookAt);
        this.lookAt(origin);

        // rotate the camera correctly
        this.rotateAxis(this.getYAxis(), amountRotated[0] * VitcoSettings.MAIN_VIEW_ROTATION_Y_FACTOR);
        this.rotateX(-amountRotated[1] * VitcoSettings.MAIN_VIEW_ROTATION_X_FACTOR);

        // move the camera out
        this.moveCamera(Camera.CAMERA_MOVEOUT, zoom);

        // shift the camera
        this.moveCamera(new SimpleVector(0, -1, 0), amountShifted[1]);
        this.moveCamera(Camera.CAMERA_MOVELEFT, amountShifted[0]);

        // shift the camera 2D
        this.moveCamera(Camera.CAMERA_MOVELEFT, amountShifted2D[0]);
        this.moveCamera(Camera.CAMERA_MOVEUP, amountShifted2D[1]);

        // notify all the listeners
        notifyListener();
    }

}
