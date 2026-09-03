package tankwars.objects;

import tankwars.managers.SoundManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Walls {

    private int x;
    private int y;
    private int width;
    private int height;

    private boolean breakable;
    private PowerUps hiddenPowerUp;

    //for asteroid breaking state
    private boolean breaking = false;

    private int health;

    private BufferedImage img;
    private static BufferedImage[] breakFrames;
    private int frameCounter = 0;
    private int currentFrame = 0;


    public Walls(int x, int y, int width, int height, boolean breakable, BufferedImage img, PowerUps powerUp) {
        loadBreakFrames();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.breakable = breakable;
        this.img = img;
        this.hiddenPowerUp = powerUp;


        if(breakable){
            this.health = 50;
        }
        else {
            this.health = Integer.MAX_VALUE;
        }
    }

    private static void loadBreakFrames() {
        if (breakFrames != null) {
            return;
        }
        breakFrames = new BufferedImage[7];

        try {
            for (int i = 0; i < breakFrames.length; i++) {
                breakFrames[i] = ImageIO.read(Objects.requireNonNull
                        (Walls.class.getClassLoader().getResource("walls/break" + (i + 1) + ".png")));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean animationFinished() {
        return breaking && currentFrame == breakFrames.length - 1;
    }


    public void draw(Graphics g) {
        if (breaking) {
            g.drawImage(breakFrames[currentFrame], x, y, width, height, null);
        } else {
            g.drawImage(img, x, y, width, height, null);
        }
    }

    public void update() {
        if (breaking) {
            frameCounter++;

            if (frameCounter >= 5) {
                frameCounter = 0;
                if (currentFrame < breakFrames.length - 1) {
                    currentFrame++;
                }
            }
        }
    }


    public Rectangle getBounds() {
        return new Rectangle(x,y,width,height);
    }

    public boolean isBreakable() {
        return breakable;
    }

    public void takeDamage(int damage) {
        if(!breakable){
            return;
        }
        health -= damage;

        if (health <= 0 && !breaking) {
            breaking = true;
            currentFrame = 0;

            SoundManager.playSound("explosion.wav", -10.0f);
        }
    }

    public boolean isDestroyed() {
        return breakable && health <= 0;
    }

    public PowerUps getHiddenPowerUp() {
        return hiddenPowerUp;
    }

    public PowerUps revealPowerUp() {
        PowerUps temp = hiddenPowerUp;
        hiddenPowerUp = null;
        return temp;
    }


}

