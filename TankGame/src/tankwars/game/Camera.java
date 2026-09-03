package tankwars.game;

import tankwars.GameConstants;

public class Camera {
    private int x;
    private int y;

    private final int viewportW;
    private final int viewportH;

    public Camera(int viewportW, int viewportH) {
        this.viewportW = viewportW;
        this.viewportH = viewportH;
    }

    public void follow(Tank tank) {
        x = (int)tank.getX() - viewportW / 2;
        y = (int)tank.getY() - viewportH / 2;

        x = Math.max(0, Math.min(x, GameConstants.GAME_SCREEN_WIDTH - viewportW));

        y = Math.max(0, Math.min(y, GameConstants.GAME_SCREEN_HEIGHT - viewportH));
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
}
