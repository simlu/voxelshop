package com.vitco.settings;

import com.threed.jpct.SimpleVector;
import com.vitco.core.data.container.VOXELMODE;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Hold all used colors and settings. Aids in keeping a consistent color schema.
 */
public final class VitcoSettings {

    // object that all data access is synced to
    public static final Object SYNC = new Object();

    // color for the "big bounding box"
    public static final Color BOUNDING_BOX_COLOR = new Color(255, 255, 255, 100);

    // float-able frame settings
    public static final Border FLOATING_FRAME_BORDER = BorderFactory.createLineBorder(new Color(20, 20, 20), 3);
    // frame settings
    public static final int FRAME_BORDER_SIZE = 4;
    public static final Border FRAME_BORDER = BorderFactory.createLineBorder(new Color(20, 20, 20), FRAME_BORDER_SIZE);
    public final static Color MAIN_MENU_ENABLED_COLOR = new Color(230, 230, 230);
    public final static Color MAIN_MENU_DISABLED_COLOR = new Color(170, 170, 170);
    public final static Color MAIN_MENU_BACKGROUND = new Color(83, 83, 83);
    // soft black color
    public final static Color SOFT_BLACK = new Color(38, 38, 38);
    public final static Color SOFT_WHITE = new Color(230, 230, 230);
    public static final Color DEFAULT_BG_COLOR = new Color(83, 83, 83);
    public static final Color FRAME_BG_COLOR = new Color(80, 80, 80);
    // tabbed pane
    public static final Color TABBED_PANE_HEADER_ACTIVE_COLOR = new Color(130, 130, 130);
    // button colors
    public static final Color BUTTON_BACKGROUND_DEFAULT = new Color(83, 83, 83);
    public static final Color BUTTON_BACKGROUND_ROLLOVER = new Color(98, 98, 98);
    public static final Color BUTTON_BACKGROUND_SELECTED = new Color(55, 55, 55);
    public static final Color BUTTON_BACKGROUND_DISABLED_SELECTED = new Color(83, 83, 83);
    public static final Color BUTTON_BACKGROUND_PRESSED = new Color(83, 83, 83);
    public static final Color BUTTON_BORDER_COLOR = new Color(39, 39, 39);

    public static final Color BUTTON_ENABLED = new Color(120, 20, 20);
    public static final Color BUTTON_ENABLED_OVER = new Color(160, 30, 30);

    // texture window settings
    public static final Color TEXTURE_BORDER = Color.BLACK;
    public static final Color TEXTURE_BORDER_ACTIVE = Color.ORANGE;
    public static final Color TEXTURE_BORDER_SELECTED = Color.RED;

    public static final String VERSION_ID = "1.7.07";
    // version id
    public static final String TITLE_STRING = "VoxelShop - Alpha Version (V" + VERSION_ID + ")";

    // wire-frame / select
    public static final Color WIREFRAME_COLOR = new Color(255, 255, 255);
    public static final Color SELECTED_VOXEL_WIREFRAME_COLOR = new Color(255, 255, 255);
    public static final Color SELECTED_VOXEL_WIREFRAME_COLOR_SHIFTED = new Color(175, 255, 172);

    // ghost overlay line color
    public static final Color GHOST_VOXEL_OVERLAY_LINE_COLOR = new Color(255, 255, 255, 100);

    // these can change externally
    public static final String PROGRAM_UPDATER_URL = "http://www.blackflux.com/software/vs/upd/digest.txt";

