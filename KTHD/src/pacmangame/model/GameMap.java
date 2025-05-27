package pacmangame.model;

import pacmangame.constants.GameConstants;
import java.util.ArrayList;
import java.util.List;

public class GameMap {
    private int[][] map;
    private int rows;
    private int cols;
    private List<ScalableMapElement> scalableElements;

    private static final int[][] DEFAULT_MAP_LAYOUT = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1},
            {1, 0, 1, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1},
            {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}, // Thêm type 2 và 3
            {1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 3, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1}, // Thêm type 2 và 3
            {1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };

    public GameMap() {
        this(DEFAULT_MAP_LAYOUT);
    }

    public GameMap(int[][] mapLayout) {
        if (mapLayout == null || mapLayout.length == 0 || mapLayout[0].length == 0) {
            this.map = DEFAULT_MAP_LAYOUT;
        } else {
            this.map = mapLayout;
        }
        this.rows = map.length;
        this.cols = map[0].length;

        initializeScalableElements();
    }

    private void initializeScalableElements() {
        scalableElements = new ArrayList<>();

        // Tìm và tạo các thành phần scalable (type 2 và 3)
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int tileType = map[row][col];
                if (tileType == 2 || tileType == 3) {
                    double elementX = col * GameConstants.GRID_SIZE;
                    double elementY = row * GameConstants.GRID_SIZE;
                    scalableElements.add(new ScalableMapElement(elementX, elementY, tileType));
                }
            }
        }
    }

    public void updateScalableElements() {
        for (ScalableMapElement element : scalableElements) {
            element.update();
        }
    }

    public void triggerScaling() {
        for (ScalableMapElement element : scalableElements) {
            element.startScaling();
        }
    }

    public int getTile(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return map[row][col];
        }
        return 1; // Tường nếu ra ngoài biên
    }

    // Kiểm tra va chạm có tính đến scalable elements
    public boolean isWalkableWithBounds(double x, double y, double width, double height) {
        // Kiểm tra va chạm với map tĩnh
        if (!isWalkableWithBoundsStatic(x, y, width, height)) {
            return false;
        }

        // Kiểm tra va chạm với scalable elements
        for (ScalableMapElement element : scalableElements) {
            if (element.checkCollision(x, y, width, height)) {
                return false;
            }
        }

        return true;
    }

    private boolean isWalkableWithBoundsStatic(double x, double y, double width, double height) {
        double margin = 1.0;

        double left = x + margin;
        double right = x + width - margin;
        double top = y + margin;
        double bottom = y + height - margin;

        return isWalkablePoint(left, top) &&
                isWalkablePoint(right, top) &&
                isWalkablePoint(left, bottom) &&
                isWalkablePoint(right, bottom) &&
                isWalkablePoint((left + right) / 2, top) &&
                isWalkablePoint((left + right) / 2, bottom) &&
                isWalkablePoint(left, (top + bottom) / 2) &&
                isWalkablePoint(right, (top + bottom) / 2);
    }

    public boolean isWalkablePoint(double x, double y) {
        if (x < 0 || y < 0) return false;

        int col = (int) (x / GameConstants.GRID_SIZE);
        int row = (int) (y / GameConstants.GRID_SIZE);
        int tileType = getTile(row, col);

        // Type 0, 2 có thể đi qua; Type 1, 3 có thể chặn (tùy thuộc vào kích thước)
        return tileType != 1;
    }

    public boolean isWalkable(double x, double y) {
        return isWalkablePoint(x, y);
    }

    public boolean isWalkable(int row, int col) {
        int tileType = getTile(row, col);
        return tileType != 1;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getScreenWidth() { return cols * GameConstants.GRID_SIZE; }
    public int getScreenHeight() { return rows * GameConstants.GRID_SIZE; }
    public List<ScalableMapElement> getScalableElements() { return scalableElements; }

    public Point2D getFirstWalkablePosition() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (isWalkable(row, col)) {
                    return new Point2D(col * GameConstants.GRID_SIZE, row * GameConstants.GRID_SIZE);
                }
            }
        }
        return new Point2D(GameConstants.GRID_SIZE, GameConstants.GRID_SIZE);
    }
}
