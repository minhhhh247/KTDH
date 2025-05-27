package pacmangame.model;

import pacmangame.constants.GameConstants;
import pacmangame.utils.MatrixUtils;
import java.util.ArrayList;
import java.util.List;

public class ScalableMapElement {
    private double x, y;
    private int type;
    private double currentSize;
    private double targetSize;
    private boolean isScaling;
    private boolean isGrowing;

    // Thêm delay timer
    private long lastScaleTime = 0;
    private static final long SCALE_DELAY = 5000; // 5 giây delay trước khi bắt đầu biến đổi

    // Hình dạng và ma trận biến đổi
    private List<Point2D> originalShape;
    private List<Point2D> transformedShape;
    private float[][] transformationMatrix;

    public ScalableMapElement(double x, double y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;

        // Khởi tạo kích thước ban đầu
        if (type == 2) {
            this.currentSize = GameConstants.INITIAL_TYPE2_SIZE;
            this.isGrowing = true;
        } else if (type == 3) {
            this.currentSize = GameConstants.INITIAL_TYPE3_SIZE;
            this.isGrowing = false;
        }

        this.targetSize = currentSize;
        this.isScaling = false;
        this.lastScaleTime = System.currentTimeMillis(); // Khởi tạo timer

        initializeShape();
        updateTransformation();
    }

    // Các phương thức khác giữ nguyên...
    private void initializeShape() {
        originalShape = new ArrayList<>();
        originalShape.add(new Point2D(0, 0));
        originalShape.add(new Point2D(1, 0));
        originalShape.add(new Point2D(1, 1));
        originalShape.add(new Point2D(0, 1));

        transformedShape = new ArrayList<>();
        for (Point2D point : originalShape) {
            transformedShape.add(new Point2D(point));
        }

        transformationMatrix = MatrixUtils.createIdentityMatrix();
    }

    public void startScaling() {
        if (isScaling) return;

        isScaling = true;

        if (type == 2) {
            if (isGrowing) {
                targetSize = Math.min(currentSize * GameConstants.SCALE_FACTOR, GameConstants.GRID_SIZE);
                if (targetSize >= GameConstants.GRID_SIZE) {
                    isGrowing = false;
                }
            } else {
                targetSize = Math.max(currentSize / GameConstants.SCALE_FACTOR, GameConstants.INITIAL_TYPE2_SIZE);
                if (targetSize <= GameConstants.INITIAL_TYPE2_SIZE) {
                    isGrowing = true;
                }
            }
        } else if (type == 3) {
            if (isGrowing) {
                targetSize = Math.min(currentSize * GameConstants.SCALE_FACTOR, GameConstants.GRID_SIZE);
                if (targetSize >= GameConstants.GRID_SIZE) {
                    isGrowing = false;
                }
            } else {
                targetSize = Math.max(currentSize / GameConstants.SCALE_FACTOR, GameConstants.INITIAL_TYPE2_SIZE);
                if (targetSize <= GameConstants.INITIAL_TYPE2_SIZE) {
                    isGrowing = true;
                }
            }
        }
    }

    // Sửa phương thức update() để thêm delay
    public void update() {
        long currentTime = System.currentTimeMillis();

        // Chỉ bắt đầu scaling nếu đã qua thời gian delay
        if (currentTime - lastScaleTime < SCALE_DELAY) {
            return; // Chưa đến lúc biến đổi
        }

        // Logic scaling hiện tại
        if (!isScaling) return;

        double scaleDiff = targetSize - currentSize;
        if (Math.abs(scaleDiff) < 0.1) {
            currentSize = targetSize;
            isScaling = false;
            lastScaleTime = currentTime; // Reset delay timer
        } else {
            currentSize += scaleDiff * 0.05;
        }

        updateTransformation();
    }

    private void updateTransformation() {
        float scaleX = (float)currentSize;
        float scaleY = (float)currentSize;

        float[][] scaleMatrix = MatrixUtils.createScaleMatrix(scaleX, scaleY);

        float centerX = (float)(x + GameConstants.GRID_SIZE / 2.0);
        float centerY = (float)(y + GameConstants.GRID_SIZE / 2.0);

        transformationMatrix = MatrixUtils.createTransformationAroundPoint(scaleMatrix, centerX, centerY);
        transformedShape = MatrixUtils.applyMatrixToShape(originalShape, transformationMatrix);
    }

    // Sửa phương thức isBlocking() để cả type 2 và 3 đều chặn khi = 24
    public boolean isBlocking() {
        // Cả type 2 và type 3 đều chặn khi kích thước bằng 24
        if (type == 2 || type == 3) {
            return currentSize == GameConstants.GRID_SIZE;
        }
        return false;
    }

    public boolean checkCollision(double checkX, double checkY, double width, double height) {
        if (!isBlocking()) return false;

        double elementCenterX = x + GameConstants.GRID_SIZE / 2.0;
        double elementCenterY = y + GameConstants.GRID_SIZE / 2.0;
        double halfSize = currentSize / 2.0;

        double elementLeft = elementCenterX - halfSize;
        double elementRight = elementCenterX + halfSize;
        double elementTop = elementCenterY - halfSize;
        double elementBottom = elementCenterY + halfSize;

        double checkRight = checkX + width;
        double checkBottom = checkY + height;

        return !(checkX > elementRight || checkRight < elementLeft ||
                checkY > elementBottom || checkBottom < elementTop);
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getType() { return type; }
    public double getCurrentSize() { return currentSize; }
    public List<Point2D> getTransformedShape() { return transformedShape; }
    public boolean isScaling() { return isScaling; }
}
