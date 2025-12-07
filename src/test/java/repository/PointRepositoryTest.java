package repository;

import models.Point;
import models.Function;
import models.User;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Тесты репозитория точек с генерацией данных")
public class PointRepositoryTest {
    private static PointRepository pointRepository;
    private static FunctionRepository functionRepository;
    private static UserRepository userRepository;

    private static Integer testUserId;
    private static Integer testFunctionId;
    private static final Random random = new Random();

    @BeforeAll
    static void setUp() throws SQLException {
        pointRepository = new PointRepository();
        functionRepository = new FunctionRepository();
        userRepository = new UserRepository();

        // Создаем тестового пользователя
        User testUser = new User("point_test_user", "password123");
        testUserId = userRepository.insert(testUser);

        // Создаем тестовую функцию
        Function testFunction = new Function("Тестовая функция для точек", "x^2", testUserId);
        testFunctionId = functionRepository.insert(testFunction);

        System.out.println("Setup complete: User ID=" + testUserId + ", Function ID=" + testFunctionId);
    }

    @AfterAll
    static void tearDown() throws SQLException {
        // Очистка (каскадное удаление через user)
        if (testUserId != null) {
            userRepository.delete(testUserId);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Генерация разнообразных точек: разные типы функций")
    void testGenerateDiversePoints() throws SQLException {
        // Создаем несколько функций разных типов
        String[] functionTypes = {"Квадратичная", "Синусоида", "Линейная", "Экспонента", "Кубическая"};
        String[] expressions = {"x^2", "sin(x)", "2*x + 1", "exp(x)", "x^3 - x"};

        List<Integer> functionIds = new ArrayList<>();

        // Генерируем точки для каждой функции
        for (int i = 0; i < functionTypes.length; i++) {
            // Создаем функцию
            Function function = new Function(
                    functionTypes[i] + "_" + System.currentTimeMillis(),
                    expressions[i],
                    testUserId
            );
            Integer funcId = functionRepository.insert(function);
            functionIds.add(funcId);

            // Генерируем уникальные точки для каждой функции
            List<Point> points = generatePointsForFunctionType(funcId, functionTypes[i], expressions[i]);
            pointRepository.insertBatch(points);

            System.out.println("Создана функция '" + functionTypes[i] +
                    "' с " + points.size() + " точками");
        }

        // Проверяем, что точки создались
        for (Integer funcId : functionIds) {
            List<Point> retrievedPoints = pointRepository.findByFunctionId(funcId);
            assertFalse(retrievedPoints.isEmpty(), "Должны быть точки для функции " + funcId);
            assertTrue(retrievedPoints.size() >= 50, "Должно быть минимум 50 точек");
        }
    }

    @Test
    @Order(2)
    @DisplayName("Генерация точек с разными диапазонами X")
    void testPointsWithDifferentXRanges() throws SQLException {
        // Создаем функцию для этого теста
        Function rangeFunction = new Function(
                "RangeTest_" + System.currentTimeMillis(),
                "x",
                testUserId
        );
        Integer rangeFuncId = functionRepository.insert(rangeFunction);

        // Генерация точек в разных диапазонах
        generateAndTestPoints(rangeFuncId, -100.0, 100.0, 10.0, "Большой диапазон");
        generateAndTestPoints(rangeFuncId, -1.0, 1.0, 0.1, "Маленький диапазон высокой точности");
        generateAndTestPoints(rangeFuncId, 0.0, 10.0, 1.0, "Положительный диапазон");
        generateAndTestPoints(rangeFuncId, -10.0, 0.0, 0.5, "Отрицательный диапазон");
    }

    @Test
    @Order(3)
    @DisplayName("Генерация точек с разной плотностью")
    void testPointsWithDifferentDensity() throws SQLException {
        Integer densityFuncId = functionRepository.insert(
                new Function("DensityTest", "x^2", testUserId)
        );

        // Разная плотность точек
        int[] pointCounts = {10, 50, 100, 500, 1000};

        for (int count : pointCounts) {
            long startTime = System.currentTimeMillis();

            List<Point> densePoints = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                double x = random.nextDouble() * 20 - 10; // от -10 до 10
                double y = x * x; // y = x^2
                densePoints.add(new Point(densityFuncId, x, y));
            }

            pointRepository.insertBatch(densePoints);

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Сгенерировано " + count + " точек за " + duration + "ms");

            // Проверяем, что все точки добавлены
            List<Point> retrieved = pointRepository.findByFunctionId(densityFuncId);
            assertTrue(retrieved.size() >= count);
        }
    }

    @Test
    @Order(4)
    @DisplayName("Генерация точек с особыми значениями")
    void testPointsWithSpecialValues() throws SQLException {
        Integer specialFuncId = functionRepository.insert(
                new Function("SpecialValues", "1/x", testUserId)
        );

        List<Point> specialPoints = new ArrayList<>();

        // Особые значения X
        double[] specialXValues = {
                0.0, 0.0001, -0.0001,           // Около нуля
                1.0, -1.0,                      // Единицы
                Math.PI, Math.E,                // Константы
                Double.MAX_VALUE / 2,           // Большие значения
                Double.MIN_VALUE,               // Очень маленькие
                -Double.MAX_VALUE / 2           // Отрицательные большие
        };

        for (double x : specialXValues) {
            double y = 1.0 / (x == 0 ? 0.0000001 : x); // Избегаем деления на 0
            specialPoints.add(new Point(specialFuncId, x, y));
        }

        // Добавляем NaN и Infinity (если поддерживается)
        try {
            specialPoints.add(new Point(specialFuncId, Double.NaN, Double.NaN));
            specialPoints.add(new Point(specialFuncId, Double.POSITIVE_INFINITY, 0.0));
            specialPoints.add(new Point(specialFuncId, Double.NEGATIVE_INFINITY, 0.0));
        } catch (Exception e) {
            System.out.println("Специальные значения NaN/Infinity не поддерживаются: " + e.getMessage());
        }

        pointRepository.insertBatch(specialPoints);

        // Проверяем поиск по диапазону
        List<Point> nearZero = pointRepository.findInXRange(specialFuncId, -0.1, 0.1);
        assertFalse(nearZero.isEmpty());

        List<Point> positive = pointRepository.findInXRange(specialFuncId, 0.5, 2.0);
        assertFalse(positive.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("Генерация случайных данных для статистического анализа")
    void testRandomDataForStatisticalAnalysis() throws SQLException {
        Integer statsFuncId = functionRepository.insert(
                new Function("Statistical", "random", testUserId)
        );

        List<Point> randomPoints = new ArrayList<>();
        int sampleSize = 1000;

        // Генерация случайных точек с разными распределениями
        for (int i = 0; i < sampleSize; i++) {
            double x;
            double y;

            // Разные типы распределений
            switch (i % 4) {
                case 0: // Равномерное распределение
                    x = random.nextDouble() * 100 - 50;
                    y = random.nextDouble() * 100 - 50;
                    break;
                case 1: // Нормальное распределение
                    x = random.nextGaussian() * 10;
                    y = random.nextGaussian() * 10;
                    break;
                case 2: // Экспоненциальное
                    x = -Math.log(1 - random.nextDouble()) * 10;
                    y = -Math.log(1 - random.nextDouble()) * 10;
                    break;
                default: // Синусоидальный паттерн
                    x = i * 0.1;
                    y = Math.sin(x) + random.nextGaussian() * 0.1;
                    break;
            }

            randomPoints.add(new Point(statsFuncId, x, y));
        }

        // Пакетная вставка
        long startTime = System.currentTimeMillis();
        pointRepository.insertBatch(randomPoints);
        long insertTime = System.currentTimeMillis() - startTime;

        System.out.println("Вставка " + sampleSize + " случайных точек: " + insertTime + "ms");

        // Проверяем статистические характеристики
        List<Point> allPoints = pointRepository.findByFunctionId(statsFuncId);
        assertEquals(sampleSize, allPoints.size());

        // Вычисляем среднее X
        double sumX = allPoints.stream().mapToDouble(Point::getXValue).sum();
        double meanX = sumX / allPoints.size();
        System.out.println("Среднее X: " + meanX);

        // Диапазон X
        double minX = allPoints.stream().mapToDouble(Point::getXValue).min().orElse(0);
        double maxX = allPoints.stream().mapToDouble(Point::getXValue).max().orElse(0);
        System.out.println("Диапазон X: [" + minX + ", " + maxX + "]");
    }

    @Test
    @Order(6)
    @DisplayName("Тест поиска точек в диапазонах")
    void testFindInXRange() throws SQLException {
        Integer rangeTestFuncId = functionRepository.insert(
                new Function("RangeSearchTest", "linear", testUserId)
        );

        // Генерируем точки от 0 до 100 с шагом 1
        List<Point> linearPoints = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            linearPoints.add(new Point(rangeTestFuncId, (double) i, (double) i * 2));
        }
        pointRepository.insertBatch(linearPoints);

        // Тестируем разные диапазоны
        testRange(rangeTestFuncId, 0.0, 10.0, 11);   // 0-10 включительно
        testRange(rangeTestFuncId, 10.5, 20.5, 11);  // 11-20 включительно
        testRange(rangeTestFuncId, 90.0, 100.0, 11); // 90-100 включительно
        testRange(rangeTestFuncId, -10.0, -5.0, 0);  // Нет точек
        testRange(rangeTestFuncId, 50.0, 50.0, 1);   // Точно одна точка
    }

    @Test
    @Order(7)
    @DisplayName("Тест обновления точек")
    void testUpdatePoints() throws SQLException {
        Integer updateFuncId = functionRepository.insert(
                new Function("UpdateTest", "x^3", testUserId)
        );

        // Создаем несколько точек
        Point p1 = new Point(updateFuncId, 1.0, 1.0);
        Point p2 = new Point(updateFuncId, 2.0, 8.0);
        Point p3 = new Point(updateFuncId, 3.0, 27.0);

        pointRepository.insert(p1);
        pointRepository.insert(p2);
        pointRepository.insert(p3);

        // Обновляем Y значение
        p2.setYValue(64.0); // 2^3 = 8, меняем на 64
        boolean updated = pointRepository.update(p2);
        assertTrue(updated);

        // Проверяем обновление
        Point retrieved = pointRepository.findByFunctionIdAndX(updateFuncId, 2.0);
        assertNotNull(retrieved);
        assertEquals(64.0, retrieved.getYValue(), 0.0001);
    }

    @Test
    @Order(8)
    @DisplayName("Тест удаления точек")
    void testDeletePoints() throws SQLException {
        Integer deleteFuncId = functionRepository.insert(
                new Function("DeleteTest", "x^2", testUserId)
        );

        // Создаем точки
        for (int i = 1; i <= 10; i++) {
            pointRepository.insert(new Point(deleteFuncId, (double) i, (double) i * i));
        }

        // Удаляем конкретную точку
        boolean deleted = pointRepository.delete(deleteFuncId, 5.0);
        assertTrue(deleted);

        // Проверяем, что точка удалена
        Point found = pointRepository.findByFunctionIdAndX(deleteFuncId, 5.0);
        assertNull(found);

        // Проверяем, что остальные точки остались
        List<Point> remaining = pointRepository.findByFunctionId(deleteFuncId);
        assertEquals(9, remaining.size());

        // Удаляем все точки функции
        pointRepository.deleteByFunctionId(deleteFuncId);
        List<Point> afterDeleteAll = pointRepository.findByFunctionId(deleteFuncId);
        assertTrue(afterDeleteAll.isEmpty());
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private List<Point> generatePointsForFunctionType(Integer functionId, String type, String expression) {
        List<Point> points = new ArrayList<>();
        int pointCount = 50 + random.nextInt(50); // 50-100 точек

        for (int i = 0; i < pointCount; i++) {
            double x = generateXForFunctionType(type, i, pointCount);
            double y = calculateYForExpression(expression, x);
            points.add(new Point(functionId, x, y));
        }

        return points;
    }

    private double generateXForFunctionType(String type, int index, int total) {
        switch (type.toLowerCase()) {
            case "синусоида":
                return index * (2 * Math.PI / total); // 0 до 2π
            case "экспонента":
                return index * 0.1; // 0 до 10 с шагом 0.1
            case "кубическая":
                return (index - total/2.0) * 0.2; // симметрично вокруг 0
            default:
                return random.nextDouble() * 20 - 10; // от -10 до 10
        }
    }

    private double calculateYForExpression(String expression, double x) {
        if (expression.contains("x^2")) {
            return x * x;
        } else if (expression.contains("sin")) {
            return Math.sin(x);
        } else if (expression.contains("exp")) {
            return Math.exp(x);
        } else if (expression.contains("x^3")) {
            return x * x * x - x;
        } else if (expression.contains("2*x")) {
            return 2 * x + 1;
        } else {
            return x; // линейная по умолчанию
        }
    }

    private void generateAndTestPoints(Integer functionId, double startX, double endX,
                                       double step, String description) throws SQLException {
        List<Point> points = new ArrayList<>();

        for (double x = startX; x <= endX; x += step) {
            double y = Math.sin(x) * 10; // Пример функции
            points.add(new Point(functionId, x, y));
        }

        pointRepository.insertBatch(points);

        // Проверяем поиск в поддиапазоне
        double midStart = startX + (endX - startX) * 0.25;
        double midEnd = startX + (endX - startX) * 0.75;

        List<Point> inRange = pointRepository.findInXRange(functionId, midStart, midEnd);
        int expectedCount = (int) Math.round((midEnd - midStart) / step) + 1;

        System.out.println(description + ": сгенерировано " + points.size() +
                " точек, в поддиапазоне найдено " + inRange.size() +
                " (ожидалось ~" + expectedCount + ")");

        assertFalse(inRange.isEmpty());
    }

    private void testRange(Integer functionId, double minX, double maxX, int expectedCount)
            throws SQLException {
        List<Point> inRange = pointRepository.findInXRange(functionId, minX, maxX);
        assertEquals(expectedCount, inRange.size(),
                "Ожидалось " + expectedCount + " точек в диапазоне [" + minX + ", " + maxX + "]");

        // Проверяем, что все X в диапазоне
        for (Point p : inRange) {
            assertTrue(p.getXValue() >= minX && p.getXValue() <= maxX,
                    "X=" + p.getXValue() + " вне диапазона [" + minX + ", " + maxX + "]");
        }
    }

    @Test
    @Order(9)
    @DisplayName("Стресс-тест: много точек")
    void testStressManyPoints() throws SQLException {
        Integer stressFuncId = functionRepository.insert(
                new Function("StressTest", "stress", testUserId)
        );

        int largeCount = 10000;
        List<Point> largeBatch = new ArrayList<>();

        System.out.println("Начало генерации " + largeCount + " точек...");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < largeCount; i++) {
            double x = random.nextDouble() * 1000;
            double y = Math.sqrt(x);
            largeBatch.add(new Point(stressFuncId, x, y));

            // Прогресс каждые 1000 точек
            if (i % 1000 == 0 && i > 0) {
                System.out.println("Сгенерировано " + i + " точек...");
            }
        }

        long generateTime = System.currentTimeMillis() - startTime;
        System.out.println("Генерация " + largeCount + " точек: " + generateTime + "ms");

        // Пакетная вставка
        startTime = System.currentTimeMillis();
        pointRepository.insertBatch(largeBatch);
        long insertTime = System.currentTimeMillis() - startTime;

        System.out.println("Вставка " + largeCount + " точек: " + insertTime + "ms");
        System.out.println("Средняя скорость: " + (largeCount * 1000.0 / insertTime) + " точек/сек");

        // Проверяем, что все точки в базе
        List<Point> retrieved = pointRepository.findByFunctionId(stressFuncId);
        assertTrue(retrieved.size() >= largeCount);
    }
}