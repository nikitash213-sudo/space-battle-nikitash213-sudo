package tankwars.managers;

import tankwars.game.Tank;
import tankwars.game.GameWorld;
import tankwars.objects.Lasers;
import tankwars.objects.Walls;
import tankwars.objects.Wormholes;
import tankwars.objects.PowerUps;

import java.awt.*;
import java.util.ArrayList;

public class CollisionManager {
/*
 * Check laser collision with walls. Walls that are breakable should take damage if a laser collides with it
 */
    public static void checkLaserCollision(ArrayList<Lasers> lasers, ArrayList<Walls> walls, ArrayList<PowerUps> powerUps) {
        lasers.removeIf(laser -> {
            for(Walls wall : walls) {
                if(laser.getBounds().intersects(wall.getBounds())) {
                    if(wall.isBreakable()) {
                        wall.takeDamage(10);
                    }
                    return true;
                }
            }
            return false;
        });

    }

    /*
     * Check wall collision with tanks. When a tank gets a meteor powerup, the breakable walls should break immediately.
     */

    public static boolean checkWallCollision(Tank tank, ArrayList<Walls> walls) {
        for (Walls wall : walls) {
            if(!tank.getBounds().intersects(wall.getBounds())) {
                continue;
            }
            if(tank.hasMeteor() && wall.isBreakable()) {
                wall.takeDamage(999);
                continue;
            }
            return true;
        }
        return false;
    }

    /*
     * Remove destroyed walls helper function. Powerups are hidden behind the asteroids right after the breaking animation
     * finishes.
     */
    public static void removeDestroyedWalls(ArrayList<Walls> walls, ArrayList<PowerUps> powerUps) {
        walls.removeIf(wall-> {
            if (wall.animationFinished()) {
                if (wall.getHiddenPowerUp() != null) {
                    powerUps.add(wall.revealPowerUp());
                }
                return true;
            }
            return false;
        });
    }

    /*
     * Check the tanks collision with the center of the portals/wormholes.
     */

    public static boolean checkPortalCollision(ArrayList<Wormholes> wormholes, Tank tank) {
        Rectangle rect = tank.getBounds();
        for (Wormholes w : wormholes) {
            if(w.getBounds().contains(rect.getCenterX(), rect.getCenterY())) {
                return true;
            }
        }
        return false;
    }

    /*
     * Check laser collision with spaceships. Keep track of the laser's owner to determine if the spaceship was hit by an
     * enemy laser and damage will be taken to them.
     */
    public static void checkLaserTankCollision(ArrayList<Lasers> lasers, Tank t1, Tank t2) {
        lasers.removeIf(laser -> {
        if (laser.getOwner() != t1 && laser.getBounds().intersects(t1.getBounds())) {
                t1.takeDamage(10);
                return true;
            }

        if (laser.getOwner() != t2 && laser.getBounds().intersects(t2.getBounds())){
            t2.takeDamage(10);
            return true;
        }
        return false;
        });

    }

    /*
     * Check if the tank has collected a powerup and apply the power up if it has
     */

    public static void checkPowerUpCollision(ArrayList<PowerUps> powerUps, Tank tank) {
        powerUps.removeIf(powerUp -> {
            if (tank.getBounds().intersects(powerUp.getBounds())) {
                powerUp.apply(tank);
                return true;
            }
            return false;
        });
    }
}
