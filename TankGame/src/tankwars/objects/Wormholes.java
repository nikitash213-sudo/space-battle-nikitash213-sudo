package tankwars.objects;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Wormholes {
    private Rectangle bounds;
    private int destinationX;
    private int destinationY;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean disappearing = false;
    private int animationFrame;
    private int animationCounter;
    private int animationSpeed = 30;
    private boolean visible = true;


    BufferedImage img;
    static BufferedImage[] disappearFrames;

    public Wormholes(int x, int y, int destinationX, int destinationY, BufferedImage img) {
        bounds = new Rectangle(x, y, 100, 100);
        this.destinationX = destinationX;
        this.destinationY = destinationY;
        this.x = x;
        this.y = y;
        this.width = 100;
        this.height = 100;
        this.img = img;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int getDestinationX() {
        return destinationX;
    }

    public int getDestinationY() {
        return destinationY;
    }

    public void loadImage(){
        try {
            img = ImageIO.read(Objects.requireNonNull(Wormholes.class.getClassLoader().getResource("wormholes/wormhole.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //portal disappearing animation
    public static void loadWormholeAnimation(){
        if (disappearFrames != null) {
            return;
        }
        disappearFrames = new BufferedImage[4];

        try {
            for (int i = 0; i < disappearFrames.length; i++) {
                disappearFrames[i] = ImageIO.read(Objects.requireNonNull
                        (Wormholes.class.getClassLoader().getResource
                        ("wormholes/wormholedisappearing" + (i + 1) + ".png")));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * start the disappearing animation from the first frame
     */
    public void startAnimation(){
        disappearing = true;
        animationFrame = 0;
        animationCounter = 0;
    }

    public boolean animationIsFinished(){
        return disappearing && animationFrame >= disappearFrames.length;
    }

    public boolean isVisible() {
        return visible;
    }


    public void draw(Graphics g) {
        //don't draw anything after the animation is finished
        if (!visible) return;

        if (disappearing) {
            //check that we don't access an invalid frame
            if (animationFrame < disappearFrames.length) {
                g.drawImage(disappearFrames[animationFrame], x, y, width, height, null);
            }
        } else {
            //draw regular portal
            g.drawImage(img, x, y, width, height, null);
        }
    }

    /*
     * Advances the disappearing animations
     */
    public void update() {
        if (!disappearing) {
            return;
        }

        animationCounter++;

        if (animationCounter >=  animationSpeed) {
            animationCounter = 0;
            animationFrame++;

            if (animationFrame >= disappearFrames.length) {
                visible = false;
            }
        }
    }

    /*
     * moves wormhole to a new location
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;

        bounds.setLocation(x, y);
    }
}