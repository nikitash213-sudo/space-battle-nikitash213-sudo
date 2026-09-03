package tankwars.game;

import tankwars.GameConstants;
import tankwars.managers.CollisionManager;
import tankwars.managers.SoundManager;
import tankwars.objects.Lasers;
import tankwars.objects.Walls;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;

/**
 * @author anthony-pc
 */
public class Tank {

    private static final float MOVE_SPEED = 5.0f;
    private static final float ROTATION_SPEED = 3.0f;
    private static final long SHOOT_DELAY = 300;
    private static final int WIDTH = 90;
    private static final int HEIGHT = 90;
    private final BufferedImage[] thrusterImage;
    private final BufferedImage[] otherThrusterImage;
    private int currentThrusterFrame = 0;
    private int currentMeteorFrame = 0;
    private int meteorFrameCounter = 0;
    private int thrusterFrameCounter = 0;
    private long lastFrameTime = 0;
    private static final int THRUSTER_FRAME_DELAY = 5;


    private float x;
    private float y;
    private float vx;
    private float vy;
    private float angle;
    private final float spawnX;
    private final float spawnY;

    private int health = 100;
    private int lives = 3;
    private boolean alive;

    private long lastShotTime = 0;
    private int damage = 10;

    private float moveSpeed = 5.0f;

    private long powerUpEndTime;

    private boolean shield;
    private boolean meteor;

    private boolean inPortal = false;

    private final BufferedImage img;
    private final BufferedImage laserImage;
    /*
     * The actions currently held down for this tank. EnumSet is a compact,
     * fast Set implementation specialized for enum elements (internally a bit
     * vector, so contains/add/remove are constant time).
     */
    private final EnumSet<PlayerAction> activeActions = EnumSet.noneOf(PlayerAction.class);

