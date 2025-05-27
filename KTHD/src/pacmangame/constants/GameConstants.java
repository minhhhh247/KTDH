package pacmangame.constants;

import java.awt.Color;

public class GameConstants {
    public static final int GRID_SIZE = 24;
    public static final int PLAYER_SQUARE_SIZE = 24;
    public static final double FIXED_POINT_X = 12.0;
    public static final double FIXED_POINT_Y = 12.0;

    // Tốc độ di chuyển
    public static final double PLAYER_SPEED = 2.0;
    public static final double GHOST_SPEED = 1.5;

    // Hitbox
    public static final boolean PLAYER_FULL_HITBOX = false;
    public static final double PLAYER_COLLISION_SIZE = PLAYER_FULL_HITBOX ?
            GRID_SIZE : GRID_SIZE * 0.8;
    public static final double GHOST_COLLISION_SIZE = GRID_SIZE * 0.7;
    public static final double SLIDE_DISTANCE = 0.5;

    // Scale
    public static final double MIN_SCALE_FACTOR = 0.5;
    public static final double MAX_SCALE_FACTOR = 2.0;

    // Colors
    public static final Color PATH_COLOR = Color.DARK_GRAY;
    public static final Color WALL_COLOR = Color.BLUE;
    public static final Color MONSTER_COLOR = Color.ORANGE;
    public static final Color PACMAN_COLOR = Color.YELLOW;
    public static final Color GHOST_COLOR = Color.RED;
    public static final Color PELLET_COLOR = Color.WHITE;
    public static final Color SCORE_COLOR = Color.WHITE;
    public static final Color SCALABLE_BLOCK_COLOR = Color.GREEN; // Màu cho ô type 2
    public static final Color DYNAMIC_WALL_COLOR = Color.MAGENTA; // Màu cho ô type 3

    public static final int TIMER_DELAY = 16; // ~60 FPS

    // Hướng di chuyển
    public static final int DIRECTION_NONE = -1;
    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_DOWN = 2;
    public static final int DIRECTION_LEFT = 3;

    // Game mechanics
    public static final int PELLET_POINTS = 10;
    public static final int PELLET_SIZE = 4;
    public static final int GHOST_SPAWN_INTERVAL = 10000;
    public static final int MAX_GHOSTS = 5;

    // Thành phần map mới
    public static final double INITIAL_TYPE2_SIZE = GRID_SIZE / 8.0; // 1/8 kích thước ô
    public static final double INITIAL_TYPE3_SIZE = GRID_SIZE; // 1 ô đầy đủ
    public static final double SCALE_FACTOR = 2.0; // Hệ số scale
    public static final int SCALE_INTERVAL = 2000; // 2 giây (milliseconds)
}
