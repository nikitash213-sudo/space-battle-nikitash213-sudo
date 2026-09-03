package tankwars.game;

import tankwars.objects.PowerUps;
import tankwars.objects.Walls;
import tankwars.objects.Wormholes;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;


public class GameMap {

    private ArrayList<Walls> walls = new ArrayList<>();
    private ArrayList<Wormholes> wormholes = new ArrayList<>();
    private BufferedImage background;
    private BufferedImage wall1;
    private BufferedImage wall2;
    private BufferedImage wormhole1;
    private static int TILE_SIZE = 40;
    private BufferedImage shield;
    private BufferedImage health;
    private BufferedImage meteor;


    public GameMap(int mapNumber) {
        walls = new ArrayList<>();
        loadWalls();
        loadPowerUps();
        loadWormholes();
        loadMap("maps/map" + mapNumber + ".csv");
        loadBackground("maps/background" + mapNumber + ".png");
    }
    private void loadMap(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
                    getClass().getClassLoader().getResourceAsStream(fileName)
            )));
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1);

                for (int col = 0; col < values.length; col++) {
                    int value;
                    try {
                        value = Integer.parseInt(values[col].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Bad value found");
                        System.out.println("File: " + fileName);
                        System.out.println("Row: " + row);
                        System.out.println("Col: " + col);
                        System.out.println("Value: " + values[col] + "'");
                        throw e;
                    }

                    int x = col * TILE_SIZE;
                    int y = row * TILE_SIZE;

                    switch(value) {
                        case 1:
                            walls.add(new Walls(x, y, TILE_SIZE, TILE_SIZE, false, wall1, null));
                            break;
                        case 2:
                            PowerUps powerUp = null;
                            if (Math.random() < 0.3) {
                                int chance = (int) (Math.random() * 3);
                                if (chance == 0) {
                                    powerUp = new PowerUps(x + 20, y + 20, health, "heal");
                                } else if (chance == 1) {
                                    powerUp = new PowerUps(x + 20, y + 20, shield, "shield");
                                } else if (chance == 2) {
                                    powerUp = new PowerUps(x, y, meteor, "meteor");
                                }
                            }
                                walls.add(new Walls(x, y, 90, 90, true, wall2, powerUp));
                                break;

                                case 3:
                                    wormholes.add(new Wormholes(x, y, 300, 200, wormhole1));
                                    break;
                            }

                }
                row++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBackground(String fileName) {
        try {
            BufferedImage original = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)));
            background = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = background.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(original, 0, 0, original.getWidth(), original.getHeight(), null);
            g2d.dispose();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadWalls() {
        try {
            wall1 = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("walls/wall1.png")));

            wall2 = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("walls/wall2.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadWormholes() {
        try {
            wormhole1 = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("wormholes/wormhole.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPowerUps() {
        try{
            health = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("powerups/powerup1.png")));
            shield = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("powerups/powerup2.png")));
            meteor = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("powerups/powerup3.png")));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getBackground() {
        return background;
    }

    public ArrayList<Walls> getWalls() {
        return walls;
    }

    public ArrayList<Wormholes> getWormholes() {
        return wormholes;
    }

    public void draw(Graphics g) {
        for (Walls w : walls) {
            w.draw(g);
        }
    }
}
