package tankwars;

public final class GameConstants {

    /*
     * Size of the game world (and the offscreen buffer it is drawn into),
     * shared by everything that simulates or renders it.
     */
    public static final int GAME_SCREEN_WIDTH = 1280;
    public static final int GAME_SCREEN_HEIGHT = 960;

    public static final int START_MENU_SCREEN_WIDTH = 500;
    public static final int START_MENU_SCREEN_HEIGHT = 550;

    public static final int END_MENU_SCREEN_WIDTH = 500;
    public static final int END_MENU_SCREEN_HEIGHT = 500;

    private GameConstants() {
        // constants holder, not meant to be instantiated
    }
}
