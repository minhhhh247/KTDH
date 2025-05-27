package pacmangame.model;

import pacmangame.constants.GameConstants;
import pacmangame.utils.MatrixUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ghost {
    // Vị trí đơn giản
    private double absoluteX, absoluteY;
    private double targetX, targetY;
    private boolean isMoving = false;
    private boolean hasTarget = false;

    // AI và di chuyển
    private int currentDirection = GameConstants.DIRECTION_NONE;
    private int previousDirection = GameConstants.DIRECTION_NONE;
    private Random random;
    private int waitCounter = 0;
    private int directionChangeDelay = 0;
    private int stuckCounter = 0;
    private int cornerEscapeCounter = 0;

    // Hitbox
    private final double COLLISION_WIDTH = GameConstants.GHOST_COLLISION_SIZE;
    private final double COLLISION_HEIGHT = GameConstants.GHOST_COLLISION_SIZE;

    // Hình dạng và biến dạng
    private List<Point2D> originalShape;
    private List<Point2D> transformedShape;
    private float[][] shapeTransformationMatrix;

    // Biến dạng đơn giản theo hướng
    private final float DEFORM_FACTOR = 1.3f;

    public Ghost(double startX, double startY) {
        this.absoluteX = startX;
        this.absoluteY = startY;
        this.targetX = startX;
        this.targetY = startY;
        this.random = new Random();
        this.currentDirection = random.nextInt(4);

        initializeShape();
        initializeTransformationMatrix();
        applyShapeTransformation();
    }

    private void initializeShape() {
        originalShape = new ArrayList<>();
        originalShape.add(new Point2D(0, GameConstants.PLAYER_SQUARE_SIZE));
        originalShape.add(new Point2D(GameConstants.PLAYER_SQUARE_SIZE, GameConstants.PLAYER_SQUARE_SIZE));
        originalShape.add(new Point2D(GameConstants.PLAYER_SQUARE_SIZE, 0));
        originalShape.add(new Point2D(0, 0));

        transformedShape = new ArrayList<>();
        for (Point2D point : originalShape) {
            transformedShape.add(new Point2D(point));
        }
    }

    private void initializeTransformationMatrix() {
        shapeTransformationMatrix = MatrixUtils.createIdentityMatrix();
    }

    public void update(GameMap gameMap) {
        if (waitCounter > 0) {
            waitCounter--;
            return;
        }

        if (directionChangeDelay > 0) {
            directionChangeDelay--;
        }

        // Áp dụng biến dạng theo hướng di chuyển
        applyDirectionalDeformation();

        // Kiểm tra xem có bị kẹt ở góc không
        if (isStuckInCorner(gameMap)) {
            handleCornerEscape(gameMap);
            return;
        }

        cornerEscapeCounter = 0;

        // Thử di chuyển theo hướng hiện tại
        if (currentDirection != GameConstants.DIRECTION_NONE) {
            if (!moveInDirectionWithMatrix(currentDirection, gameMap)) {
                stuckCounter++;
                if (stuckCounter > 1) {
                    chooseNewDirectionSmart(gameMap);
                    stuckCounter = 0;
                    directionChangeDelay = 3;
                }
            } else {
                stuckCounter = 0;
            }
        } else {
            chooseNewDirectionSmart(gameMap);
        }

        // Thay đổi hướng ngẫu nhiên thỉnh thoảng
        if (random.nextInt(300) == 0 && directionChangeDelay <= 0) {
            chooseNewDirectionSmart(gameMap);
        }
    }

    private void applyDirectionalDeformation() {
        // Reset về ma trận đơn vị
        shapeTransformationMatrix = MatrixUtils.createIdentityMatrix();

        // Áp dụng biến dạng theo hướng di chuyển
        float[][] deformMatrix = MatrixUtils.createIdentityMatrix();

        switch (currentDirection) {
            case GameConstants.DIRECTION_UP:
                // Lên: scale 1, DEFORM_FACTOR (kéo dài theo Y)
                deformMatrix = MatrixUtils.createScaleMatrix(1.0f, DEFORM_FACTOR);
                break;
            case GameConstants.DIRECTION_DOWN:
                // Xuống: scale 1, 1/DEFORM_FACTOR (nén theo Y)
                deformMatrix = MatrixUtils.createScaleMatrix(1.0f, 1.0f / DEFORM_FACTOR);
                break;
            case GameConstants.DIRECTION_LEFT:
                // Trái: scale 1/DEFORM_FACTOR, 1 (nén theo X)
                deformMatrix = MatrixUtils.createScaleMatrix(1.0f / DEFORM_FACTOR, 1.0f);
                break;
            case GameConstants.DIRECTION_RIGHT:
                // Phải: scale DEFORM_FACTOR, 1 (kéo dài theo X)
                deformMatrix = MatrixUtils.createScaleMatrix(DEFORM_FACTOR, 1.0f);
                break;
            default:
                // Không di chuyển, giữ nguyên
                break;
        }

        // Áp dụng biến đổi quanh điểm cố định
        if (currentDirection != GameConstants.DIRECTION_NONE) {
            shapeTransformationMatrix = MatrixUtils.createTransformationAroundPoint(
                    deformMatrix, (float)GameConstants.FIXED_POINT_X, (float)GameConstants.FIXED_POINT_Y);
        }

        applyShapeTransformation();
    }

    private void applyShapeTransformation() {
        transformedShape = MatrixUtils.applyMatrixToShape(originalShape, shapeTransformationMatrix);
    }

    private boolean moveInDirectionWithMatrix(int direction, GameMap gameMap) {
        // Tính vector di chuyển
        float dx = 0, dy = 0;
        switch (direction) {
            case GameConstants.DIRECTION_UP:
                dy = -(float)GameConstants.GHOST_SPEED;
                break;
            case GameConstants.DIRECTION_DOWN:
                dy = (float)GameConstants.GHOST_SPEED;
                break;
            case GameConstants.DIRECTION_LEFT:
                dx = -(float)GameConstants.GHOST_SPEED;
                break;
            case GameConstants.DIRECTION_RIGHT:
                dx = (float)GameConstants.GHOST_SPEED;
                break;
            default:
                return false;
        }

        // Tạo ma trận tịnh tiến
        float[][] translationMatrix = MatrixUtils.createTranslationMatrix(dx, dy);

        // Tính vị trí mới bằng ma trận
        Point2D currentPos = new Point2D(absoluteX, absoluteY);
        Point2D newPos = MatrixUtils.applyTransformation(currentPos, translationMatrix);

        double offsetX = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_WIDTH) / 2;
        double offsetY = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_HEIGHT) / 2;

        if (gameMap.isWalkableWithBounds(newPos.x + offsetX, newPos.y + offsetY,
                COLLISION_WIDTH, COLLISION_HEIGHT)) {
            // Áp dụng di chuyển
            absoluteX = newPos.x;
            absoluteY = newPos.y;
            return true;
        } else {
            return trySlideMovementWithMatrix(dx, dy, direction, gameMap);
        }
    }

    private boolean trySlideMovementWithMatrix(float mainDx, float mainDy, int direction, GameMap gameMap) {
        double offsetX = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_WIDTH) / 2;
        double offsetY = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_HEIGHT) / 2;

        float[] slideDirections = {
                (float)GameConstants.SLIDE_DISTANCE,
                -(float)GameConstants.SLIDE_DISTANCE,
                (float)(GameConstants.SLIDE_DISTANCE * 2),
                -(float)(GameConstants.SLIDE_DISTANCE * 2)
        };

        for (float slideAmount : slideDirections) {
            float slideDx = 0, slideDy = 0;

            if (direction == GameConstants.DIRECTION_UP || direction == GameConstants.DIRECTION_DOWN) {
                slideDx = slideAmount;
            } else {
                slideDy = slideAmount;
            }

            // Tạo ma trận tịnh tiến kết hợp
            float[][] combinedMatrix = MatrixUtils.createTranslationMatrix(mainDx + slideDx, mainDy + slideDy);

            // Tính vị trí test bằng ma trận
            Point2D currentPos = new Point2D(absoluteX, absoluteY);
            Point2D testPos = MatrixUtils.applyTransformation(currentPos, combinedMatrix);

            if (gameMap.isWalkableWithBounds(testPos.x + offsetX, testPos.y + offsetY,
                    COLLISION_WIDTH, COLLISION_HEIGHT)) {
                absoluteX = testPos.x;
                absoluteY = testPos.y;
                return true;
            }
        }

        return false;
    }

    // Các phương thức AI khác giữ nguyên
    private boolean isStuckInCorner(GameMap gameMap) {
        List<Integer> availableDirections = getAvailableDirections(gameMap);

        if (availableDirections.size() <= 1) {
            cornerEscapeCounter++;
            return cornerEscapeCounter > 5;
        }

        return false;
    }

    private void handleCornerEscape(GameMap gameMap) {
        List<Integer> allDirections = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            allDirections.add(i);
        }

        int oppositeDirection = (currentDirection + 2) % 4;
        if (canMoveInDirection(oppositeDirection, gameMap)) {
            currentDirection = oppositeDirection;
            previousDirection = currentDirection;
            cornerEscapeCounter = 0;
            return;
        }

        for (int dir : allDirections) {
            if (dir != currentDirection && canMoveInDirection(dir, gameMap)) {
                currentDirection = dir;
                previousDirection = currentDirection;
                cornerEscapeCounter = 0;
                return;
            }
        }

        forceRandomMovementWithMatrix(gameMap);
    }

    private void forceRandomMovementWithMatrix(GameMap gameMap) {
        float randomDx = (float)((random.nextDouble() - 0.5) * 2);
        float randomDy = (float)((random.nextDouble() - 0.5) * 2);

        float[][] randomMatrix = MatrixUtils.createTranslationMatrix(randomDx, randomDy);
        Point2D currentPos = new Point2D(absoluteX, absoluteY);
        Point2D testPos = MatrixUtils.applyTransformation(currentPos, randomMatrix);

        double offsetX = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_WIDTH) / 2;
        double offsetY = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_HEIGHT) / 2;

        if (gameMap.isWalkableWithBounds(testPos.x + offsetX, testPos.y + offsetY,
                COLLISION_WIDTH, COLLISION_HEIGHT)) {
            absoluteX = testPos.x;
            absoluteY = testPos.y;
            cornerEscapeCounter = 0;
            waitCounter = 10;
        }
    }

    private boolean canMoveInDirection(int direction, GameMap gameMap) {
        float dx = 0, dy = 0;
        switch (direction) {
            case GameConstants.DIRECTION_UP:
                dy = -(float)GameConstants.GHOST_SPEED;
                break;
            case GameConstants.DIRECTION_DOWN:
                dy = (float)GameConstants.GHOST_SPEED;
                break;
            case GameConstants.DIRECTION_LEFT:
                dx = -(float)GameConstants.GHOST_SPEED;
                break;
            case GameConstants.DIRECTION_RIGHT:
                dx = (float)GameConstants.GHOST_SPEED;
                break;
        }

        float[][] translationMatrix = MatrixUtils.createTranslationMatrix(dx, dy);
        Point2D currentPos = new Point2D(absoluteX, absoluteY);
        Point2D testPos = MatrixUtils.applyTransformation(currentPos, translationMatrix);

        double offsetX = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_WIDTH) / 2;
        double offsetY = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_HEIGHT) / 2;

        return gameMap.isWalkableWithBounds(testPos.x + offsetX, testPos.y + offsetY,
                COLLISION_WIDTH, COLLISION_HEIGHT);
    }

    private List<Integer> getAvailableDirections(GameMap gameMap) {
        List<Integer> availableDirections = new ArrayList<>();

        for (int dir = 0; dir < 4; dir++) {
            if (canMoveInDirection(dir, gameMap)) {
                availableDirections.add(dir);
            }
        }

        return availableDirections;
    }

    private void chooseNewDirectionSmart(GameMap gameMap) {
        List<Integer> availableDirections = getAvailableDirections(gameMap);

        if (availableDirections.isEmpty()) {
            handleCornerEscape(gameMap);
            return;
        }

        List<Integer> preferredDirections = new ArrayList<>();
        int oppositeDirection = (previousDirection + 2) % 4;

        for (int dir : availableDirections) {
            if (dir != oppositeDirection) {
                preferredDirections.add(dir);
            }
        }

        if (!preferredDirections.isEmpty()) {
            currentDirection = preferredDirections.get(random.nextInt(preferredDirections.size()));
        } else {
            currentDirection = availableDirections.get(random.nextInt(availableDirections.size()));
        }

        previousDirection = currentDirection;
    }

    public double getX() { return absoluteX; }
    public double getY() { return absoluteY; }
    public List<Point2D> getTransformedShape() { return transformedShape; }
}
