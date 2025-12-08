package repository;

import DTO.Point;
import JDBC.repository.PointRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PointRepositoryTest {
    private PointRepository pointRepository;
    private final Integer functionId1 = 1;
    private final Integer functionId2 = 2;

    @BeforeEach
    void setUp() {
        pointRepository = new PointRepository();
        // Очищаем таблицу перед тестами
        clearPointsTable();

        // Добавляем тестовые точки
        pointRepository.insert(new Point(functionId1, 1.0, 2.0));
        pointRepository.insert(new Point(functionId1, 2.0, 4.0));
        pointRepository.insert(new Point(functionId1, 3.0, 6.0));
        pointRepository.insert(new Point(functionId2, 1.0, 1.0));
        pointRepository.insert(new Point(functionId2, 2.0, 2.0));
    }

    @AfterEach
    void tearDown() {
        // Очищаем таблицу после тестов
        clearPointsTable();
    }

    @Test
    void testInsert() {
        Point newPoint = new Point(functionId1, 4.0, 8.0);
        pointRepository.insert(newPoint);

        List<Point> points = pointRepository.findByFunctionId(functionId1);
        assertEquals(4, points.size());

        // Проверяем, что новая точка добавлена
        boolean found = false;
        for (Point p : points) {
            if (p.getXValue() == 4.0 && p.getYValue() == 8.0) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    void testFindByFunctionId() {
        List<Point> points1 = pointRepository.findByFunctionId(functionId1);
        assertEquals(3, points1.size());

        List<Point> points2 = pointRepository.findByFunctionId(functionId2);
        assertEquals(2, points2.size());

        // Проверяем данные первой функции
        boolean found1 = false, found2 = false, found3 = false;
        for (Point p : points1) {
            if (p.getXValue() == 1.0 && p.getYValue() == 2.0) found1 = true;
            if (p.getXValue() == 2.0 && p.getYValue() == 4.0) found2 = true;
            if (p.getXValue() == 3.0 && p.getYValue() == 6.0) found3 = true;
        }
        assertTrue(found1);
        assertTrue(found2);
        assertTrue(found3);
    }

    @Test
    void testFindByFunctionIdEmpty() {
        List<Point> points = pointRepository.findByFunctionId(999);
        assertNotNull(points);
        assertTrue(points.isEmpty());
    }

    @Test
    void testFindByFunctionIdAndX() {
        Point point = pointRepository.findByFunctionIdAndX(functionId1, 2.0);
        assertNotNull(point);
        assertEquals(functionId1, point.getFunctionId());
        assertEquals(2.0, point.getXValue(), 0.001);
        assertEquals(4.0, point.getYValue(), 0.001);
    }

    @Test
    void testFindByFunctionIdAndXNonExistent() {
        Point point = pointRepository.findByFunctionIdAndX(functionId1, 999.0);
        assertNull(point);

        Point point2 = pointRepository.findByFunctionIdAndX(999, 1.0);
        assertNull(point2);
    }

    @Test
    void testUpdate() {
        Point pointToUpdate = pointRepository.findByFunctionIdAndX(functionId1, 2.0);
        assertNotNull(pointToUpdate);

        pointToUpdate.setYValue(10.0);
        boolean updateResult = pointRepository.update(pointToUpdate);
        assertTrue(updateResult);

        Point updatedPoint = pointRepository.findByFunctionIdAndX(functionId1, 2.0);
        assertNotNull(updatedPoint);
        assertEquals(10.0, updatedPoint.getYValue(), 0.001);
    }

    @Test
    void testUpdateNonExistent() {
        Point nonExistentPoint = new Point(999, 999.0, 999.0);
        boolean updateResult = pointRepository.update(nonExistentPoint);
        assertFalse(updateResult);
    }

    @Test
    void testDelete() {
        boolean deleteResult = pointRepository.delete(functionId1, 2.0);
        assertTrue(deleteResult);

        Point deletedPoint = pointRepository.findByFunctionIdAndX(functionId1, 2.0);
        assertNull(deletedPoint);

        List<Point> points = pointRepository.findByFunctionId(functionId1);
        assertEquals(2, points.size());
    }

    @Test
    void testDeleteNonExistent() {
        boolean deleteResult = pointRepository.delete(999, 999.0);
        assertFalse(deleteResult);
    }

    @Test
    void testFindAllSortedByX() {
        clearPointsTable();

        // Добавляем точки в разном порядке по X
        pointRepository.insert(new Point(1, 3.0, 30.0));
        pointRepository.insert(new Point(1, 1.0, 10.0));
        pointRepository.insert(new Point(1, 2.0, 20.0));

        List<Point> sortedPoints = pointRepository.findAllSortedByX();

        assertEquals(3, sortedPoints.size());
        assertEquals(1.0, sortedPoints.get(0).getXValue(), 0.001);
        assertEquals(2.0, sortedPoints.get(1).getXValue(), 0.001);
        assertEquals(3.0, sortedPoints.get(2).getXValue(), 0.001);
    }

    @Test
    void testFindAllSortedByY() {
        clearPointsTable();

        // Добавляем точки в разном порядке по Y
        pointRepository.insert(new Point(1, 10.0, 3.0));
        pointRepository.insert(new Point(1, 20.0, 1.0));
        pointRepository.insert(new Point(1, 30.0, 2.0));

        List<Point> sortedPoints = pointRepository.findAllSortedByY();

        assertEquals(3, sortedPoints.size());
        assertEquals(1.0, sortedPoints.get(0).getYValue(), 0.001);
        assertEquals(2.0, sortedPoints.get(1).getYValue(), 0.001);
        assertEquals(3.0, sortedPoints.get(2).getYValue(), 0.001);
    }

    @Test
    void testFullCrudCycle() {
        // Create
        Point newPoint = new Point(100, 5.0, 25.0);
        pointRepository.insert(newPoint);
        // Read
        List<Point> points = pointRepository.findByFunctionId(100);
        assertEquals(1, points.size());
        assertEquals(5.0, points.getFirst().getXValue(), 0.001);
        assertEquals(25.0, points.getFirst().getYValue(), 0.001);
        // Update
        Point pointToUpdate = points.getFirst();
        pointToUpdate.setYValue(30.0);
        assertTrue(pointRepository.update(pointToUpdate));
        Point updatedPoint = pointRepository.findByFunctionIdAndX(100, 5.0);
        assertEquals(30.0, updatedPoint.getYValue(), 0.001);
        // Delete
        assertTrue(pointRepository.delete(100, 5.0));
        assertNull(pointRepository.findByFunctionIdAndX(100, 5.0));
    }

    // Вспомогательный метод для очистки таблицы
    private void clearPointsTable() {
        // Находим все уникальные functionId и удаляем их точки
        // Простой способ: удаляем все точки для test functionId
        for (int i = 1; i <= 10; i++) {
            List<Point> points = pointRepository.findByFunctionId(i);
            for (Point point : points) {
                pointRepository.delete(i, point.getXValue());
            }
        }
    }
}