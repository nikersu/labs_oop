package repository;

import models.Point;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Тесты репозитория точек с генерацией данных")
public class PointRepositoryTest {
    private static PointRepository repository;
    private final Integer testFunctionId = 1;
    Double testXValue = 5.5;

    @BeforeAll
    static void setUp() {
        repository = new PointRepository();
    }

    @Test
    @Order(1)
    @DisplayName("Генерация и добавление разнообразных точек")
    void testInsertDiversePoints() throws SQLException {
        // Генерация разнообразных данных точек
        double[] xValues = {0.0, 1.5, -3.14, 100.0, 0.001, -999.999, 2.71, 0.0};
        double[] yValues = {1.0, -2.5, 0.0, 255.5, 0.0001, 888.888, 3.14, 0.0};
        Integer[] functionIds = {1, 1, 1, 2, 2, 3, 3, 4};

        // Добавляем несколько точек
        for (int i = 0; i < xValues.length; i++) {
            Point point = new Point();
            point.setFunctionId(functionIds[i]);
            point.setXValue(xValues[i]);
            point.setYValue(yValues[i]);

            repository.insert(point);

            System.out.println("Добавлена точка: function_id=" + functionIds[i] +
                    ", x=" + xValues[i] + ", y=" + yValues[i]);
        }
    }

    @Test
    @Order(2)
    @DisplayName("Поиск всех точек функции")
    void testFindByFunctionId() throws SQLException {
        List<Point> points = repository.findByFunctionId(testFunctionId);

        assertNotNull(points, "Список точек не должен быть null");
        assertFalse(points.isEmpty(), "Должна быть хотя бы одна точка для function_id=" + testFunctionId);

        // Мы добавили 3 точки для function_id=1 (индексы 0,1,2 в массивах выше)
        assertTrue(points.size() >= 3, "Должно быть минимум 3 точки для function_id=" + testFunctionId);

        System.out.println("Найдено точек для function_id=" + testFunctionId + ": " + points.size());
        points.forEach(p -> System.out.println("  - x=" + p.getXValue() + ", y=" + p.getYValue()));
    }

    @Test
    @Order(3)
    @DisplayName("Поиск конкретной точки по function_id и x_value")
    void testFindByFunctionIdAndX() throws SQLException {
        // Сначала добавляем тестовую точку для поиска
        Point testPoint = new Point();
        testPoint.setFunctionId(testFunctionId);
        testPoint.setXValue(testXValue);
        testPoint.setYValue(10.2);
        repository.insert(testPoint);

        // Ищем точку
        Point found = repository.findByFunctionIdAndX(testFunctionId, testXValue);

        assertNotNull(found, "Точка должна быть найдена");
        assertEquals(testFunctionId, found.getFunctionId(), "function_id должны совпадать");
        assertEquals(testXValue, found.getXValue(), "x_value должны совпадать");
        assertEquals(10.2, found.getYValue(), "y_value должны совпадать");

        System.out.println("Найдена точка: function_id=" + testFunctionId +
                ", x=" + testXValue + ", y=" + 10.2);
    }

    @Test
    @Order(4)
    @DisplayName("Обновление точки")
    void testUpdate() throws SQLException {
        // Добавляем точку для обновления
        Point point = new Point();
        point.setFunctionId(testFunctionId);
        point.setXValue(7.7);
        point.setYValue(15.0);
        repository.insert(point);

        // Обновляем точку
        Point updatedPoint = new Point();
        updatedPoint.setFunctionId(testFunctionId);
        updatedPoint.setXValue(7.7);
        updatedPoint.setYValue(30.5); // Новое значение Y

        boolean success = repository.update(updatedPoint);
        assertTrue(success, "Обновление должно быть успешным");

        // Проверяем, что обновилось
        Point found = repository.findByFunctionIdAndX(testFunctionId, 7.7);
        assertNotNull(found);
        assertEquals(30.5, found.getYValue(), "y_value должен быть обновлен");

        System.out.println("Точка обновлена: x=" + 7.7 + ", y=" + 15.0 + " → " + 30.5);
    }

    @Test
    @Order(5)
    @DisplayName("Удаление конкретной точки")
    void testDelete() throws SQLException {
        // Создаем временную точку для удаления
        Point tempPoint = new Point();
        tempPoint.setFunctionId(99);
        tempPoint.setXValue(123.456);
        tempPoint.setYValue(789.012);
        repository.insert(tempPoint);

        // Удаляем
        boolean deleted = repository.delete(99, 123.456);
        assertTrue(deleted, "Удаление должно быть успешным");

        // Проверяем, что удалена
        Point found = repository.findByFunctionIdAndX(99, 123.456);
        assertNull(found, "Точка должна быть удалена из БД");

        System.out.println("Точка function_id=99, x=123.456 успешно удалена");
    }

    @Test
    @Order(6)
    @DisplayName("Поиск несуществующей точки")
    void testFindNonExistentPoint() throws SQLException {
        Point point = repository.findByFunctionIdAndX(999999, 999.999);
        assertNull(point, "Несуществующая точка должна возвращать null");

        System.out.println("Поиск несуществующей точки возвращает null (корректно)");
    }

