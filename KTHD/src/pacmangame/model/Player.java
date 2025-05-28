package pacmangame.model;

import pacmangame.constants.GameConstants;
import pacmangame.utils.MatrixUtils;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private double absoluteX, absoluteY;
    private double targetX, targetY;
    private boolean isMoving = false;
    private boolean hasTarget = false;
    private boolean isStopped = false;

    private int currentDirection = GameConstants.DIRECTION_NONE;
    private int requestedDirection = GameConstants.DIRECTION_NONE;

    private final double COLLISION_WIDTH = GameConstants.PLAYER_COLLISION_SIZE;
    private final double COLLISION_HEIGHT = GameConstants.PLAYER_COLLISION_SIZE;

    private double currentScaleFactor = 1.0;
    private float[][] shapeTransformationMatrix;

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
        if (isStopped) return;

        handleContinuousMovement(gameMap);

        if (hasTarget) {
            moveToTarget();
        }
    }

    public void stopMoving() {
        isStopped = true;
        isMoving = false;
        hasTarget = false;
        requestedDirection = GameConstants.DIRECTION_NONE;
        currentDirection = GameConstants.DIRECTION_NONE;
    }

    public boolean checkCollisionWithGhost(Ghost ghost) {
        double playerCenterX = absoluteX + GameConstants.PLAYER_SQUARE_SIZE / 2;
        double playerCenterY = absoluteY + GameConstants.PLAYER_SQUARE_SIZE / 2;

        double ghostCenterX = ghost.getX() + GameConstants.PLAYER_SQUARE_SIZE / 2;
        double ghostCenterY = ghost.getY() + GameConstants.PLAYER_SQUARE_SIZE / 2;

        double distance = Math.sqrt(
                Math.pow(playerCenterX - ghostCenterX, 2) +
                        Math.pow(playerCenterY - ghostCenterY, 2)
        );

        double collisionDistance = (GameConstants.PLAYER_SQUARE_SIZE + GameConstants.PLAYER_SQUARE_SIZE) / 2 * 0.8;
        return distance < collisionDistance;
    }

    private void handleContinuousMovement(GameMap gameMap) {
        if (requestedDirection != GameConstants.DIRECTION_NONE) {
            moveInDirectionWithMatrix(requestedDirection, gameMap);
        }
    }

    private void moveInDirectionWithMatrix(int direction, GameMap gameMap) {
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

        float[][] translationMatrix = MatrixUtils.createTranslationMatrix(dx, dy);
        Point2D currentPos = new Point2D(absoluteX, absoluteY);
        Point2D newPos = MatrixUtils.applyTransformation(currentPos, translationMatrix);

        double offsetX = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_WIDTH) / 2;
        double offsetY = (GameConstants.PLAYER_SQUARE_SIZE - COLLISION_HEIGHT) / 2;

        if (gameMap.isWalkableWithBounds(newPos.x + offsetX, newPos.y + offsetY,
                COLLISION_WIDTH, COLLISION_HEIGHT)) {
            absoluteX = newPos.x;
            absoluteY = newPos.y;
        }
    }

    public void setRequestedDirection(int direction) {
        if (!isStopped) {
            this.requestedDirection = direction;
            this.currentDirection = direction;
        }
    }

    public void clearRequestedDirection() {
        this.requestedDirection = GameConstants.DIRECTION_NONE;
    }

    public void moveToPoint(double targetX, double targetY, GameMap gameMap) {
        if (isStopped) return;

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

    public double getX() { return absoluteX; }
    public double getY() { return absoluteY; }
    public boolean isMoving() { return isMoving || requestedDirection != GameConstants.DIRECTION_NONE; }
    public boolean isStopped() { return isStopped; }
    public List<Point2D> getTransformedShape() { return transformedShape; }
    public double getCollisionWidth() { return COLLISION_WIDTH; }
    public double getCollisionHeight() { return COLLISION_HEIGHT; }
}
