package com.vitco.res;

import com.threed.jpct.SimpleVector;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Hold all used colors. Aids in keeping a consistent color schema.
 */
public final class VitcoSettings {
    // color picker slider
    public static final Color COLOR_PICKER_SLIDER_KNOB_COLOR = new Color(110, 143, 163);

    // e.g. for shortcut manager
    public static final Color EDIT_BG_COLOR = new Color(250, 250, 250); // new Color(187, 209, 255);
    public static final Color EDIT_TEXT_COLOR = new Color(30, 30, 30); // new Color(0, 0, 0);
    public static final Color EDIT_ERROR_BG_COLOR = new Color(255, 171, 161); // new Color(255,200,200);
    public static final Color DEFAULT_TEXT_COLOR = new Color(233,233,233);
    public static final Color DEFAULT_BG_COLOR =  new Color(85, 85, 85);
    public static final Color DEFAULT_HOVER_COLOR = new Color(90,100,120);
    public static final Color DEFAULT_DARK_BG_COLOR = new Color(40, 40, 40);

    // e.g. for side view
    public static final Color ANIMATION_LINE_INNER_COLOR = new Color(230, 20, 30, 200);
    public static final Color ANIMATION_LINE_OUTER_COLOR = new Color(0, 0, 0, 100);
    public static final Color ANIMATION_LINE_PREVIEW_ADD_COLOR = new Color(0, 25, 212, 200);
    public static final Color ANIMATION_LINE_PREVIEW_REMOVE_COLOR = new Color(175, 175, 175, 200);
    public static final Color ANIMATION_BG_COLOR = new Color(126, 126, 126);
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
    public static final float SIDE_VIEW_COARSE_ZOOM_SPEED = 1000f; // for buttons
    public static final float SIDE_VIEW_FINE_ZOOM_SPEED = 300f; // for mouse wheel
    public static final float SIDE_VIEW_MIN_ZOOM = 10000f;
    public static final float SIDE_VIEW_MAX_ZOOM = 30000f;
    public static final float SIDE_VIEW_ZOOM_START = 20000f;
    public static final float SIDE_VIEW_ZOOM_FOV = 0.015f;
    public static final float SIDE_VIEW_SIDE_MOVE_FACTOR = 1f; // "drag" move content

    // xyz orientation
    public static final Color ANIMATION_AXIS_OUTER_COLOR = new Color(0, 0, 0, 255);
    public static final Color ANIMATION_AXIS_COLOR_X = new Color(141, 0, 0, 255);
    public static final Color ANIMATION_AXIS_COLOR_Y = new Color(8, 141, 1, 255);
    public static final Color ANIMATION_AXIS_COLOR_Z = new Color(0, 92, 180, 255);
    public static final float ANIMATION_AXIS_LINE_SIZE = 3.5f;
    public static final Color ANIMATION_CENTER_CROSS_COLOR = new Color(0, 0, 0, 255); // cross in the center

    // main view
    public static final float MAIN_VIEW_ZOOM_SPEED_SLOW = 10;
    public static final float MAIN_VIEW_ZOOM_SPEED_FAST = 25;
    public static final float MAIN_VIEW_ZOOM_OUT_LIMIT = 500;
    public static final float MAIN_VIEW_ZOOM_IN_LIMIT = 100;
    public static final SimpleVector MAIN_VIEW_CAMERA_POSITION = new SimpleVector(-30, -200, -200);
    public static final float MAIN_VIEW_SIDE_MOVE_FACTOR = 0.2f; // "drag" move content
    public static final float MAIN_VIEW_ROTATION_X_FACTOR = 0.02f;
    public static final float MAIN_VIEW_ROTATION_Y_FACTOR = 0.01f;

    // general
    public static final Color DEFAULT_BORDER_COLOR = new Color(90, 90, 90);
    public static final Color DEFAULT_BORDER_COLOR_LIGHT = new Color(130, 135, 144);

    // layer
    public static final Color VISIBLE_LAYER_BG = new Color(85, 85, 85);
    public static final Color HIDDEN_LAYER_BG = new Color(120, 85, 85);
    public static final Color VISIBLE_SELECTED_LAYER_BG = new Color(56, 77, 115);
    public static final Color HIDDEN_SELECTED_LAYER_BG = new Color(127, 48, 43);

    // general table
    public static final Font TABLE_FONT = new Font(
            UIManager.getDefaults().getFont("TabbedPane.font").getName(),
            UIManager.getDefaults().getFont("TabbedPane.font").getStyle(),
            UIManager.getDefaults().getFont("TabbedPane.font").getSize()+1
    );
    public static final Font TABLE_FONT_BOLD = new Font(
            UIManager.getDefaults().getFont("TabbedPane.font").getName(),
            Font.BOLD,
            UIManager.getDefaults().getFont("TabbedPane.font").getSize()+1
    );
    public static final int DEFAULT_TABLE_INCREASE = 10;
    public static final Border DEFAULT_CELL_BORDER =
            BorderFactory.createEmptyBorder(0, 10, 0, 0);
    public static final Border DEFAULT_CELL_BORDER_EDIT =
            BorderFactory.createEmptyBorder(DEFAULT_TABLE_INCREASE / 2, 10, 0, 0);

    // general config
    public static final Float VOXEL_SIZE = 10f;
    public static final Float VOXEL_GROUND_DISTANCE = 10f * VitcoSettings.VOXEL_SIZE + VitcoSettings.VOXEL_SIZE/2 + 0.1f;
    public static final Float VOXEL_GROUND_PLANE_SIZE = 25 * VOXEL_SIZE; // when changing this make sure the edges are ok
    public static final Float VOXEL_GROUND_MAX_RANGE = VitcoSettings.VOXEL_GROUND_PLANE_SIZE/(VitcoSettings.VOXEL_SIZE*2);
    public static final Color VOXEL_GROUND_PLANE_COLOR = new Color(215, 215, 215);
    public static final Color VOXEL_PREVIEW_LINE_COLOR = new Color(0,0,0,100);

    // preview plane
    public static final Float VOXEL_PREVIEW_PLANE_SIZE = 5 * VOXEL_SIZE;
    public static final Color VOXEL_PREVIEW_PLANE_COLOR = new Color(70, 122, 255);

    // file import
    public static final Integer VOXEL_COUNT_FILE_IMPORT_LIMIT = 1000;

}
