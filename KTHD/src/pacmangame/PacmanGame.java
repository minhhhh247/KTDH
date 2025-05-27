package pacmangame;

import pacmangame.constants.GameConstants;
import pacmangame.graphics.GameRenderer;
import pacmangame.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PacmanGame extends JPanel implements KeyListener, MouseListener {

    private GameMap gameMap;
    private Player player;
    private List<Ghost> ghosts;
    private List<Pellet> pellets;
    private Random random;
    private Timer gameTimer;

    // Game state
    private int score = 0;
    private long lastGhostSpawnTime = 0;
    private long lastScaleTime = 0;
    private final long GHOST_SPAWN_INTERVAL = GameConstants.GHOST_SPAWN_INTERVAL;
    private final long SCALE_INTERVAL = GameConstants.SCALE_INTERVAL;
    private boolean gameOver = false;

    // Trạng thái phím
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    public PacmanGame() {
        this.gameMap = new GameMap();
        this.random = new Random();
        this.ghosts = new ArrayList<>();
        this.pellets = new ArrayList<>();

        setPreferredSize(new Dimension(gameMap.getScreenWidth(), gameMap.getScreenHeight()));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        initializePlayer();
        spawnInitialGhost();
        generatePellets();
        startTimer();

        long currentTime = System.currentTimeMillis();
        lastGhostSpawnTime = currentTime;
        lastScaleTime = currentTime;
    }

    private void initializePlayer() {
        Point2D startPos = gameMap.getFirstWalkablePosition();
        player = new Player(startPos.x, startPos.y);
    }

    private void spawnInitialGhost() {
        spawnGhost();
    }

    private void spawnGhost() {
        if (ghosts.size() >= GameConstants.MAX_GHOSTS) return;

        int attempts = 0;
        while (attempts < 50) {
            double randomX = random.nextDouble() * (gameMap.getScreenWidth() - GameConstants.PLAYER_SQUARE_SIZE);
            double randomY = random.nextDouble() * (gameMap.getScreenHeight() - GameConstants.PLAYER_SQUARE_SIZE);

            if (gameMap.isWalkableWithBounds(randomX, randomY,
                    GameConstants.GHOST_COLLISION_SIZE, GameConstants.GHOST_COLLISION_SIZE)) {

                if (Math.abs(randomX - player.getX()) > GameConstants.GRID_SIZE * 3 ||
                        Math.abs(randomY - player.getY()) > GameConstants.GRID_SIZE * 3) {
                    ghosts.add(new Ghost(randomX, randomY));
                    break;
                }
            }
            attempts++;
        }
    }

    private void generatePellets() {
        pellets.clear();

        for (int row = 0; row < gameMap.getRows(); row++) {
            for (int col = 0; col < gameMap.getCols(); col++) {
                int tileType = gameMap.getTile(row, col);
                if (tileType == 0) { // Chỉ sinh trên ô trống (type 0)
                    double pelletX = col * GameConstants.GRID_SIZE +
                            (GameConstants.GRID_SIZE - GameConstants.PELLET_SIZE) / 2;
                    double pelletY = row * GameConstants.GRID_SIZE +
                            (GameConstants.GRID_SIZE - GameConstants.PELLET_SIZE) / 2;

                    if (Math.abs(pelletX - player.getX()) > GameConstants.GRID_SIZE * 2 ||
                            Math.abs(pelletY - player.getY()) > GameConstants.GRID_SIZE * 2) {
                        pellets.add(new Pellet(pelletX, pelletY));
                    }
                }
            }
        }
    }

    private void startTimer() {
        gameTimer = new Timer(GameConstants.TIMER_DELAY, e -> {
            if (!gameOver) {
                updateGame();
            }
            repaint();
        });
        gameTimer.start();
    }

    private void updateGame() {
        updatePlayerDirection();
        player.update(gameMap);

        // Cập nhật scalable elements
        gameMap.updateScalableElements();

        // Cập nhật quái
        for (Ghost ghost : ghosts) {
            ghost.update(gameMap);
        }

        // Kiểm tra va chạm với quái
        checkGhostCollisions();

        // Kiểm tra ăn vật phẩm
        checkPelletCollisions();

        // Kiểm tra sinh quái mới
        if (!gameOver) {
            checkGhostSpawning();
        }

        // Kiểm tra trigger scaling
        checkScalingTrigger();

        // Kiểm tra sinh vật phẩm mới
        checkPelletRespawn();
    }

    private void checkScalingTrigger() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScaleTime >= SCALE_INTERVAL) {
            gameMap.triggerScaling();
            lastScaleTime = currentTime;
        }
    }

    private void checkGhostCollisions() {
        if (gameOver) return;

        for (Ghost ghost : ghosts) {
            if (player.checkCollisionWithGhost(ghost)) {
                player.stopMoving();
                gameOver = true;

                System.out.println("Game Over! Điểm hiện tại: " + score);

                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Game Over!\nĐiểm của bạn: " + score,
                            "Game Over",
                            JOptionPane.INFORMATION_MESSAGE);
                });

                break;
            }
        }
    }

    private void checkPelletCollisions() {
        for (Pellet pellet : pellets) {
            if (pellet.checkCollision(player)) {
                score += GameConstants.PELLET_POINTS;
            }
        }
    }

    private void checkGhostSpawning() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastGhostSpawnTime >= GHOST_SPAWN_INTERVAL) {
            spawnGhost();
            lastGhostSpawnTime = currentTime;
        }
    }

    private void checkPelletRespawn() {
        boolean allPelletsEaten = true;
        for (Pellet pellet : pellets) {
            if (!pellet.isEaten()) {
                allPelletsEaten = false;
                break;
            }
        }

        if (allPelletsEaten) {
            generatePellets();
        }
    }

    private void updatePlayerDirection() {
        if (upPressed) {
            player.setRequestedDirection(GameConstants.DIRECTION_UP);
        } else if (downPressed) {
            player.setRequestedDirection(GameConstants.DIRECTION_DOWN);
        } else if (leftPressed) {
            player.setRequestedDirection(GameConstants.DIRECTION_LEFT);
        } else if (rightPressed) {
            player.setRequestedDirection(GameConstants.DIRECTION_RIGHT);
        } else {
            player.clearRequestedDirection();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ bản đồ
        GameRenderer.drawGameMap(g2d, gameMap);

        // Vẽ scalable elements
        GameRenderer.drawScalableElements(g2d, gameMap.getScalableElements());

        // Vẽ vật phẩm
        for (Pellet pellet : pellets) {
            GameRenderer.drawPellet(g2d, pellet);
        }

        // Vẽ player
        GameRenderer.drawPlayer(g2d, player);

        // Vẽ quái
        for (Ghost ghost : ghosts) {
            GameRenderer.drawGhost(g2d, ghost);
        }

        // Vẽ điểm số
        GameRenderer.drawScore(g2d, score, getWidth());

        // Vẽ tọa độ nhân vật
        GameRenderer.drawPlayerCoordinates(g2d, player);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!gameOver) {
            double targetX = e.getX() - player.getCollisionWidth() / 2;
            double targetY = e.getY() - player.getCollisionHeight() / 2;
            player.moveToPoint(targetX, targetY, gameMap);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) return;

        int keyCode = e.getKeyCode();

        switch (keyCode) {
            case KeyEvent.VK_UP:
                upPressed = true;
                break;
            case KeyEvent.VK_DOWN:
                downPressed = true;
                break;
            case KeyEvent.VK_LEFT:
                leftPressed = true;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = true;
                break;
            case KeyEvent.VK_Q:
                player.scaleUp();
                break;
            case KeyEvent.VK_W:
                player.scaleDown();
                break;
            case KeyEvent.VK_E:
                player.shearX(1.5);
                break;
            case KeyEvent.VK_R:
                player.shearY(1.5);
                break;
            case KeyEvent.VK_D:
                player.shearX(-1.5);
                break;
            case KeyEvent.VK_F:
                player.shearY(-1.5);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        switch (keyCode) {
            case KeyEvent.VK_UP:
                upPressed = false;
                break;
            case KeyEvent.VK_DOWN:
                downPressed = false;
                break;
            case KeyEvent.VK_LEFT:
                leftPressed = false;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PacmanGame gamePanel = new PacmanGame();

            JFrame gameFrame = new JFrame("Pacman Game - With Scalable Elements");
            gameFrame.add(gamePanel);
            gameFrame.pack();
            gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameFrame.setLocationRelativeTo(null);
            gameFrame.setVisible(true);
            gamePanel.requestFocusInWindow();
        });
    }
}
