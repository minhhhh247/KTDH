package pacmangame.utils;

import pacmangame.model.Point2D;
import java.util.List;
import java.util.ArrayList;

public class MatrixUtils {

    public static float[][] multiplyMatrix(float[][] a, float[][] b) {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = 0;
                for (int k = 0; k < 3; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    public static Point2D applyTransformation(Point2D point, float[][] matrix) {
        // SỬA: sửa lại công thức ma trận
        float x = (float)(point.x * matrix[0][0] + point.y * matrix[1][0] + matrix[2][0]);
        float y = (float)(point.x * matrix[0][1] + point.y * matrix[1][1] + matrix[2][1]);
        return new Point2D(x, y);
    }

    public static float[][] createTranslationMatrix(float dx, float dy) {
        return new float[][]{
                {1, 0, 0},
                {0, 1, 0},
                {dx, dy, 1}
        };
    }

    public static float[][] createScaleMatrix(float sx, float sy) {
        return new float[][]{
                {sx, 0, 0},
                {0, sy, 0},
                {0, 0, 1}
        };
    }

    public static float[][] createShearXMatrix(float shx) {
        return new float[][]{
                {1, 0, 0},
                {shx, 1, 0},
                {0, 0, 1}
        };
    }

    public static float[][] createShearYMatrix(float shy) {
        return new float[][]{
                {1, shy, 0},
                {0, 1, 0},
                {0, 0, 1}
        };
    }

    public static float[][] createIdentityMatrix() {
        return new float[][]{
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        };
    }

    public static float[][] createTransformationAroundPoint(float[][] transformMatrix, float cx, float cy) {
        float[][] toOrigin = {
                {1, 0, 0},
                {0, 1, 0},
                {-cx, -cy, 1}
        };

        float[][] backToPosition = {
                {1, 0, 0},
                {0, 1, 0},
                {cx, cy, 1}
        };

        float[][] temp = multiplyMatrix(transformMatrix, toOrigin);
        return multiplyMatrix(backToPosition, temp);
    }

    public static List<Point2D> applyMatrixToShape(List<Point2D> shape, float[][] matrix) {
        List<Point2D> transformedShape = new ArrayList<>();
        for (Point2D point : shape) {
            transformedShape.add(applyTransformation(point, matrix));
        }
        return transformedShape;
    }
}
