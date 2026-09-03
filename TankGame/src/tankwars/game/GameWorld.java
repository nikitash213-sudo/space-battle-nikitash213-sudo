package tankwars.game;

import tankwars.GameConstants;
import tankwars.GameState;
import tankwars.Launcher;
import tankwars.managers.CollisionManager;
import tankwars.managers.SoundManager;
import tankwars.objects.Lasers;
import tankwars.objects.PowerUps;
import tankwars.objects.Walls;
import tankwars.objects.Wormholes;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author anthony-pc
 */
public class GameWorld extends JPanel implements Runnable {

    private static final int UPDATES_PER_SECOND = 144;
    private static final long TIME_STEP_NS = 1_000_000_000L / UPDATES_PER_SECOND;
    /*
     * Upper bound on how much real time one pass of the loop is allowed to
     * consume. Without it, a long stall (breakpoint, OS hiccup) would make the
     * accumulator huge and trigger a "spiral of death": the loop runs so many
     * catch-up updates that it falls even further behind.
     */
    private static final long MAX_FRAME_TIME_NS = 250_000_000L; // 250 ms

    private BufferedImage world;
    private BufferedImage laserImage;
    private BufferedImage laserImage2;
    private BufferedImage lifeIcon;
    private BufferedImage lifeIcon2;
    private BufferedImage[] blueThrusterFlames;
    private BufferedImage[] redThrusterFlames;
    private BufferedImage[] orangeThrusterFlames;

    private Tank t1;
    private Tank t2;
    private ArrayList<Lasers> lasers;
    private final Launcher lf;
    private long tick = 0;
    private String winner;
    private GameMap currentMap;
    private int mapNumber = 1;
    private Camera camera1 = new Camera(GameConstants.GAME_SCREEN_WIDTH/2, GameConstants.GAME_SCREEN_HEIGHT);
    private Camera camera2 = new Camera(GameConstants.GAME_SCREEN_WIDTH/2, GameConstants.GAME_SCREEN_HEIGHT);
    private boolean changingMap = false;
    private ArrayList<PowerUps> powerUps = new ArrayList<>();
    private Wormholes spawnPortal;

    public GameWorld(Launcher lf) {
        this.lf = lf;
    }

