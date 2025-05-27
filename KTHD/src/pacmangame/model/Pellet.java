package pacmangame.model;

import pacmangame.constants.GameConstants;

public class Pellet {
    private double x, y;
    private boolean eaten;
    private final double size;

    public Pellet(double x, double y) {
        this.x = x;
        this.y = y;
        this.eaten = false;
        this.size = GameConstants.PELLET_SIZE;
    }

    public boolean checkCollision(Player player) {
        if (eaten) return false;

        // Kiểm tra va chạm với player
        double playerCenterX = player.getX() + GameConstants.PLAYER_SQUARE_SIZE / 2;
        double playerCenterY = player.getY() + GameConstants.PLAYER_SQUARE_SIZE / 2;

        double pelletCenterX = x + size / 2;
        double pelletCenterY = y + size / 2;

        double distance = Math.sqrt(
                Math.pow(playerCenterX - pelletCenterX, 2) +
                        Math.pow(playerCenterY - pelletCenterY, 2)
        );

        // Nếu khoảng cách nhỏ hơn bán kính tổng
        if (distance < (GameConstants.PLAYER_SQUARE_SIZE / 2 + size / 2)) {
            eaten = true;
            return true;
        }

        return false;
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isEaten() { return eaten; }
    public double getSize() { return size; }
}
