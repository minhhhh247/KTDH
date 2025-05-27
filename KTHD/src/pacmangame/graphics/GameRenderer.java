package pacmangame.graphics;

import pacmangame.constants.GameConstants;
import pacmangame.model.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;

public class GameRenderer {

    public static void drawGameMap(Graphics2D g2d, GameMap gameMap) {
        for (int row = 0; row < gameMap.getRows(); row++) {
            for (int col = 0; col < gameMap.getCols(); col++) {
                int tileType = gameMap.getTile(row, col);
                Color tileColor;
                switch (tileType) {
                    case 0: tileColor = GameConstants.PATH_COLOR; break;
                    case 1: tileColor = GameConstants.WALL_COLOR; break;
                    case 2: tileColor = GameConstants.PATH_COLOR; break; // Type 2 nền như đường đi
                    case 3: tileColor = GameConstants.PATH_COLOR; break; // Type 3 nền như đường đi
                    default: tileColor = Color.BLACK; break;
                }
                g2d.setColor(tileColor);
                g2d.fillRect(col * GameConstants.GRID_SIZE, row * GameConstants.GRID_SIZE,
                        GameConstants.GRID_SIZE, GameConstants.GRID_SIZE);
            }
        }
    }

    public static void drawScalableElements(Graphics2D g2d, List<ScalableMapElement> elements) {
        for (ScalableMapElement element : elements) {
            drawScalableElement(g2d, element);
        }
    }

    private static void drawScalableElement(Graphics2D g2d, ScalableMapElement element) {
        // Chọn màu theo type
        Color elementColor;
        if (element.getType() == 2) {
            elementColor = GameConstants.SCALABLE_BLOCK_COLOR;
        } else {
            elementColor = GameConstants.DYNAMIC_WALL_COLOR;
        }

        g2d.setColor(elementColor);

        // Tính vị trí và kích thước để vẽ
        double centerX = element.getX() + GameConstants.GRID_SIZE / 2.0;
        double centerY = element.getY() + GameConstants.GRID_SIZE / 2.0;
        double halfSize = element.getCurrentSize() / 2.0;

        int drawX = (int)(centerX - halfSize);
        int drawY = (int)(centerY - halfSize);
        int drawSize = (int)element.getCurrentSize();

        // Vẽ hình vuông
        g2d.fillRect(drawX, drawY, drawSize, drawSize);

        // Vẽ viền
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRect(drawX, drawY, drawSize, drawSize);
    }

    public static void drawPlayer(Graphics2D g2d, Player player) {
        g2d.setColor(GameConstants.PACMAN_COLOR);
        Path2D.Double path = new Path2D.Double();
        List<Point2D> shapePoints = player.getTransformedShape();

        if (shapePoints.isEmpty()) return;

        path.moveTo(shapePoints.get(0).x + player.getX(), shapePoints.get(0).y + player.getY());
        for (int i = 1; i < shapePoints.size(); i++) {
            path.lineTo(shapePoints.get(i).x + player.getX(), shapePoints.get(i).y + player.getY());
        }
        path.closePath();
        g2d.fill(path);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(path);
    }

    public static void drawGhost(Graphics2D g2d, Ghost ghost) {
        g2d.setColor(GameConstants.GHOST_COLOR);
        Path2D.Double path = new Path2D.Double();
        List<Point2D> shape = ghost.getTransformedShape();

        path.moveTo(shape.get(0).x + ghost.getX(), shape.get(0).y + ghost.getY());
        for (int i = 1; i < shape.size(); i++) {
            path.lineTo(shape.get(i).x + ghost.getX(), shape.get(i).y + ghost.getY());
        }
        path.closePath();
        g2d.fill(path);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(path);
    }

    public static void drawPellet(Graphics2D g2d, Pellet pellet) {
        if (pellet.isEaten()) return;

        g2d.setColor(GameConstants.PELLET_COLOR);
        g2d.fillOval((int)pellet.getX(), (int)pellet.getY(),
                (int)pellet.getSize(), (int)pellet.getSize());
    }

    public static void drawScore(Graphics2D g2d, int score, int screenWidth) {
        g2d.setColor(GameConstants.SCORE_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));

        String scoreText = "Score: " + score;
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(scoreText);

        g2d.drawString(scoreText, screenWidth - textWidth - 10, 25);
    }

    public static void drawPlayerCoordinates(Graphics2D g2d, Player player) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString(String.format("Player: (%.1f, %.1f)", player.getX(), player.getY()), 10, 20);
    }
}