    // cursor
    public static final Cursor CURSOR_DEFAULT = Cursor.getDefaultCursor();
    public static final Cursor CURSOR_BLANK = Toolkit.getDefaultToolkit().createCustomCursor(
            new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");

    // initial settings
    public static final Color INITIAL_CURRENT_COLOR = new Color(199, 89, 68);
    public static final VOXELMODE INITIAL_VOXEL_MODE = VOXELMODE.DRAW;
    public static final boolean INITIAL_MODE_IS_ANIMATION = false;
    public static final boolean INITIAL_ANIMATION_VOXEL_SNAP = true;

    // e.g. for shortcut manager
    public static final Color EDIT_BG_COLOR = new Color(250, 250, 250); // new Color(187, 209, 255);
    public static final Color EDIT_TEXT_COLOR = new Color(30, 30, 30); // new Color(0, 0, 0);
    public static final Color EDIT_ERROR_BG_COLOR = new Color(255, 171, 161); // new Color(255,200,200);
    public static final Color DEFAULT_TEXT_COLOR = new Color(233,233,233);
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
    public static final float SIDE_VIEW_FINE_ZOOM_SPEED = 500f; // for mouse wheel
    public static final float SIDE_VIEW_MIN_ZOOM = 10000f;
    public static final float SIDE_VIEW_MAX_ZOOM = 60000f;
    public static final float SIDE_VIEW_ZOOM_START = 20000f;
    public static final float SIDE_VIEW_ZOOM_FOV = 0.015f;
    public static final float SIDE_VIEW_SIDE_MOVE_FACTOR = 1f; // "drag" move content
    public static final SimpleVector SIDE_VIEW1_CAMERA_POSITION = new SimpleVector(0,-1,-VitcoSettings.SIDE_VIEW_ZOOM_START);
    public static final SimpleVector SIDE_VIEW2_CAMERA_POSITION = new SimpleVector(0,-VitcoSettings.SIDE_VIEW_ZOOM_START,-1);
    public static final SimpleVector SIDE_VIEW3_CAMERA_POSITION = new SimpleVector(-VitcoSettings.SIDE_VIEW_ZOOM_START,0,-1);

    // xyz orientation
    public static final Color ANIMATION_AXIS_OUTER_COLOR = new Color(0, 0, 0, 255);
    public static final Color ANIMATION_AXIS_COLOR_X = new Color(141, 0, 0, 255);
    public static final Color ANIMATION_AXIS_COLOR_Y = new Color(8, 141, 1, 255);
    public static final Color ANIMATION_AXIS_COLOR_Z = new Color(0, 92, 180, 255);
    public static final float ANIMATION_AXIS_LINE_SIZE = 3.5f;
    public static final Color ANIMATION_CENTER_CROSS_COLOR = new Color(0, 0, 0, 255); // cross in the center

    // main view
    public static final float MAIN_VIEW_ZOOM_SPEED_SLOW = 15;
    public static final float MAIN_VIEW_ZOOM_SPEED_FAST = 25;
    public static final float MAIN_VIEW_ZOOM_OUT_LIMIT = 1500;
    public static final float MAIN_VIEW_ZOOM_IN_LIMIT = 100;
    public static final float MAIN_VIEW_ZOOM_FOV = 1.25f;
    public static final SimpleVector MAIN_VIEW_CAMERA_POSITION = new SimpleVector(-400, -500, -500);
    public static final float MAIN_VIEW_SIDE_MOVE_FACTOR = 0.2f; // "drag" move content
    public static final float MAIN_VIEW_ROTATION_X_FACTOR = 0.02f;
    public static final float MAIN_VIEW_ROTATION_Y_FACTOR = 0.01f;

    // general
    public static final Color DEFAULT_BORDER_COLOR = new Color(60, 60, 60);
    public static final Color DEFAULT_BORDER_COLOR_LIGHT = new Color(130, 135, 144);
    public static final Color DEFAULT_BORDER_COLOR_HIGHLIGHTED = new Color(189, 58, 12);

    // layer
    public static final Color VISIBLE_LAYER_BG = new Color(95, 95, 95);
    public static final Color HIDDEN_LAYER_BG = new Color(120, 85, 85);
    public static final Color VISIBLE_SELECTED_LAYER_BG = new Color(56, 77, 115);
    public static final Color HIDDEN_SELECTED_LAYER_BG = new Color(127, 48, 43);
    public static final int MAX_LAYER_COUNT = 50;

    // general table
    public static final Font TABLE_FONT = new Font("Tohama", Font.PLAIN, 12);
    public static final Font TABLE_FONT_BOLD = new Font("Tohama", Font.BOLD, 12);
    public static final int DEFAULT_TABLE_INCREASE = 10;
    public static final Border DEFAULT_CELL_BORDER =
            BorderFactory.createEmptyBorder(0, 10, 0, 0);
    public static final Border DEFAULT_CELL_BORDER_EDIT =
            BorderFactory.createEmptyBorder(DEFAULT_TABLE_INCREASE / 2, 10, 0, 0);
    public static final Color TABLE_HEADER_BG_COLOR = new Color(100, 100, 100, 200);
    public static final Color TABLE_HEADER_COLOR = new Color(40, 40, 40);
    public static final Color DEFAULT_CELL_COLOR = new Color(95, 95, 95);
    public static final Color DEFAULT_SCROLL_PANE_BG_COLOR = new Color(100, 100, 100, 200);

    // general voxel stuff
    public static final float VOXEL_SIZE = 10f;
    public static final float HALF_VOXEL_SIZE = 5f;

    public static final float VOXEL_GROUND_DISTANCE = VitcoSettings.VOXEL_SIZE/2;
    public static final Color VOXEL_GROUND_PLANE_COLOR = new Color(215, 215, 215);

    public static final Color VOXEL_PREVIEW_LINE_COLOR = new Color(0,0,0,100);
    public static final Color VOXEL_PREVIEW_LINE_COLOR_BRIGHT = new Color(255,255,255,100);

    public static final SimpleVector VOXEL_WORLD_OFFSET = new SimpleVector(
            VitcoSettings.HALF_VOXEL_SIZE, VitcoSettings.VOXEL_GROUND_DISTANCE, VitcoSettings.HALF_VOXEL_SIZE);

    public static final Color[] GRAYSCALE_COLOR_SWATCH = new Color[]{
            new Color(0, 0, 0), new Color(14, 14, 14), new Color(27, 27, 27),
            new Color(41, 41, 41), new Color(54, 54, 54), new Color(68, 68, 68),
            new Color(81, 81, 81), new Color(95, 95, 95), new Color(108, 108, 108),
            new Color(122, 122, 122), new Color(135, 135, 135), new Color(149, 149, 149),
            new Color(162, 162, 162), new Color(176, 176, 176), new Color(189, 189, 189),
            new Color(203, 203, 203), new Color(216, 216, 216), new Color(230, 230, 230),
            new Color(243, 243, 243), new Color(255, 255, 255)
    };

    // maximum voxel count per layer (and for picture import)
    public static final int MAX_VOXEL_COUNT_PER_LAYER = 100000;

    // grid size for triangulation
    public static final int TRI_GRID_SIZE = 13;
    //offset
    public static final int TRI_GRID_OFFSET = 6;

    // the corners of polygon triangulation are shifted into this direction
    // to reduce see through edges
    public static final float TRIANGLE_INTERPOLATION_VALUE = 0.002f;
    // interpolation to move textures away from the corners,
    // reduces showing of neighbouring pixels (interpolation errors)
    public static final float TEXTURE_INTERPOLATION_VALUE = 0.004f;
    // interpolation to show black outline (moves textures towards the outside)
    public static final float BORDER_INSET_VALUE = 0.04f;

    // help screen settings
    public static final Color HELP_OVERLAY_DEFAULT_COLOR = Color.ORANGE;
    public static final Color HELP_OVERLAY_HIGHLIGHT_COLOR = new Color(184, 31, 0);

    // splash screen colors
    public static final Color SPLASH_SCREEN_OVERLAY_TEXT_COLOR = new Color(127, 157, 184);

}
