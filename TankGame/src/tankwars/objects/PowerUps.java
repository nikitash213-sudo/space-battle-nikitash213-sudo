package tankwars.objects;

import tankwars.game.Tank;
import tankwars.managers.SoundManager;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PowerUps {
    private int x;
    private int y;
    private BufferedImage img;
    private String type;

    public PowerUps(int x, int y, BufferedImage img, String type) {
        this.x = x;
        this.y = y;
        this.img = img;
        this.type = type;
    }

    public void draw(Graphics g) {
        g.drawImage(img,x, y,40, 40, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 40, 40);
    }

    public String getType() {
        return type;
    }

    public void apply(Tank tank) {
        SoundManager.playSound("powerups.wav", -15.0f);
        switch (type) {
            case "heal":
                tank.heal(50);
                break;
            case "shield":
                tank.activateShield();
                break;
            case "meteor":
                tank.activateMeteor();
                break;
        }

    }
}



