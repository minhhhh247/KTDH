package pacmangame.utils;

import pacmangame.constants.GameConstants;
import pacmangame.model.Point2D;
import java.util.List;
import java.util.ArrayList;

public class MatrixUtils {

    // Nhân hai ma trận 3x3 (giống multiplyMatrix trong C++)
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

    // Áp dụng ma trận lên điểm (giống applyTransformation trong C++)
    public static Point2D applyTransformation(Point2D point, float[][] matrix) {
        float x = (float)(point.x * matrix[0][0] + point.y * matrix[1][0] + matrix[2][0]);
        float y = (float)(point.x * matrix[0][1] + point.y * matrix[1][1] + matrix[2][1]);
        return new Point2D(x, y);
    }

    // Ma trận tịnh tiến (case 1 trong C++)
    public static float[][] createTranslationMatrix(float dx, float dy) {
        return new float[][]{
                {1, 0, 0},
                {0, 1, 0},
                {dx, dy, 1}
        };
    }

    // Ma trận scale (case 3 trong C++)
    public static float[][] createScaleMatrix(float sx, float sy) {
        return new float[][]{
                {sx, 0, 0},
                {0, sy, 0},
                {0, 0, 1}
        };
    }

    // Ma trận shear X (case 7 trong C++)
    public static float[][] createShearXMatrix(float shx) {
        return new float[][]{
                {1, 0, 0},
                {shx, 1, 0},
                {0, 0, 1}
        };
    }

    // Ma trận shear Y (case 8 trong C++)
    public static float[][] createShearYMatrix(float shy) {
        return new float[][]{
                {1, shy, 0},
                {0, 1, 0},
                {0, 0, 1}
        };
    }

    // Ma trận đơn vị
    public static float[][] createIdentityMatrix() {
        return new float[][]{
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        };
    }

    // Biến đổi quanh điểm cố định (case 2, 3 trong C++)
    public static float[][] createTransformationAroundPoint(float[][] transformMatrix, float cx, float cy) {
        // Ma trận tịnh tiến về gốc
        float[][] toOrigin = {
                {1, 0, 0},
                {0, 1, 0},
                {-cx, -cy, 1}
        };

        // Ma trận tịnh tiến ngược lại
        float[][] back = {
                {1, 0, 0},
                {0, 1, 0},
                {cx, cy, 1}
        };

        // Nhân ma trận: toOrigin * transformMatrix * back
        float[][] temp = multiplyMatrix(toOrigin, transformMatrix);
        return multiplyMatrix(temp, back);
    }

    // Áp dụng ma trận lên danh sách điểm
    public static List<Point2D> applyMatrixToShape(List<Point2D> shape, float[][] matrix) {
        List<Point2D> transformedShape = new ArrayList<>();
        for (Point2D point : shape) {
            transformedShape.add(applyTransformation(point, matrix));
        }
        return transformedShape;
    }
}
