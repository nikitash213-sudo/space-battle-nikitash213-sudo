package tankwars;

import java.awt.*;

/**
 * The screens the game can be showing. Each constant doubles as the
 * CardLayout key (via name()) for its panel, and carries the size the
 * window's content area should have while that screen is visible.
 */
public enum GameState {
    START(GameConstants.START_MENU_SCREEN_WIDTH, GameConstants.START_MENU_SCREEN_HEIGHT),
    CONTROLS(GameConstants.START_MENU_SCREEN_WIDTH, GameConstants.START_MENU_SCREEN_HEIGHT),
    GAME(GameConstants.GAME_SCREEN_WIDTH, GameConstants.GAME_SCREEN_HEIGHT),
    END(GameConstants.END_MENU_SCREEN_WIDTH, GameConstants.END_MENU_SCREEN_HEIGHT);


    private final Dimension panelSize;

    GameState(int width, int height) {
        this.panelSize = new Dimension(width, height);
    }

    public Dimension getPanelSize() {
        return panelSize;
    }
}
