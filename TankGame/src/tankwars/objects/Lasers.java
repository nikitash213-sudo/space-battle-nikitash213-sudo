package tankwars.objects;

import tankwars.GameConstants;
import tankwars.game.Tank;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Lasers {

    private static final int WIDTH = 64;
    private static final int HEIGHT = 6;
    private float x;
    private float y;
    private float angle;
    private Tank owner;
    private final BufferedImage laserImage;

    private static final float SPEED = 10;

    private boolean active = true;

    public Lasers(float x, float y, float angle, Tank tank, BufferedImage laserImage) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.owner = tank;
        this.laserImage = laserImage;
    }


    public Tank getOwner() {
        return owner;
    }

    public void update(){
        x += SPEED * (float)Math.cos(Math.toRadians(angle));
        y += SPEED * (float)Math.sin(Math.toRadians(angle));
    }

    public void draw(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        at.rotate(Math.toRadians(angle), WIDTH/2.0, HEIGHT/2.0);
        at.scale(WIDTH/(double) laserImage.getWidth(), HEIGHT/(double) laserImage.getHeight());

        g2.drawImage(laserImage, at, null);
    }
    public boolean isActive() {
        return active;
    }

    public void deactivate(){
        active = false;
    }

    public Rectangle getBounds(){
        return new Rectangle((int)x, (int)y, WIDTH, HEIGHT);
    }

    public boolean isOutOfBounds() {
        return x < 0 || y < 0 || x > GameConstants.GAME_SCREEN_WIDTH || y > GameConstants.GAME_SCREEN_HEIGHT;
    }

}
