package operations;

import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.Point;
import functions.TabulatedFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TabulatedFunctionOperationServiceTest {

    @Test
    public void testAsPointsWithArrayTabulatedFunction() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};

        // создаем табулированную функцию на основе массива
        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        // преобразуем функцию в массив точек
        Point[] points = TabulatedFunctionOperationService.asPoints(function);
        // проверка на корректность преобразования
        assertEquals(xValues.length, points.length);

        for (int i = 0; i < points.length; i++) {
            assertEquals(xValues[i], points[i].x, 1e-10);
            assertEquals(yValues[i], points[i].y, 1e-10);
        }
    }

    @Test
    public void testAsPointsWithLinkedListTabulatedFunction() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0};
        // создаем табулированную функцию на основе связного списка
        TabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        // преобразуем функцию в массив точек
        Point[] points = TabulatedFunctionOperationService.asPoints(function);

        // проверка на корректность преобразования
        assertEquals(xValues.length, points.length);
        for (int i = 0; i < points.length; i++) {
            assertEquals(xValues[i], points[i].x, 1e-10);
            assertEquals(yValues[i], points[i].y, 1e-10);
        }
    }

    @Test
    public void testAsPointsWithSinglePoint() {
        // тест для функции с минимальным количеством точек
        double[] xValues = {1.0, 2.0};
        double[] yValues = {3.0, 4.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        Point[] points = TabulatedFunctionOperationService.asPoints(function);

        assertEquals(2, points.length);
        assertEquals(1.0, points[0].x, 1e-10);
        assertEquals(3.0, points[0].y, 1e-10);
        assertEquals(2.0, points[1].x, 1e-10);
        assertEquals(4.0, points[1].y, 1e-10);
    }

    @Test
    public void testAsPointsOrder() {
        // проверка порядка точек в массиве
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        Point[] points = TabulatedFunctionOperationService.asPoints(function);

        // проверка, что точки идут в правильном порядке
        assertEquals(1.0, points[0].x, 1e-10);
        assertEquals(10.0, points[0].y, 1e-10);
        assertEquals(2.0, points[1].x, 1e-10);
        assertEquals(20.0, points[1].y, 1e-10);
        assertEquals(3.0, points[2].x, 1e-10);
        assertEquals(30.0, points[2].y, 1e-10);
    }
}