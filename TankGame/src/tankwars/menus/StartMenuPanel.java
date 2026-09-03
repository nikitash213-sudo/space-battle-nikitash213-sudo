package tankwars.menus;

import tankwars.GameState;
import tankwars.Launcher;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class StartMenuPanel extends JPanel {

    private BufferedImage menuBackground;

    public StartMenuPanel(Launcher lf) {
        try {
            menuBackground = ImageIO.read(
                    Objects.requireNonNull(this.getClass().getClassLoader().getResource("menu/title.png"),
                            "Could not find menu/title.png")
            );
        } catch (IOException e) {
            System.out.println("Error: cannot read menu background");
            e.printStackTrace();
            System.exit(-3);
        }
        this.setBackground(Color.BLACK);
        this.setLayout(null);

        JButton start = new JButton("Start");
        start.setFont(new Font("Courier New", Font.BOLD, 24));
        start.setBounds(160, 300, 150, 50);
        start.addActionListener(actionEvent -> lf.setFrame(GameState.CONTROLS));

        JButton exit = new JButton("Exit");
        exit.setFont(new Font("Courier New", Font.BOLD, 24));
        exit.setBounds(160, 400, 150, 50);
        exit.addActionListener(actionEvent -> lf.closeGame());

        this.add(start);
        this.add(exit);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(this.menuBackground, 0, 0, null);
    }
}
