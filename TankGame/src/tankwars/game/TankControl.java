package tankwars.game;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author anthony-pc
 */
public class TankControl implements KeyListener {

    private final Tank t1;
    /*
     * Maps each bound key code to the action it triggers. Looking the key up
     * in a map replaces the chain of if-statements comparing against each
     * bound key one by one.
     */
    private final Map<Integer, PlayerAction> keyBindings = new HashMap<>();

    public TankControl(Tank t1, int up, int down, int left, int right, int shoot) {
        this.t1 = t1;
        this.keyBindings.put(up, PlayerAction.UP);
        this.keyBindings.put(down, PlayerAction.DOWN);
        this.keyBindings.put(left, PlayerAction.LEFT);
        this.keyBindings.put(right, PlayerAction.RIGHT);
        this.keyBindings.put(shoot, PlayerAction.SHOOT);
    }

    @Override
    public void keyTyped(KeyEvent ke) {
        // unused, but required by the KeyListener interface
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        PlayerAction action = this.keyBindings.get(ke.getKeyCode());
        if (action != null) {
            this.t1.press(action);
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        PlayerAction action = this.keyBindings.get(ke.getKeyCode());
        if (action != null) {
            this.t1.release(action);
        }
    }
}
