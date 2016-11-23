package com.vitco.app.settings;

import com.threed.jpct.FrameBuffer;

/**
 * Holds dynamic settings. Updating doesn't trigger any changes that might be required.
 */
public class DynamicSettings {

    // sampling mode
    public static int SAMPLING_MODE = FrameBuffer.SAMPLINGMODE_NORMAL;
    public static int SAMPLING_MODE_MULTIPLICAND = 1;
    public static float SAMPLING_MODE_DIVIDEND = 1;

    // setter
    public static void setSamplingMode(boolean highQuality) {
        // set the variables accordingly to the quality
        if (highQuality) {
            SAMPLING_MODE = FrameBuffer.SAMPLINGMODE_OGSS;
            SAMPLING_MODE_MULTIPLICAND = 2;
            SAMPLING_MODE_DIVIDEND = 0.5f;
        } else {
            SAMPLING_MODE = FrameBuffer.SAMPLINGMODE_NORMAL;
            SAMPLING_MODE_MULTIPLICAND = 1;
            SAMPLING_MODE_DIVIDEND = 1;
        }
    }

    // ---------------

    // when changing this make sure the edges are ok
    public static int VOXEL_PLANE_SIZE_X = 20;
    public static int VOXEL_PLANE_SIZE_Y = 20;
    public static int VOXEL_PLANE_SIZE_Z = 20;

    public static float VOXEL_PLANE_RANGE_X = VOXEL_PLANE_SIZE_X / 2f;
    public static float VOXEL_PLANE_RANGE_Y = VOXEL_PLANE_SIZE_Y / 2f;
    public static float VOXEL_PLANE_RANGE_Z = VOXEL_PLANE_SIZE_Z / 2f;

    public static int VOXEL_PLANE_RANGE_X_POS = (int) Math.floor((VOXEL_PLANE_SIZE_X + 1) / 2f);
    public static int VOXEL_PLANE_RANGE_Y_POS = (int) Math.floor((VOXEL_PLANE_SIZE_Y + 1) / 2f);
    public static int VOXEL_PLANE_RANGE_Z_POS = (int) Math.floor((VOXEL_PLANE_SIZE_Z + 1) / 2f);

    public static int VOXEL_PLANE_RANGE_X_NEG = (int) -Math.ceil((VOXEL_PLANE_SIZE_X + 1) / 2f);
    public static int VOXEL_PLANE_RANGE_Y_NEG = (int) -Math.ceil((VOXEL_PLANE_SIZE_Y + 1) / 2f);
    public static int VOXEL_PLANE_RANGE_Z_NEG = (int) -Math.ceil((VOXEL_PLANE_SIZE_Z + 1) / 2f);

    public static float VOXEL_PLANE_WORLD_SIZE_X = VOXEL_PLANE_SIZE_X * VitcoSettings.VOXEL_SIZE;
    public static float VOXEL_PLANE_WORLD_SIZE_Y = VOXEL_PLANE_SIZE_Y * VitcoSettings.VOXEL_SIZE;
    public static float VOXEL_PLANE_WORLD_SIZE_Z = VOXEL_PLANE_SIZE_Z * VitcoSettings.VOXEL_SIZE;

    // setters
    public static void setPlaneSizeX(int newVal) {
        VOXEL_PLANE_SIZE_X = newVal;
        VOXEL_PLANE_RANGE_X = VOXEL_PLANE_SIZE_X / 2f;
        VOXEL_PLANE_RANGE_X_POS = (int) Math.floor((VOXEL_PLANE_SIZE_X + 1) / 2f);
        VOXEL_PLANE_RANGE_X_NEG = (int) -Math.ceil((VOXEL_PLANE_SIZE_X + 1) / 2f);
        VOXEL_PLANE_WORLD_SIZE_X = VOXEL_PLANE_SIZE_X * VitcoSettings.VOXEL_SIZE;
    }
    public static void setPlaneSizeY(int newVal) {
        VOXEL_PLANE_SIZE_Y = newVal;
        VOXEL_PLANE_RANGE_Y = VOXEL_PLANE_SIZE_Y / 2f;
        VOXEL_PLANE_RANGE_Y_POS = (int) Math.floor((VOXEL_PLANE_SIZE_Y + 1) / 2f);
        VOXEL_PLANE_RANGE_Y_NEG = (int) -Math.ceil((VOXEL_PLANE_SIZE_Y + 1) / 2f);
        VOXEL_PLANE_WORLD_SIZE_Y = VOXEL_PLANE_SIZE_Y * VitcoSettings.VOXEL_SIZE;
    }
    public static void setPlaneSizeZ(int newVal) {
        VOXEL_PLANE_SIZE_Z = newVal;
        VOXEL_PLANE_RANGE_Z = VOXEL_PLANE_SIZE_Z / 2f;
        VOXEL_PLANE_RANGE_Z_POS = (int) Math.floor((VOXEL_PLANE_SIZE_Z + 1) / 2f);
        VOXEL_PLANE_RANGE_Z_NEG = (int) -Math.ceil((VOXEL_PLANE_SIZE_Z + 1) / 2f);
        VOXEL_PLANE_WORLD_SIZE_Z = VOXEL_PLANE_SIZE_Z * VitcoSettings.VOXEL_SIZE;
    }
}
