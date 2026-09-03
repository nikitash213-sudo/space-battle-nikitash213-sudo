package tankwars;

import tankwars.game.GameWorld;
import tankwars.managers.SoundManager;
import tankwars.menus.ControlsScreen;
import tankwars.menus.EndGamePanel;
import tankwars.menus.StartMenuPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public class Launcher {

    /*
     * Main panel in JFrame, the layout of this panel
     * will be card layout, this will allow us to switch
     * to sub-panels depending on game state.
     */
    private JPanel mainPanel;
    /*
     * game panel is used to show our game to the screen. inside this panel
     * also contains the game loop. This is where our objects are updated and
     * redrawn. This panel will execute its game loop on a separate thread.
     * This is to ensure responsiveness of the GUI. It is also a bad practice to
     * run long running loops(or tasks) on Java Swing's main thread. This thread is
     * called the event dispatch thread.
     */
    private GameWorld gamePanel;
    /*
     * The thread currently running the game loop, if any. Tracked so that
     * showing the game screen twice cannot start a second loop updating the
     * same world.
     */
    private Thread gameThread;
    /*
     * JFrame used to store our main panel. We will also attach all event
     * listeners to this JFrame.
     */
    private final JFrame jf;
    /*
     * CardLayout is used to manage our sub-panels. This is a layout manager
     * used for our game. It will be attached to the main panel.
     */


    private CardLayout cl;

    public Launcher() {
        this.jf = new JFrame();
        this.jf.setTitle("Space Battle Game");
        // when the GUI is closed, this will also shut down the VM
        this.jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initUIComponents() {
        this.mainPanel = new JPanel();
        /*
         * start panel is used to view the start menu. It contains
         * two buttons: start and exit.
         */
        JPanel startPanel = new StartMenuPanel(this);
        this.gamePanel = new GameWorld(this);
        this.gamePanel.initializeGame(); // initialize game, but DO NOT start game
        this.gamePanel.resetGame();

        //Controls panel to display the controls screen before the game starts.
        JPanel controlsPanel = new ControlsScreen(this);

        /*
         * end panel is used to show the end game panel. It contains
         * two buttons: restart and exit.
         */
        JPanel endPanel = new EndGamePanel(this);
        this.cl = new CardLayout();
        this.mainPanel.setLayout(cl);
        this.mainPanel.add(startPanel, GameState.START.name());
        this.mainPanel.add(controlsPanel, GameState.CONTROLS.name());
        this.mainPanel.add(gamePanel, GameState.GAME.name());
        this.mainPanel.add(endPanel, GameState.END.name());
        this.jf.add(mainPanel);
        this.jf.setResizable(false);
        this.setFrame(GameState.START);
        SoundManager.playMusic("music.wav", -20.0f);
    }

    public void setFrame(GameState state) {
        this.jf.setVisible(false);
        /*
         * Size the content area (the displau panel) rather than the window: pack() adds
         * the title bar and borders on top, so the full panel stays visible.
         * setSize() on the JFrame would include the decorations and cut off the
         * bottom/right of the panel.
         */
        this.mainPanel.setPreferredSize(state.getPanelSize());
        this.jf.pack();
        if (state == GameState.GAME && (this.gameThread == null || !this.gameThread.isAlive())) {
            /*
             * start a new thread for the game to run. This will ensure our JFrame is
             * responsive and not stuck executing the game loop.
             */
            this.gameThread = new Thread(this.gamePanel);
            this.gameThread.start();
        }
        this.jf.setLocationRelativeTo(null); // center the window on the screen
        this.cl.show(mainPanel, state.name());
        this.jf.setVisible(true);
    }

    public JFrame getJf() {
        return jf;
    }

    public GameWorld getGamePanel() {
        return gamePanel;
    }

    public void closeGame() {
        this.jf.dispatchEvent(new WindowEvent(this.jf, WindowEvent.WINDOW_CLOSING));
    }

    public static void main(String[] args) {
        // build and show the GUI on the event dispatch thread, per Swing threading
        // rules
        SwingUtilities.invokeLater(() -> (new Launcher()).initUIComponents());
    }
}
