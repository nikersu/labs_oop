package operations;

import functions.Point;
import functions.TabulatedFunction;

public class TabulatedFunctionOperationService {
    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        // создаем массив точек размером, равным количеству точек в функции
        Point[] points = new Point[tabulatedFunction.getCount()];
        int i = 0;
        // цикл for-each для итерации по точкам функции
        for (Point point : tabulatedFunction) {
            points[i] = point;
            i++;
        }
        return points;
    }
}