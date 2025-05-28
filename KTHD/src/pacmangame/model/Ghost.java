package pacmangame.model;

import pacmangame.constants.GameConstants;
import pacmangame.utils.MatrixUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ghost {
    private double absoluteX, absoluteY;

    // AI di chuyển theo thuật toán Pacman
    private int currentDirection = GameConstants.DIRECTION_NONE;
    private int previousDirection = GameConstants.DIRECTION_NONE;
    private Random random;
    private int directionChangeTimer = 0;
    private int stuckCounter = 0;

    // Hitbox
    private final double COLLISION_WIDTH = GameConstants.GHOST_COLLISION_SIZE;
    private final double COLLISION_HEIGHT = GameConstants.GHOST_COLLISION_SIZE;

    // Hình dạng và biến dạng
    private List<Point2D> originalShape;
    private List<Point2D> transformedShape;
    private float[][] shapeTransformationMatrix;

    // Biến dạng theo hướng
    private final float DEFORM_FACTOR = 1.3f;

    public Ghost(double startX, double startY) {
        this.absoluteX = startX;
        this.absoluteY = startY;
        this.random = new Random();
        this.currentDirection = random.nextInt(4);
        this.directionChangeTimer = random.nextInt(60) + 30; // 30-90 frames

        initializeShape();
        initializeTransformationMatrix();
        applyDirectionalDeformation();
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
        directionChangeTimer--;

        // Biến dạng theo hướng di chuyển
        applyDirectionalDeformation();

        // Logic di chuyển theo thuật toán Pacman
        if (currentDirection != GameConstants.DIRECTION_NONE) {
            if (!moveWithMatrix(currentDirection, gameMap)) {
                // Gặp tường - chọn hướng mới theo thuật toán
                handleDirectionChange(gameMap);
            } else {
                stuckCounter = 0;
            }
        } else {
            chooseInitialDirection(gameMap);
        }

        // Thay đổi hướng định kỳ (như thuật toán Pacman)
        if (directionChangeTimer <= 0) {
            considerDirectionChange(gameMap);
            directionChangeTimer = random.nextInt(120) + 60; // 60-180 frames
        }
    }

    // Di chuyển bằng ma trận tịnh tiến
    private boolean moveWithMatrix(int direction, GameMap gameMap) {
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

        // Áp dụng ma trận lên vị trí hiện tại
        Point2D currentPos = new Point2D(absoluteX, absoluteY);
        Point2D newPos = MatrixUtils.applyTransformation(currentPos, translationMatrix);

        // Kiểm tra biên màn hình
        if (newPos.x < 0 || newPos.y < 0 ||
                newPos.x + GameConstants.PLAYER_SQUARE_SIZE > gameMap.getScreenWidth() ||
                newPos.y + GameConstants.PLAYER_SQUARE_SIZE > gameMap.getScreenHeight()) {
            return false;
        }

        // Kiểm tra va chạm
        double offsetX = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_WIDTH) / 2;
        double offsetY = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_HEIGHT) / 2;

        if (gameMap.isWalkableWithBounds(newPos.x + offsetX, newPos.y + offsetY,
                COLLISION_WIDTH, COLLISION_HEIGHT)) {
            // Cập nhật vị trí bằng ma trận
            absoluteX = newPos.x;
            absoluteY = newPos.y;
            return true;
        }

        return false;
    }

    // Xử lý thay đổi hướng khi gặp tường (theo thuật toán Pacman)
    private void handleDirectionChange(GameMap gameMap) {
        stuckCounter++;

        if (stuckCounter > 1) {
            List<Integer> availableDirections = getAvailableDirections(gameMap);

            if (!availableDirections.isEmpty()) {
                // Loại bỏ hướng ngược lại (quy tắc Pacman)
                int oppositeDirection = getOppositeDirection(currentDirection);
                availableDirections.removeIf(dir -> dir == oppositeDirection);

                if (availableDirections.isEmpty()) {
                    // Nếu chỉ có hướng ngược lại, chấp nhận
                    currentDirection = oppositeDirection;
                } else {
                    // Chọn hướng ngẫu nhiên từ các hướng khả dụng
                    currentDirection = availableDirections.get(random.nextInt(availableDirections.size()));
                }

                previousDirection = currentDirection;
                stuckCounter = 0;
            }
        }
    }

    // Chọn hướng ban đầu
    private void chooseInitialDirection(GameMap gameMap) {
        List<Integer> availableDirections = getAvailableDirections(gameMap);

        if (!availableDirections.isEmpty()) {
            currentDirection = availableDirections.get(random.nextInt(availableDirections.size()));
            previousDirection = currentDirection;
        }
    }

    // Cân nhắc thay đổi hướng định kỳ
    private void considerDirectionChange(GameMap gameMap) {
        // 30% cơ hội thay đổi hướng (tạo tính ngẫu nhiên)
        if (random.nextInt(100) < 30) {
            List<Integer> availableDirections = getAvailableDirections(gameMap);

            if (availableDirections.size() > 1) {
                // Loại bỏ hướng hiện tại để buộc thay đổi
                availableDirections.removeIf(dir -> dir == currentDirection);

                if (!availableDirections.isEmpty()) {
                    currentDirection = availableDirections.get(random.nextInt(availableDirections.size()));
                    previousDirection = currentDirection;
                }
            }
        }
    }

    // Lấy danh sách hướng có thể di chuyển
    private List<Integer> getAvailableDirections(GameMap gameMap) {
        List<Integer> availableDirections = new ArrayList<>();

        for (int dir = 0; dir < 4; dir++) {
            if (canMoveInDirection(dir, gameMap)) {
                availableDirections.add(dir);
            }
        }

        return availableDirections;
    }

    // Kiểm tra có thể di chuyển theo hướng không
    private boolean canMoveInDirection(int direction, GameMap gameMap) {
        float dx = 0, dy = 0;
        switch (direction) {
            case GameConstants.DIRECTION_UP:
                dy = -(float)GameConstants.GHOST_SPEED * 2; // Test xa hơn
                break;
            case GameConstants.DIRECTION_DOWN:
                dy = (float)GameConstants.GHOST_SPEED * 2;
                break;
            case GameConstants.DIRECTION_LEFT:
                dx = -(float)GameConstants.GHOST_SPEED * 2;
                break;
            case GameConstants.DIRECTION_RIGHT:
                dx = (float)GameConstants.GHOST_SPEED * 2;
                break;
        }

        // Sử dụng ma trận để test
        float[][] testMatrix = MatrixUtils.createTranslationMatrix(dx, dy);
        Point2D currentPos = new Point2D(absoluteX, absoluteY);
        Point2D testPos = MatrixUtils.applyTransformation(currentPos, testMatrix);

        // Kiểm tra biên
        if (testPos.x < 0 || testPos.y < 0 ||
                testPos.x + GameConstants.PLAYER_SQUARE_SIZE > gameMap.getScreenWidth() ||
                testPos.y + GameConstants.PLAYER_SQUARE_SIZE > gameMap.getScreenHeight()) {
            return false;
        }

        double offsetX = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_WIDTH) / 2;
        double offsetY = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_HEIGHT) / 2;

        return gameMap.isWalkableWithBounds(testPos.x + offsetX, testPos.y + offsetY,
                COLLISION_WIDTH, COLLISION_HEIGHT);
    }

    private int getOppositeDirection(int direction) {
        switch (direction) {
            case GameConstants.DIRECTION_UP: return GameConstants.DIRECTION_DOWN;
            case GameConstants.DIRECTION_DOWN: return GameConstants.DIRECTION_UP;
            case GameConstants.DIRECTION_LEFT: return GameConstants.DIRECTION_RIGHT;
            case GameConstants.DIRECTION_RIGHT: return GameConstants.DIRECTION_LEFT;
            default: return GameConstants.DIRECTION_NONE;
        }
    }

    // Biến dạng theo hướng di chuyển
    private void applyDirectionalDeformation() {
        shapeTransformationMatrix = MatrixUtils.createIdentityMatrix();

        if (currentDirection == GameConstants.DIRECTION_NONE) {
            applyShapeTransformation();
            return;
        }

        float[][] deformMatrix;

        switch (currentDirection) {
            case GameConstants.DIRECTION_UP:
                deformMatrix = MatrixUtils.createScaleMatrix(1.0f, DEFORM_FACTOR);
                break;
            case GameConstants.DIRECTION_DOWN:
                deformMatrix = MatrixUtils.createScaleMatrix(1.0f, 1.0f / DEFORM_FACTOR);
                break;
            case GameConstants.DIRECTION_LEFT:
                deformMatrix = MatrixUtils.createScaleMatrix(1.0f / DEFORM_FACTOR, 1.0f);
                break;
            case GameConstants.DIRECTION_RIGHT:
                deformMatrix = MatrixUtils.createScaleMatrix(DEFORM_FACTOR, 1.0f);
                break;
            default:
                deformMatrix = MatrixUtils.createIdentityMatrix();
                break;
        }

        // Áp dụng biến đổi quanh điểm cố định
        shapeTransformationMatrix = MatrixUtils.createTransformationAroundPoint(
                deformMatrix, (float)GameConstants.FIXED_POINT_X, (float)GameConstants.FIXED_POINT_Y);

        applyShapeTransformation();
    }

    private void applyShapeTransformation() {
        transformedShape = MatrixUtils.applyMatrixToShape(originalShape, shapeTransformationMatrix);
    }

    public boolean checkCollisionWithPlayer(Player player) {
        double ghostCenterX = absoluteX + GameConstants.PLAYER_SQUARE_SIZE / 2;
        double ghostCenterY = absoluteY + GameConstants.PLAYER_SQUARE_SIZE / 2;

        double playerCenterX = player.getX() + GameConstants.PLAYER_SQUARE_SIZE / 2;
        double playerCenterY = player.getY() + GameConstants.PLAYER_SQUARE_SIZE / 2;

        double distance = Math.sqrt(
                Math.pow(ghostCenterX - playerCenterX, 2) +
                        Math.pow(ghostCenterY - playerCenterY, 2)
        );

        double collisionDistance = (GameConstants.PLAYER_SQUARE_SIZE + GameConstants.PLAYER_SQUARE_SIZE) / 2 * 0.8;
        return distance < collisionDistance;
    }

    public double getX() { return absoluteX; }
    public double getY() { return absoluteY; }
    public List<Point2D> getTransformedShape() { return transformedShape; }
}