    /*
     * Real elapsed time is added to an accumulator, and the simulation is
     * advanced in fixed TIME_STEP_NS increments until it has caught up with
     * real time. This decouples the simulation rate from the render rate:
     * updates happen exactly UPDATES_PER_SECOND times per second of real time,
     * no matter how fast or slow rendering is, which keeps game speed
     * consistent across machines and keeps the simulation deterministic.
     */
    @Override
    public void run() {
        long previous = System.nanoTime();
        long accumulator = 0;
        try {
            while (true) {
                long now = System.nanoTime();
                long frameTime = Math.min(now - previous, MAX_FRAME_TIME_NS);
                previous = now;
                accumulator += frameTime;

                // advance the simulation in fixed steps until caught up with real time
                while (accumulator >= TIME_STEP_NS) {
                    this.tick++;

                    this.t1.update(currentMap.getWalls());
                    this.t2.update(currentMap.getWalls());

                    for (Walls w : currentMap.getWalls()) {
                        w.update();
                    }

                    for (Wormholes w : currentMap.getWormholes()) {
                        w.update();
                    }
                    if (spawnPortal != null) {
                        spawnPortal.update();
                        if (spawnPortal.animationIsFinished()) {
                            spawnPortal = null;
                        }
                    }

                    boolean t1TouchingPortal = CollisionManager.checkPortalCollision(currentMap.getWormholes(), t1);
                    boolean t2TouchingPortal = CollisionManager.checkPortalCollision(currentMap.getWormholes(), t2);

                    if(!changingMap && ((t1TouchingPortal && !t1.isInPortal())) || (t2TouchingPortal && !t2.isInPortal())){
                        SoundManager.playSound("portal.wav", -15.0f);
                        changingMap = true;
                        changeMap(mapNumber + 1);

                        //temporary portal at the spawn location
                        Wormholes.loadWormholeAnimation();
                        spawnPortal = new Wormholes((int)t1.getX(), (int)t1.getY(),0, 0, null);
                        spawnPortal.startAnimation();

                        //prevent immediately teleporting
                        t1.setInPortal(true);
                        t2.setInPortal(true);

                        changingMap = false;
                    }

                    //update spaceship's portal state
                    t1.setInPortal(t1TouchingPortal);
                    t2.setInPortal(t2TouchingPortal);


                    // update tank
                    if (this.t1.isShooting()) {
                        ArrayList<Lasers> shots = this.t1.shoot();

                        if(shots != null) {
                            this.lasers.addAll(shots);
                        }
                    }

                    if (this.t2.isShooting()) {
                        ArrayList<Lasers> shots = this.t2.shoot();
                        if(shots != null) {
                            this.lasers.addAll(shots);
                        }
                    }

                    for (Lasers laser : this.lasers) {
                        laser.update();
                    }

                    CollisionManager.checkLaserCollision(lasers, currentMap.getWalls(), powerUps);
                    CollisionManager.checkLaserTankCollision(lasers, t1, t2);
                    CollisionManager.removeDestroyedWalls(currentMap.getWalls(), powerUps);
                    CollisionManager.checkPowerUpCollision(powerUps, t1);
                    CollisionManager.checkPowerUpCollision(powerUps, t2);

                    if (t1.isDead()) {
                        winner = "Player 2";
                        lf.setFrame(GameState.END);
                        return;
                    }

                    if (t2.isDead()) {
                        winner = "Player 1";
                        lf.setFrame(GameState.END);
                        return;
                    }

                    lasers.removeIf(Lasers::isOutOfBounds);
                    accumulator -= TIME_STEP_NS;

                    if (t1.getLives() <= 0) {
                        lf.setFrame(GameState.END);
                        return;
                    }
                    if (t2.getLives() <= 0) {
                        lf.setFrame(GameState.END);
                        return;
                    }
                }

                this.repaint(); // redraw game

                // sleep until the next simulation step is due, to avoid busy-waiting
                long sleepNs = TIME_STEP_NS - accumulator;
                if (sleepNs > 0) {
                    Thread.sleep(sleepNs / 1_000_000, (int) (sleepNs % 1_000_000));
                }
            }
        } catch (InterruptedException e) {
            // another thread asked the game loop to stop; exit run() to end it
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Reset game to its initial state.
     */
    public void resetGame() {
        this.tick = 0;
        this.t1.reset();
        this.t2.reset();
        this.lasers.clear();
        this.powerUps.clear();
        mapNumber = 1;
        winner = null;
        currentMap = new GameMap(mapNumber);
        setSpawnLocations();
    }

    /**
     * Load all resources for Tank Wars Game. Set all Game Objects to their
     * initial state as well.
     */
    public void initializeGame() {
        this.world = new BufferedImage(GameConstants.GAME_SCREEN_WIDTH,
                GameConstants.GAME_SCREEN_HEIGHT,
                BufferedImage.TYPE_INT_RGB);

        BufferedImage t1img = null;
        BufferedImage t2img = null;

        lasers = new ArrayList<>();


        try {
            /*
             * note class loaders read files from the out folder (build folder in Netbeans) and not the
             * current working directory. When running a jar, class loaders will read from within the jar.
             */
            BufferedImage original  = ImageIO.read(
                    Objects.requireNonNull(GameWorld.class.getClassLoader().getResource("tanks/tank1.png"),
                            "Could not find tanks/tank1.png")
            );
            BufferedImage tankImage = new BufferedImage(100,100, BufferedImage.TYPE_INT_ARGB);

            BufferedImage original2 = ImageIO.read(Objects.requireNonNull(GameWorld.class.getClassLoader().getResource("tanks/tank2.png"),
                    "Could not find tanks/tank2.png"));

            BufferedImage tankImage2 = new BufferedImage(100,100, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = tankImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(original, 0, 0, tankImage.getWidth(), tankImage.getHeight(), null);
            g2d.dispose();
            t1img = tankImage;

            Graphics2D g2d2 = tankImage2.createGraphics();
            g2d2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d2.drawImage(original2, 0, 0, tankImage2.getWidth(), tankImage2.getHeight(), null);
            g2d2.dispose();
            t2img = tankImage2;

            lifeIcon = ImageIO.read(Objects.requireNonNull(GameWorld.class.getClassLoader().getResource("maps/life.png")));
            lifeIcon2 = ImageIO.read(Objects.requireNonNull(GameWorld.class.getClassLoader().getResource("maps/life2.png")));


            BufferedImage originalLaser = ImageIO.read(Objects.requireNonNull(GameWorld.class.getClassLoader().getResource("lasers/laser1.png")));
            laserImage = new BufferedImage(32, 8, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = laserImage.createGraphics();
            g2.drawImage(originalLaser, 0, 0, 32, 8, null);
            g2.dispose();

            BufferedImage originalLaser2 = ImageIO.read(Objects.requireNonNull(GameWorld.class.getClassLoader().getResource("lasers/laser2.png")));
            laserImage2 = new BufferedImage(32, 8, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g22 = laserImage2.createGraphics();
            g22.drawImage(originalLaser2, 0, 0, 32, 8, null);
            g22.dispose();

            blueThrusterFlames = loadAnimation("thrusters/blue", 4);
            redThrusterFlames = loadAnimation("thrusters/red", 4);
            orangeThrusterFlames = loadAnimation("thrusters/orange", 8);



        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

        t1 = new Tank(300, 300, 0, 0, 0, t1img, laserImage2, blueThrusterFlames, orangeThrusterFlames);
        t2 = new Tank(700, 300, 0, 0, 0, t2img, laserImage, redThrusterFlames, orangeThrusterFlames);

       changeMap(1);

        TankControl tc1 = new TankControl(t1, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE);
        TankControl tc2 = new TankControl(t2, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER);
        this.lf.getJf().addKeyListener(tc1);
        this.lf.getJf().addKeyListener(tc2);
    }

    private void setSpawnLocations() {
        switch (mapNumber) {
            case 1:
                t1.setX(100);
                t1.setY(100);
                t2.setX(850);
                t2.setY(500);
                break;
            case 2:
                t1.setX(150);
                t1.setY(450);
                t2.setX(800);
                t2.setY(100);
                break;
            case 3:
                t1.setX(100);
                t1.setY(300);
                t2.setX(850);
                t2.setY(300);
                break;
        }
    }

    private void changeMap(int newMap) {
        mapNumber = newMap;
        if (mapNumber > 3) {
            mapNumber = 1;
        }
        if (mapNumber < 1) {
            mapNumber = 3;
        }
        currentMap = new GameMap(mapNumber);
        lasers.clear();
        powerUps.clear();
        setSpawnLocations();
    }

    private void drawCamera(Graphics2D g, Camera cam) {
        //move the graphics view based on the camera position
        //camera follows the tank instead of drawing the entire world
        g.translate(-cam.getX(), -cam.getY());
        //draw the map background
        g.drawImage(currentMap.getBackground(), 0, 0, null);

        //create a copy before drawing
        //game update thread can remove destroyed asteroids/walls while the paint thread is drawing them
        //changing an ArrayList while looping through causes a ConcurrentModificationException
        //a copy gives the drawing code a safe snapshot of the list
        ArrayList<Walls> wallCopy = new ArrayList<>(currentMap.getWalls());

        //draw everything else on top of the background
        for (Walls wall : wallCopy) {
            wall.draw(g);
        }

        //create a copy of the powerup list, powerups can be removed when a spaceship collects them while the screen
        //is being drawn
        ArrayList<PowerUps> powerUpCopy = new ArrayList<>(powerUps);

        //draw all revealed powerups
        for (PowerUps powerUp : powerUpCopy) {
            powerUp.draw(g);
        }

        //lasers are constantly being added when spaceships shoot and removed when they hit something
        ArrayList<Lasers> lasersCopy = new ArrayList<>(lasers);

        //draw all active lasers
        for (Lasers laser : lasersCopy) {
            laser.draw(g);
        }

        for (Wormholes wormhole : currentMap.getWormholes()) {
            wormhole.draw(g);
        }
        if (spawnPortal != null) {
            spawnPortal.draw(g);
        }

        t1.drawImage(g);
        t2.drawImage(g);

        //releases the graphics object after drawing is finished
        g.dispose();
    }

    private void drawMiniMap (Graphics2D g) {
        int miniX = GameConstants.GAME_SCREEN_WIDTH - 220;
        int miniY = 20;
        int miniWidth = 200;
        int miniHeight = 150;

        //background for the mini map
        g.setColor(Color.BLACK);
        g.fillRect(miniX, miniY, miniWidth, miniHeight);

        //scale
        double scaleX = (double) miniWidth / GameConstants.GAME_SCREEN_WIDTH;
        double scaleY = (double) miniHeight / GameConstants.GAME_SCREEN_HEIGHT;

        //draw player 1
        g.setColor(Color.CYAN);
        g.fillOval(miniX + (int)(t1.getX() * scaleX), miniY + (int)(t1.getY() * scaleY), 8, 8);

        //draw player2
        g.setColor(Color.RED);
        g.fillOval(miniX + (int)(t2.getX() * scaleX), miniY + (int)(t2.getY() * scaleY), 8, 8);

        //border
        g.setColor(Color.WHITE);
        g.drawRect(miniX, miniY, miniWidth, miniHeight);
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Graphics2D buffer = world.createGraphics();
        // clear the previous frame before drawing the new one
        buffer.setColor(Color.BLACK);
        buffer.fillRect(0, 0, GameConstants.GAME_SCREEN_WIDTH, GameConstants.GAME_SCREEN_HEIGHT);

        int halfWidth = GameConstants.GAME_SCREEN_WIDTH / 2;
        int height = GameConstants.GAME_SCREEN_HEIGHT;

        camera1.follow(t1);
        camera2.follow(t2);

        drawCamera((Graphics2D) buffer.create(0,0,halfWidth,height), camera1);
        drawCamera((Graphics2D)  buffer.create(halfWidth,0,halfWidth,height), camera2);

        buffer.setColor(Color.WHITE);
        buffer.fillRect(halfWidth - 1, 0, 2, height);

        //player 1 health bar
        buffer.setColor(Color.RED);
        buffer.fillRect(50,30,200,20);
        buffer.setColor(Color.GREEN);
        buffer.fillRect(50,30, t1.getHealth() * 2, 20);
        //player 2 health bar
        buffer.setColor(Color.RED);
        buffer.fillRect(650,30,200, 20);
        buffer.setColor(Color.GREEN);
        buffer.fillRect(650,30, t2.getHealth() *2, 20);
        //lives
        for (int i = 0; i < t1.getLives(); i++) {
            buffer.drawImage(lifeIcon2, 50 + (i *35), 60, 30, 20, null);
        }
        for (int i = 0; i < t2.getLives(); i++) {
            buffer.drawImage(lifeIcon, 650 + (i *35), 60, 30, 20, null);
        }

        drawMiniMap(buffer);
        buffer.dispose();
        g2.drawImage(world, 0, 0, null);
    }
    public String getWinner() {
        return winner;
    }

    private BufferedImage[] loadAnimation(String folder, int frameCount) throws IOException {
        BufferedImage[] images = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            images[i] = ImageIO.read(Objects.requireNonNull(GameWorld.class.getClassLoader().getResource(folder + "/thruster" + i + ".png")));
        }
        return images;
    }

}
