package tankwars.menus;

import tankwars.GameConstants;
import tankwars.GameState;
import tankwars.Launcher;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class EndGamePanel extends JPanel {

    private BufferedImage menuBackground;
    private final Launcher lf;


    public EndGamePanel(Launcher lf) {
        this.lf = lf;
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

        JButton restart = new JButton("Restart Game");
        restart.setFont(new Font("Courier New", Font.BOLD, 24));
        restart.setBounds(120, 300, 250, 50);
        restart.addActionListener(actionEvent -> {lf.getGamePanel().resetGame();
        lf.setFrame(GameState.GAME); });

        JButton exit = new JButton("Exit");
        exit.setFont(new Font("Courier New", Font.BOLD, 24));
        exit.setBounds(120, 400, 250, 50);
        exit.addActionListener(actionEvent -> lf.closeGame());

        this.add(restart);
        this.add(exit);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(this.menuBackground, 0, 0, null);
        String winner = lf.getGamePanel().getWinner();
        g.setFont(new Font("Courier New", Font.BOLD, 48));
        if (winner.equals("Player 1")){
            g.setColor(Color.BLUE);
        }
        else{
            g.setColor(Color.RED);
        }

        String text = winner + " Wins!";

        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = 60;
        g.drawString(text, x, y);
    }
}