    @Test
    @Order(7)
    @DisplayName("Обновление несуществующей точки")
    void testUpdateNonExistentPoint() throws SQLException {
        Point nonExistent = new Point();
        nonExistent.setFunctionId(999999);
        nonExistent.setXValue(999.999);
        nonExistent.setYValue(888.888);

        boolean success = repository.update(nonExistent);
        assertFalse(success, "Обновление несуществующей точки должно возвращать false");

        System.out.println("Обновление несуществующей точки возвращает false (корректно)");
    }

    @Test
    @Order(8)
    @DisplayName("Генерация точек с крайними значениями")
    void testInsertEdgeCases() throws SQLException {
        // Тест с разными edge case данными

        // 1. Ноль и отрицательные значения
        Point zeroPoint = new Point();
        zeroPoint.setFunctionId(100);
        zeroPoint.setXValue(0.0);
        zeroPoint.setYValue(0.0);
        repository.insert(zeroPoint);

        // 2. Очень большие значения
        Point largePoint = new Point();
        largePoint.setFunctionId(100);
        largePoint.setXValue(999999.999999);
        largePoint.setYValue(-888888.888888);
        repository.insert(largePoint);

        // 3. Очень маленькие значения
        Point smallPoint = new Point();
        smallPoint.setFunctionId(100);
        smallPoint.setXValue(0.000001);
        smallPoint.setYValue(-0.000001);
        repository.insert(smallPoint);

        // 4. Идентичные x для разных функций
        Point sameX1 = new Point();
        sameX1.setFunctionId(101);
        sameX1.setXValue(5.0);
        sameX1.setYValue(10.0);
        repository.insert(sameX1);

        Point sameX2 = new Point();
        sameX2.setFunctionId(102);
        sameX2.setXValue(5.0);
        sameX2.setYValue(20.0);
        repository.insert(sameX2);

        System.out.println("Добавлены точки с крайними значениями:");
        System.out.println("  - Нулевые значения: x=0, y=0");
        System.out.println("  - Большие значения: x=999999.999999, y=-888888.888888");
        System.out.println("  - Маленькие значения: x=0.000001, y=-0.000001");
        System.out.println("  - Идентичный x для разных function_id: x=5.0 (для function_id 101 и 102)");
    }

    @Test
    @Order(9)
    @DisplayName("Комплексный сценарий: генерация, поиск, изменение, удаление")
    void testComplexScenario() throws SQLException {
        System.out.println("\n=== Комплексный сценарий ===");

        // 1. Генерация набора точек
        System.out.println("1. Генерация 5 точек для function_id=200");
        for (int i = 1; i <= 5; i++) {
            Point p = new Point();
            p.setFunctionId(200);
            p.setXValue(i * 10.0);
            p.setYValue(i * 20.0);
            repository.insert(p);
            System.out.println("   Добавлена: x=" + (i * 10.0) + ", y=" + (i * 20.0));
        }

        // 2. Поиск всех точек
        System.out.println("\n2. Поиск всех точек function_id=200");
        List<Point> allPoints = repository.findByFunctionId(200);
        assertEquals(5, allPoints.size(), "Должно быть 5 точек");
        System.out.println("   Найдено: " + allPoints.size() + " точек");

        // 3. Поиск конкретной точки
        System.out.println("\n3. Поиск конкретной точки (x=30.0)");
        Point specific = repository.findByFunctionIdAndX(200, 30.0);
        assertNotNull(specific, "Точка должна быть найдена");
        assertEquals(60.0, specific.getYValue(), "y должен быть 60.0");
        System.out.println("   Найдена: x=30.0, y=60.0");

        // 4. Изменение точки
        System.out.println("\n4. Изменение точки (x=40.0)");
        Point toUpdate = new Point();
        toUpdate.setFunctionId(200);
        toUpdate.setXValue(40.0);
        toUpdate.setYValue(999.999); // Новое значение
        repository.update(toUpdate);

        Point updated = repository.findByFunctionIdAndX(200, 40.0);
        assertEquals(999.999, updated.getYValue(), "y должен обновиться");
        System.out.println("   Обновлена: x=40.0, y=80.0 → 999.999");

        // 5. Удаление точки
        System.out.println("\n5. Удаление точки (x=10.0)");
        repository.delete(200, 10.0);

        List<Point> afterDelete = repository.findByFunctionId(200);
        assertEquals(4, afterDelete.size(), "Должно остаться 4 точки");
        System.out.println("   Удалена точка x=10.0, осталось: " + afterDelete.size() + " точек");

        // 6. Проверка удаленной точки
        System.out.println("\n6. Проверка удаленной точки");
        Point deleted = repository.findByFunctionIdAndX(200, 10.0);
        assertNull(deleted, "Удаленная точка не должна находиться");
        System.out.println("   Удаленная точка не найдена (корректно)");
    }
}