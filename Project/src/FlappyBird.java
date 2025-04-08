import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    int frameWidth = 360;
    int frameHeight = 640;
    boolean isGameStarted = false;

    //image attributes
    Image backgroundImage;
    Image birdImage;
    Image lowerPipeImage;
    Image upperPipeImage;

    //player
    int playerStartPosX = frameWidth / 8;
    int playerStartPosY = frameHeight / 2;
    int playerWidth = 34;
    int playerHeight = 24;
    Player player;

    //pipes atributes
    int pipeStartPosX = frameWidth;
    int pipesStartPosY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;
    ArrayList<Pipe> pipes;

    //game over
    boolean isGameOver = false;
    Font gameOverFont = new Font("Arial", Font.BOLD, 36);

    //score
    int score = 0;
    private JLabel scoreLabel;

    //game logic
    Timer gameLoop;
    Timer pipesCooldown;
    int gravity = 1;

    //constructor
    public FlappyBird() {
        setPreferredSize(new Dimension(frameWidth, frameHeight));
        setFocusable(true);
        addKeyListener(this);

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setBounds(10, 10, 200, 30);
        setLayout(null); // penting untuk bisa atur posisi manual
        add(scoreLabel);

        //load images
        backgroundImage = new ImageIcon(getClass().getResource("assets/background.png")).getImage();
        birdImage = new ImageIcon(getClass().getResource("assets/bird.png")).getImage();
        lowerPipeImage = new ImageIcon(getClass().getResource("assets/lowerPipe.png")).getImage();
        upperPipeImage = new ImageIcon(getClass().getResource("assets/upperPipe.png")).getImage();

        player = new Player(playerStartPosX, playerStartPosY, playerWidth, playerHeight, birdImage);
        pipes = new ArrayList<Pipe>();

        pipesCooldown = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("pipa");
                placePipes();
            }
        });
        pipesCooldown.start();

        gameLoop = new Timer(1000/60, this);
        gameLoop.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponents(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, frameWidth, frameHeight, null);

        g.drawImage(player.getImage(), player.getPosX(), player.getPosY(), player.getWidth(), player.getHeight(), null);

        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.getImage(), pipe.getPosX(), pipe.getPosY(), pipe.getWidth(), pipe.getHeight(), null);
        }

        if (isGameOver) {
            g.setColor(Color.RED);
            g.setFont(gameOverFont);
            g.drawString("GAME OVER", frameWidth / 2 - 100, frameHeight / 2);
        }
    }

    public void move() {
        if (!isGameOver) {

            if (isGameStarted) {
                player.setVelocityY(player.getVelocityY() + gravity);
                player.setPosY(player.getPosY() + player.getVelocityY());
                player.setPosY(Math.max(player.getPosY(), 0));

                //tambahkan logika game over jika jatuh ke bawah
                if (player.getPosY() + player.getHeight() >= frameHeight) {
                    isGameOver = true;
                    gameLoop.stop();
                    pipesCooldown.stop();
                    return;
                }
            }

            //hanya cek untuk pipa atas (indeks genap) karena pipa diletakkan berpasangan
            for (int i = 0; i < pipes.size(); i += 2) {
                //pipa atas (selalu indeks genap)
                Pipe upperPipe = pipes.get(i);

                //pastikan pipa bawah ada dengan memeriksa indeks
                if (i + 1 < pipes.size()) {
                    //pipa bawah (selalu indeks ganjil)
                    Pipe lowerPipe = pipes.get(i + 1);

                    //gerakkan kedua pipa
                    upperPipe.setPosX(upperPipe.getPosX() + upperPipe.getVelocityX());
                    lowerPipe.setPosX(lowerPipe.getPosX() + lowerPipe.getVelocityX());

                    //cek tabrakan untuk kedua pipa
                    if (cekTabrakan(player, upperPipe) || cekTabrakan(player, lowerPipe)) {
                        isGameOver = true;
                        gameLoop.stop();
                        pipesCooldown.stop();
                    }

                    //cek jika set pipa telah dilewati (hanya cek pipa atas)
                    if (!upperPipe.isPassed() && upperPipe.getPosX() + upperPipe.getWidth() < player.getPosX()) {
                        upperPipe.setPassed(true);
                        lowerPipe.setPassed(true);
                        score++;
                        updateScoreLabel();
                    }
                }
            }
        }
    }

    private void updateScoreLabel() {
        scoreLabel.setText("Score: " + score);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!isGameStarted) {
                isGameStarted = true;
            }
            if (!isGameOver) {
                player.setVelocityY(-10);
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_R && isGameOver) {
            restartGame();
        }
    }


    @Override
    public void keyReleased(KeyEvent e) {

    }

    public void placePipes() {
        int randomPosY = (int) (pipesStartPosY - pipeHeight/4 - Math.random() * (pipeHeight/2));
        int openingSpace = frameHeight/4;

        Pipe upperPipe = new Pipe(pipeStartPosX, randomPosY, pipeWidth, pipeHeight, upperPipeImage);
        pipes.add(upperPipe);

        Pipe lowerPipe = new Pipe(pipeStartPosX, (randomPosY + openingSpace + pipeHeight), pipeWidth, pipeHeight, lowerPipeImage);
        pipes.add(lowerPipe);
    }

    public boolean cekTabrakan(Player player, Pipe pipe) {
        Rectangle playerBird = new Rectangle(player.getPosX(), player.getPosY(), player.getWidth(), player.getHeight());
        Rectangle pipeBird = new Rectangle(pipe.getPosX(), pipe.getPosY(), pipe.getWidth(), pipe.getHeight());
        return playerBird.intersects(pipeBird);
    }

    public void restartGame() {
        //reset player
        player.setPosX(playerStartPosX);
        player.setPosY(playerStartPosY);
        player.setVelocityY(0);

        //reset pipes dan skor
        pipes.clear();
        score = 0;
        updateScoreLabel();

        //reset status game
        isGameOver = false;
        isGameStarted = false;

        //restart timer
        gameLoop.start();
        pipesCooldown.start();

        repaint();
    }

}
