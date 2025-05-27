package pacmangame.model;

import pacmangame.constants.GameConstants;
import pacmangame.utils.MatrixUtils;
import java.util.ArrayList;
import java.util.List;

public class Player {
    // Vị trí đơn giản
    private double absoluteX, absoluteY;
    private double targetX, targetY;
    private boolean isMoving = false;
    private boolean hasTarget = false;
    private boolean isStopped = false; // Thêm trạng thái dừng

    // Hướng di chuyển liên tục
    private int currentDirection = GameConstants.DIRECTION_NONE;
    private int requestedDirection = GameConstants.DIRECTION_NONE;

    // Hitbox
    private final double COLLISION_WIDTH = GameConstants.PLAYER_COLLISION_SIZE;
    private final double COLLISION_HEIGHT = GameConstants.PLAYER_COLLISION_SIZE;

    // Ma trận biến đổi cho hình dạng
    private double currentScaleFactor = 1.0;
    private float[][] shapeTransformationMatrix;

    // Hình dạng gốc và đã biến đổi
    private List<Point2D> originalShape;
    private List<Point2D> transformedShape;

    public Player(double startX, double startY) {
        this.absoluteX = startX;
        this.absoluteY = startY;
        this.targetX = startX;
        this.targetY = startY;

        initializeShape();
        initializeTransformationMatrix();
        applyShapeTransformation();
    }

    private void initializeShape() {
        originalShape = new ArrayList<>();
        originalShape.add(new Point2D(0, 0));
        originalShape.add(new Point2D(0, GameConstants.PLAYER_SQUARE_SIZE));
        originalShape.add(new Point2D(GameConstants.PLAYER_SQUARE_SIZE, GameConstants.PLAYER_SQUARE_SIZE));
        originalShape.add(new Point2D(GameConstants.PLAYER_SQUARE_SIZE, GameConstants.PLAYER_SQUARE_SIZE/2));
        originalShape.add(new Point2D(GameConstants.PLAYER_SQUARE_SIZE, 0));

        transformedShape = new ArrayList<>();
        for (Point2D point : originalShape) {
            transformedShape.add(new Point2D(point));
        }
    }

    private void initializeTransformationMatrix() {
        shapeTransformationMatrix = MatrixUtils.createIdentityMatrix();
    }

    public void update(GameMap gameMap) {
        // Nếu bị dừng, không di chuyển
        if (isStopped) return;

        // Xử lý di chuyển liên tục
        handleContinuousMovement(gameMap);

        // Xử lý di chuyển đến target (nếu có)
        if (hasTarget) {
            moveToTarget();
        }
    }

    // Phương thức dừng nhân vật khi va chạm với quái
    public void stopMoving() {
        isStopped = true;
        isMoving = false;
        hasTarget = false;
        requestedDirection = GameConstants.DIRECTION_NONE;
        currentDirection = GameConstants.DIRECTION_NONE;
    }

    // Kiểm tra va chạm với quái
    public boolean checkCollisionWithGhost(Ghost ghost) {
        // Tính tâm của player
        double playerCenterX = absoluteX + GameConstants.PLAYER_SQUARE_SIZE / 2;
        double playerCenterY = absoluteY + GameConstants.PLAYER_SQUARE_SIZE / 2;

        // Tính tâm của ghost
        double ghostCenterX = ghost.getX() + GameConstants.PLAYER_SQUARE_SIZE / 2;
        double ghostCenterY = ghost.getY() + GameConstants.PLAYER_SQUARE_SIZE / 2;

        // Tính khoảng cách giữa hai tâm
        double distance = Math.sqrt(
                Math.pow(playerCenterX - ghostCenterX, 2) +
                        Math.pow(playerCenterY - ghostCenterY, 2)
        );

        // Va chạm nếu khoảng cách nhỏ hơn tổng bán kính
        double collisionDistance = (GameConstants.PLAYER_SQUARE_SIZE + GameConstants.PLAYER_SQUARE_SIZE) / 2 * 0.8;
        return distance < collisionDistance;
    }

    private void handleContinuousMovement(GameMap gameMap) {
        // Nếu có hướng được yêu cầu, di chuyển theo hướng đó
        if (requestedDirection != GameConstants.DIRECTION_NONE) {
            moveInDirectionWithMatrix(requestedDirection, gameMap);
        }
    }

