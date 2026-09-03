package tankwars.menus;

import tankwars.GameState;
import tankwars.Launcher;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ControlsScreen extends JPanel {
    private BufferedImage controlsImage;

    public ControlsScreen(Launcher lf) {
        try {
            controlsImage = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("menu/controls.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    lf.setFrame(GameState.GAME);
                }
            }
        });
    }

    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (controlsImage != null) {
            g.drawImage(controlsImage, 0, 0, getWidth(), getHeight(), this);
        }
    }


}