    Tank(float x, float y, float vx, float vy, float angle, BufferedImage img, BufferedImage laserImage
         ,BufferedImage[] thrusterImage, BufferedImage[] otherThrusterImage) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.spawnX = x;
        this.spawnY = y;
        this.img = img;
        this.angle = angle;
        this.laserImage = laserImage;
        this.thrusterImage = thrusterImage;
        this.otherThrusterImage = otherThrusterImage;
    }

    void setX(float x) {
        this.x = x;
    }

    void setY(float y) {
        this.y = y;
    }

    void press(PlayerAction action) {
        this.activeActions.add(action);
    }

    void release(PlayerAction action) {
        this.activeActions.remove(action);
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x + 20, (int)y + 20, 60, 60);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getAngle() {
        return angle;
    }

    void update(ArrayList<Walls> walls) {

        updateMeteor();

        updateThrusterAnimation();
        updateMeteorAnimation();

        float oldX = x;
        float oldY = y;

        if (this.activeActions.contains(PlayerAction.UP)) {
            this.moveForwards();
        }

        if (this.activeActions.contains(PlayerAction.DOWN)) {
            this.moveBackwards();
        }

        if (this.activeActions.contains(PlayerAction.LEFT)) {
            this.rotateLeft();
        }

        if (this.activeActions.contains(PlayerAction.RIGHT)) {
            this.rotateRight();
        }

       if (CollisionManager.checkWallCollision(this, walls)) {
           x = oldX;
           y = oldY;
       }
    }

    private void rotateLeft() {
        this.angle -= ROTATION_SPEED;
    }

    private void rotateRight() {
        this.angle += ROTATION_SPEED;
    }

    private void moveForwards() {
        move(1);
    }

    private void moveBackwards() {
        move(-1);
    }

    private void move(int direction) {
        float movementAngle = angle - 90;
        vx = Math.round(MOVE_SPEED * Math.cos(Math.toRadians(movementAngle)));
        vy = Math.round(MOVE_SPEED * Math.sin(Math.toRadians(movementAngle)));
        x += direction * vx;
        y += direction * vy;
        checkBorder();
    }

    /**
     * Keep the whole tank image inside the game world.
     */
    private void checkBorder() {
        x = Math.clamp(x, 0, GameConstants.GAME_SCREEN_WIDTH - WIDTH);
        y = Math.clamp(y, 0, GameConstants.GAME_SCREEN_HEIGHT - HEIGHT);
    }

    public void takeDamage(int amount) {

        if (shield) {
            shield = false;
            return;
        }

        health -= amount;

        if (health <= 0) {
            lives--;

            if (lives > 0) {
                respawn();
            }
        }
    }



    public boolean isShooting() {
        return activeActions.contains(PlayerAction.SHOOT);
    }

    public boolean isDead() {
        return lives <= 0;
    }

    public ArrayList<Lasers> shoot() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastShotTime < SHOOT_DELAY) {
            return null;
        }

        lastShotTime = currentTime;

       SoundManager.playSound("laser.wav", -20.0f);

        ArrayList<Lasers> shots = new ArrayList<>();
        double radians = Math.toRadians(angle - 90);

        float forwardX = (float)Math.cos(radians);
        float forwardY = (float)Math.sin(radians);

        float sideX = (float)Math.cos(radians + Math.PI/2);
        float sideY = (float)Math.sin(radians + Math.PI/2);

        float centerX = x + WIDTH / 2.0f - 30;
        float centerY = y + HEIGHT / 2.0f;

        float leftX = centerX - sideX * 8 + forwardX * 80;
        float leftY = centerY - sideY * 8 + forwardY * 80;
        float rightX = centerX + sideX * 8 + forwardX * 80;
        float rightY = centerY + sideY * 8 + forwardY * 80;

        shots.add(new Lasers(leftX, leftY, angle - 90, this, laserImage));
        shots.add(new Lasers(rightX, rightY, angle - 90, this, laserImage));

        return shots;
    }

    @Override
    public String toString() {
        return "x=" + x + ", y=" + y + ", angle=" + angle;
    }

    void drawImage(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        at.rotate(Math.toRadians(angle), WIDTH/2.0, HEIGHT/2.0);
        at.scale(WIDTH /(double) img.getWidth(), HEIGHT /(double) img.getHeight());

        if (activeActions.contains(PlayerAction.UP) || activeActions.contains(PlayerAction.DOWN)) {

            if (meteor) {
                drawMeteorFlame(g2d, 3, -15);
            } else {
                drawThruster(g2d, 10, -15);
            }
        }

        g2d.drawImage(img, at, null);


        if (shield) {
            Graphics2D g2d2 = (Graphics2D) g.create();
            g2d2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            g2d2.setColor(Color.CYAN);
            g2d2.fillOval((int)x - 15, (int)y - 15, WIDTH + 30, HEIGHT + 30);
            g2d2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
            g2d2.setColor(Color.CYAN);
            g2d2.setStroke(new BasicStroke(3));
            g2d2.drawOval((int)x-15, (int)y-15, WIDTH + 30, HEIGHT + 30);

            g2d2.dispose();
        }


    }

    public boolean collidesWith(Walls wall) {
        return wall.getBounds().intersects(this.getBounds());
    }

    public int getHealth() {
        return health;
    }

    public int getLives() {
        return lives;
    }

    public void respawn() {
        this.x = spawnX;
        this.y = spawnY;
        this.health = 100;
    }

    public void reset() {
        this.x = spawnX;
        this.y = spawnY;
        this.health = 100;
        this.lives = 3;
        this.activeActions.clear();

        this.shield = false;
        this.meteor = false;

        this.currentThrusterFrame = 0;
        this.currentMeteorFrame = 0;
    }

    public void heal(int amount) {
        health += amount;

        if (health > 100) {
            health = 100;
        }
    }

    public void activatePowerUp(String type) {
    SoundManager.playSound("powerups.wav", -30.0f);
        switch (type) {
            case "heal":
                health = Math.min(100, health + 30);
                break;
            case "shield":
                shield = true;
                break;
            case "meteor":
                activateMeteor();
                break;
        }
    }

    public void activateShield() {
        shield = true;
    }

    public boolean hasShield() {
        return shield;
    }

    public void activateMeteor() {
        meteor = true;
        powerUpEndTime = System.currentTimeMillis() + 10000;
    }
    private void updateMeteor() {
        if(meteor && System.currentTimeMillis() > powerUpEndTime) {
            meteor = false;
        }
    }
    public boolean hasMeteor() {
        return meteor;

    }

    public void consumeMeteor() {
        meteor = false;
    }

    private void drawThruster(Graphics2D g2d, double localX, double localY) {
        AffineTransform at = new AffineTransform();
        //move to the spaceship's position
        at.translate(x,y);
        //rotate with the ship
        at.rotate(Math.toRadians(angle), WIDTH/2.0, HEIGHT/2.0);
        //move to the engine location
        at.translate(localX, localY);
        //resize flames
        at.scale(65.0 / thrusterImage[currentThrusterFrame].getWidth(), 120.0 / thrusterImage[currentThrusterFrame].getHeight());
        //draw the current animation frame
        g2d.drawImage(thrusterImage[currentThrusterFrame], at, null);
    }

    private void drawMeteorFlame(Graphics2D g2d, double localX, double localY) {
        AffineTransform at = new AffineTransform();
        at.translate(x,y);
        at.rotate(Math.toRadians(angle), WIDTH/2.0, HEIGHT/2.0);
        at.translate(localX, localY);
        at.scale(85.0/ otherThrusterImage[currentMeteorFrame].getWidth(), 110.0/otherThrusterImage[currentMeteorFrame].getHeight());
        g2d.drawImage(otherThrusterImage[currentMeteorFrame], at, null);
    }

    private void updateMeteorAnimation() {
        if(!meteor) {
            currentMeteorFrame = 0;
            currentThrusterFrame = 0;
            return;
        }
        meteorFrameCounter++;
        if (meteorFrameCounter >= THRUSTER_FRAME_DELAY) {
            meteorFrameCounter = 0;
            currentMeteorFrame++;
            if (currentMeteorFrame >= otherThrusterImage.length){
                currentMeteorFrame = 0;
            }
        }
    }



    private void updateThrusterAnimation() {
        boolean thrusting  = activeActions.contains(PlayerAction.UP) || activeActions.contains(PlayerAction.DOWN);
        if (!thrusting) {
            currentThrusterFrame = 0;
            thrusterFrameCounter = 0;
            return;
        }
        thrusterFrameCounter++;
        if (thrusterFrameCounter >= THRUSTER_FRAME_DELAY) {
            thrusterFrameCounter = 0;
            currentThrusterFrame++;
            if (currentThrusterFrame >= thrusterImage.length) {
                currentThrusterFrame = 0;
            }
        }
    }


    public boolean isInPortal() {
        return inPortal;
    }

    public void setInPortal(boolean inPortal) {
        this.inPortal = inPortal;
    }
}
