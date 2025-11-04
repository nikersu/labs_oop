package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PointTest {
    @Test
    public void testPointCreation() {
        // создание точки с положительными координатами
        Point point = new Point(2.5, 3.7);
        assertEquals(2.5, point.x, 1e-10);
        assertEquals(3.7, point.y, 1e-10);
    }
    @Test
    public void testPointWithNegativeCoordinates() {
        // создание точки с отрицательными координатами
        Point point = new Point(-1.5, -2.5);
        assertEquals(-1.5, point.x, 1e-10);
        assertEquals(-2.5, point.y, 1e-10);
    }
    @Test
    public void testPointWithZeroCoordinates() {
        // создание точки с нулевыми координатами
        Point point = new Point(0.0, 0.0);
        assertEquals(0.0, point.x, 1e-10);
        assertEquals(0.0, point.y, 1e-10);
    }
    @Test
    public void testPointWithMaxMinValues() {
        // создание точки с предельными значениями double
        Point point1 = new Point(Double.MAX_VALUE, Double.MIN_VALUE);
        assertEquals(Double.MAX_VALUE, point1.x, 1e-10);
        assertEquals(Double.MIN_VALUE, point1.y, 1e-10);

        Point point2 = new Point(Double.MIN_VALUE, Double.MAX_VALUE);
        assertEquals(Double.MIN_VALUE, point2.x, 1e-10);
        assertEquals(Double.MAX_VALUE, point2.y, 1e-10);
    }
    @Test
    public void testPointWithNaNValues() {
        // создание точки со специальными значениями NaN
        Point point = new Point(Double.NaN, Double.NaN);
        assertTrue(Double.isNaN(point.x));
        assertTrue(Double.isNaN(point.y));
    }
    @Test
    public void testPointWithInfinityValues() {
        // создание точки с бесконечными значениями
        Point point1 = new Point(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, point1.x);
        assertEquals(Double.NEGATIVE_INFINITY, point1.y);

        Point point2 = new Point(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertEquals(Double.NEGATIVE_INFINITY, point2.x);
        assertEquals(Double.POSITIVE_INFINITY, point2.y);
    }
    @Test
    public void testMultiplePointInstances() {
        // создание нескольких независимых экземпляров точек
        Point point1 = new Point(1.0, 2.0);
        Point point2 = new Point(3.0, 4.0);
        Point point3 = new Point(1.0, 2.0);

        // каждый экземпляр должен сохранять свое собственное состояние
        assertEquals(1.0, point1.x, 1e-10);
        assertEquals(2.0, point1.y, 1e-10);

        assertEquals(3.0, point2.x, 1e-10);
        assertEquals(4.0, point2.y, 1e-10);

        assertEquals(1.0, point3.x, 1e-10);
        assertEquals(2.0, point3.y, 1e-10);
    }
    @Test
    public void testPointPrecision() {
        // числа с высокой точности
        Point point = new Point(1.23456789012345, 9.87654321098765);
        assertEquals(1.23456789012345, point.x, 1e-14);
        assertEquals(9.87654321098765, point.y, 1e-14);
    }
}