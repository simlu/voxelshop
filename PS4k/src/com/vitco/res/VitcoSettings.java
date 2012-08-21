package com.vitco.res;

import java.awt.*;

/**
 * Hold all used colors. Aids in keeping a consistent color schema.
 */
public final class VitcoSettings {
    // e.g. for shortcut manager
    public static final Color EDIT_BG_COLOR = new Color(83, 155, 173); // new Color(187, 209, 255);
    public static final Color EDIT_TEXT_COLOR = new Color(255, 255, 255); // new Color(0, 0, 0);
    public static final Color EDIT_ERROR_BG_COLOR = new Color(208,49,28); // new Color(255,200,200);
    public static final Color DEFAULT_TEXT_COLOR = new Color(10,10,10);
    public static final Color DEFAULT_BG_COLOR =  new Color(255, 255, 255);
    public static final Color DEFAULT_HOVER_COLOR = new Color(235,241,251);

    // e.g. for side view
    public static final Color ANIMATION_LINE_COLOR = new Color(230, 20, 30, 200);
    public static final Color ANIMATION_LINE_PREVIEW_ADD_COLOR = new Color(0, 25, 212, 200);
    public static final Color ANIMATION_LINE_PREVIEW_REMOVE_COLOR = new Color(175, 175, 175, 200);
    public static final Color ANIMATION_BG_COLOR = new Color(126, 126, 126);
    public static final Color ANIMATION_BORDER_COLOR = new Color(56, 56, 56);
    public static final Color ANIMATION_DOT_INNER_COLOR = new Color(230, 20, 30, 150);
    public static final Color ANIMATION_DOT_OUTER_COLOR = new Color(65, 8, 11, 200);
    public static final Color ANIMATION_DOT_HL_INNER_COLOR = new Color(255, 255, 255, 100);
    public static final Color ANIMATION_DOT_HL_OUTER_COLOR = new Color(24, 24, 24, 200);
    public static final Color ANIMATION_DOT_SEL_INNER_COLOR = new Color(0, 25, 212, 100);
    public static final Color ANIMATION_DOT_SEL_OUTER_COLOR = new Color(0, 9, 65, 200);

    public static final float ANIMATION_LINE_SIZE = 2.0f;
    public static final float ANIMATION_CIRCLE_BORDER_SIZE = 1.0f;
    public static final int ANIMATION_CIRCLE_RADIUS = 8;
    // zoom for side view
    public static final double SIDE_VIEW_COARSE_ZOOM_SPEED = 0.2; // for buttons
    public static final double SIDE_VIEW_FINE_ZOOM_SPEED = 0.05; // for mouse wheel
    public static final double SIDE_VIEW_MIN_ZOOM = 0.1;
    public static final double SIDE_VIEW_MAX_ZOOM = 3;

    public static final Color ANIMATION_CROSS_OUTER_COLOR = new Color(0, 0, 0, 255);
    public static final Color ANIMATION_CROSS_CENTER_COLOR = new Color(65, 65, 65, 255);
    public static final Color ANIMATION_CROSS_COLOR_X = new Color(141, 0, 0, 255);
    public static final Color ANIMATION_CROSS_COLOR_Y = new Color(8, 141, 1, 255);
    public static final Color ANIMATION_CROSS_COLOR_Z = new Color(0, 92, 180, 255);
    public static final float ANIMATION_CROSS_LINE_SIZE = 5.0f;

    // main view
    public static final Color MAIN_VIEW_LINE_OVERLAY_COLOR = new Color(0, 0, 0, 50);
    public static final float MAIN_VIEW_LINE_OVERLAY_SIZE = 6f;
    public static final Color MAIN_VIEW_DOT_OVERLAY_COLOR = new Color(255, 0, 0, 200);
    public static final float MAIN_VIEW_ZOOM_SPEED = 10;
    public static final float MAIN_VIEW_ZOOM_OUT_LIMIT = 500;
    public static final float MAIN_VIEW_ZOOM_IN_LIMIT = 100;
    public static final float MAIN_VIEW_CAMERA_POSITION_X = 0;
    public static final float MAIN_VIEW_CAMERA_POSITION_Y = -200;
    public static final float MAIN_VIEW_CAMERA_POSITION_Z = -200;
    public static final float MAIN_VIEW_SIDE_MOVE_FACTOR = 0.2f; // right click drag move
    public static final float MAIN_VIEW_ROTATION_X_FACTOR = 0.02f;
    public static final float MAIN_VIEW_ROTATION_Y_FACTOR = 0.01f;

}
