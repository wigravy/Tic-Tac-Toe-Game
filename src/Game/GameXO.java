package Game;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class GameXO extends JFrame {
    private final int SIZE = 3;
    private JButton[][] map = new JButton[SIZE][SIZE];
    private final ImageIcon DOT_X_IMG = new ImageIcon(this.getClass().getResource("res\\X.png"));
    private final ImageIcon DOT_O_IMG = new ImageIcon(this.getClass().getResource("res\\O.png"));
    private final ImageIcon EMPTY = new ImageIcon(this.getClass().getResource("res\\empty2.jpg"));
    private final ImageIcon ICON = new ImageIcon(this.getClass().getResource("res\\icon.png"));
    private int playerTurn = 0;
    private File soundWin = new File("src\\Game\\res\\WinSound.wav");
    private File soundLose = new File("src\\Game\\res\\LoseSound.wav");


    private JMenu createFileMenu() {
        JMenu file = new JMenu("Файл");
        JMenuItem newGame = new JMenuItem("Новая игра");
        JMenuItem exit = new JMenuItem("Выход");
        exit.addActionListener((actionEvent) -> System.exit(0));
        newGame.addActionListener((actionEvent) -> this.newGame());
        file.add(newGame);
        file.addSeparator();
        file.add(exit);
        return file;
    }

    private void newGame() {
        this.dispose();
        new GameXO();
    }

    private void playSoundOnClick(File sound) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioInputStream inAudio = AudioSystem.getAudioInputStream(sound);
        Clip clip = AudioSystem.getClip();
        clip.open(inAudio);
        clip.setFramePosition(0);
        clip.start();
    }

    private JPanel gameField() {
        JPanel panel = new JPanel(new GridLayout(SIZE, SIZE));

        for(int i = 0; i < SIZE; ++i) {
            for(int j = 0; j < SIZE; ++j) {
                this.map[i][j] = new JButton();
                this.map[i][j].setIcon(this.EMPTY);
                JButton currentCell = this.map[i][j];
                currentCell.addActionListener((ActionEvent) -> {
                    currentCell.setDisabledIcon(this.DOT_X_IMG);
                    currentCell.setEnabled(false);
                    try {
                        this.checkGame();
                    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                        e.printStackTrace();
                    }
                });
                panel.add(this.map[i][j]);
            }
        }
        return panel;
    }

    private void aiTurn() {
        boolean aiwin = true;
        boolean userwin = true;
        if (this.playerTurn > 1) {
            for(int i = 0; i < SIZE; ++i) {
                for(int j = 0; j < SIZE; ++j) {
                    if (this.map[i][j].isEnabled()) {
                        this.map[i][j].setEnabled(false);
                        this.map[i][j].setDisabledIcon(this.DOT_O_IMG);
                        if (this.checkWin(this.DOT_O_IMG, this.map)) {
                            return;
                        }

                        this.map[i][j].setEnabled(true);
                        this.map[i][j].setDisabledIcon(null);
                    }
                }
            }
            aiwin = false;
        }

        if (!aiwin) {
            for(int i = 0; i < SIZE; ++i) {
                for(int j = 0; j < SIZE; ++j) {
                    if (this.map[i][j].isEnabled()) {
                        this.map[i][j].setEnabled(false);
                        this.map[i][j].setDisabledIcon(this.DOT_X_IMG);
                        if (this.checkWin(this.DOT_X_IMG, this.map)) {
                            this.map[i][j].setDisabledIcon(this.DOT_O_IMG);
                            return;
                        }

                        this.map[i][j].setEnabled(true);
                        this.map[i][j].setDisabledIcon(null);
                    }
                }
            }
            userwin = false;
        }

        if ((!aiwin && !userwin) || this.playerTurn < 2) {
            int x, y;
            if (this.map[1][1].isEnabled()) {
                this.map[1][1].setDisabledIcon(this.DOT_O_IMG);
                this.map[1][1].setEnabled(false);
            } else {
                do {
                    Random rand = new Random();
                    x = rand.nextInt(SIZE);
                    y = rand.nextInt(SIZE);
                } while (!this.map[x][y].isEnabled());
                this.map[x][y].setDisabledIcon(this.DOT_O_IMG);
                this.map[x][y].setEnabled(false);
            }
        }
    }

    private boolean checkWin(ImageIcon icon, JButton[][] map) {
        int countH = 0, countV = 0;
        for(int i = 0; i < SIZE; ++i) {
            countH = 0;
            countV = 0;
            for(int j = 0; j < SIZE; ++j) {
                if (map[i][j].getDisabledIcon() == icon) ++countH;
                if (map[j][i].getDisabledIcon() == icon) ++countV;
                if (countH == 3 || countV == 3) return true;
            }
        }

        int countD = 0, countRD = 0;
        for(int i = 0; i < 3; ++i) {
            if (map[i][i].getDisabledIcon() == icon) countD++;
            if (map[i][3 - i - 1].getDisabledIcon() == icon) ++countRD;
            if (countD == 3 || countRD == 3) return true;
        }
        return false;
    }

    private boolean isMapFull() {
        for(int i = 0; i < SIZE; ++i) {
            for(int j = 0; j < SIZE; ++j) {
                if (this.map[i][j].isEnabled()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void checkGame() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        ++this.playerTurn;
        if (this.checkWin(this.DOT_X_IMG, this.map)) {
            this.playSoundOnClick(this.soundWin);
            JOptionPane.showMessageDialog(this, "Победил кожаный мешок!", "GameOver! You win!", -1);
            this.newGame();
        } else if (this.isMapFull()) {
            JOptionPane.showMessageDialog(this, "Ничья", "GameOver! Field is full!", -1);
            this.newGame();
        } else {
            this.aiTurn();
            if (this.checkWin(this.DOT_O_IMG, this.map)) {
                this.playSoundOnClick(this.soundLose);
                JOptionPane.showMessageDialog(this, "Победил совершенный механизм!", "GameOver! You lose!", -1);
                this.newGame();
            } else if (this.isMapFull()) {
                JOptionPane.showMessageDialog(this, "Ничья", "GameOver! Field is full!", -1);
                this.newGame();
            }
        }

    }

    private GameXO() throws HeadlessException {
        this.setTitle("Tic Tac Toe Game");
        this.setIconImage(this.ICON.getImage());
        JMenuBar bar = new JMenuBar();
        bar.add(this.createFileMenu());
        this.setJMenuBar(bar);
        this.add(this.gameField());
        this.setSize(600, 600);
        this.setLocationRelativeTo((Component)null);
        this.setDefaultCloseOperation(3);
        this.setResizable(false);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        new GameXO();
    }
}