    private void moveInDirectionWithMatrix(int direction, GameMap gameMap) {
        // Tính vector di chuyển
        float dx = 0, dy = 0;
        switch (direction) {
            case GameConstants.DIRECTION_UP:
                dy = -(float)GameConstants.PLAYER_SPEED;
                break;
            case GameConstants.DIRECTION_DOWN:
                dy = (float)GameConstants.PLAYER_SPEED;
                break;
            case GameConstants.DIRECTION_LEFT:
                dx = -(float)GameConstants.PLAYER_SPEED;
                break;
            case GameConstants.DIRECTION_RIGHT:
                dx = (float)GameConstants.PLAYER_SPEED;
                break;
            default:
                return;
        }

        // Tạo ma trận tịnh tiến
        float[][] translationMatrix = MatrixUtils.createTranslationMatrix(dx, dy);

        // Tính vị trí mới bằng ma trận
        Point2D currentPos = new Point2D(absoluteX, absoluteY);
        Point2D newPos = MatrixUtils.applyTransformation(currentPos, translationMatrix);

        // Tính offset cho collision
        double offsetX = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_WIDTH) / 2;
        double offsetY = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_HEIGHT) / 2;

        // Kiểm tra va chạm
        if (gameMap.isWalkableWithBounds(newPos.x + offsetX, newPos.y + offsetY,
                COLLISION_WIDTH, COLLISION_HEIGHT)) {
            // Áp dụng di chuyển
            absoluteX = newPos.x;
            absoluteY = newPos.y;
        } else {
            // Thử trôi
            trySlideMovementWithMatrix(dx, dy, direction, gameMap);
        }
    }

    private void trySlideMovementWithMatrix(float mainDx, float mainDy, int direction, GameMap gameMap) {
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
                // Áp dụng di chuyển kết hợp
                absoluteX = testPos.x;
                absoluteY = testPos.y;
                return;
            }
        }
    }

    // Phương thức điều khiển
    public void setRequestedDirection(int direction) {
        if (!isStopped) { // Chỉ cho phép điều khiển khi chưa bị dừng
            this.requestedDirection = direction;
            this.currentDirection = direction;
        }
    }

    public void clearRequestedDirection() {
        this.requestedDirection = GameConstants.DIRECTION_NONE;
    }

    // Di chuyển đến một điểm cụ thể
    public void moveToPoint(double targetX, double targetY, GameMap gameMap) {
        if (isStopped) return; // Không cho phép di chuyển khi bị dừng

        double offsetX = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_WIDTH) / 2;
        double offsetY = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_HEIGHT) / 2;

        if (gameMap.isWalkableWithBounds(targetX + offsetX, targetY + offsetY, COLLISION_WIDTH, COLLISION_HEIGHT)) {
            this.targetX = targetX;
            this.targetY = targetY;
            this.hasTarget = true;
            this.isMoving = true;
        }
    }

    private void moveToTarget() {
        if (!hasTarget) return;

        double dx = targetX - absoluteX;
        double dy = targetY - absoluteY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= GameConstants.PLAYER_SPEED) {
            absoluteX = targetX;
            absoluteY = targetY;
            isMoving = false;
            hasTarget = false;
        } else {
            float moveX = (float)((dx / distance) * GameConstants.PLAYER_SPEED);
            float moveY = (float)((dy / distance) * GameConstants.PLAYER_SPEED);

            float[][] stepMatrix = MatrixUtils.createTranslationMatrix(moveX, moveY);
            Point2D currentPos = new Point2D(absoluteX, absoluteY);
            Point2D newPos = MatrixUtils.applyTransformation(currentPos, stepMatrix);

            absoluteX = newPos.x;
            absoluteY = newPos.y;
        }
    }

    // Phương thức biến đổi hình dạng
    public boolean scaleUp() {
        if (currentScaleFactor * 2.0 <= GameConstants.MAX_SCALE_FACTOR) {
            applyShapeScale(2.0f);
            return true;
        }
        return false;
    }

    public boolean scaleDown() {
        if (currentScaleFactor * 0.5 >= GameConstants.MIN_SCALE_FACTOR) {
            applyShapeScale(0.5f);
            return true;
        }
        return false;
    }

    public void shearX(double factor) {
        float[][] shearMatrix = MatrixUtils.createShearXMatrix((float)factor);
        float[][] transformAroundFixedPoint = MatrixUtils.createTransformationAroundPoint(
                shearMatrix, (float)GameConstants.FIXED_POINT_X, (float)GameConstants.FIXED_POINT_Y);
        shapeTransformationMatrix = MatrixUtils.multiplyMatrix(transformAroundFixedPoint, shapeTransformationMatrix);
        applyShapeTransformation();
    }

    public void shearY(double factor) {
        float[][] shearMatrix = MatrixUtils.createShearYMatrix((float)factor);
        float[][] transformAroundFixedPoint = MatrixUtils.createTransformationAroundPoint(
                shearMatrix, (float)GameConstants.FIXED_POINT_X, (float)GameConstants.FIXED_POINT_Y);
        shapeTransformationMatrix = MatrixUtils.multiplyMatrix(transformAroundFixedPoint, shapeTransformationMatrix);
        applyShapeTransformation();
    }

    private void applyShapeScale(float scaleFactor) {
        float[][] scaleMatrix = MatrixUtils.createScaleMatrix(scaleFactor, scaleFactor);
        float[][] transformAroundFixedPoint = MatrixUtils.createTransformationAroundPoint(
                scaleMatrix, (float)GameConstants.FIXED_POINT_X, (float)GameConstants.FIXED_POINT_Y);
        shapeTransformationMatrix = MatrixUtils.multiplyMatrix(transformAroundFixedPoint, shapeTransformationMatrix);
        currentScaleFactor *= scaleFactor;
        applyShapeTransformation();
    }

    private void applyShapeTransformation() {
        transformedShape = MatrixUtils.applyMatrixToShape(originalShape, shapeTransformationMatrix);
    }

    // Getters
    public double getX() { return absoluteX; }
    public double getY() { return absoluteY; }
    public boolean isMoving() { return isMoving || requestedDirection != GameConstants.DIRECTION_NONE; }
    public boolean isStopped() { return isStopped; }
    public List<Point2D> getTransformedShape() { return transformedShape; }
    public double getCollisionWidth() { return COLLISION_WIDTH; }
    public double getCollisionHeight() { return COLLISION_HEIGHT; }
}
