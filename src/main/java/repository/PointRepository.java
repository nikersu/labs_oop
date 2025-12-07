package repository;

import models.Point;
import database.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PointRepository {
    private static final Logger logger = LoggerFactory.getLogger(PointRepository.class);

    // SQL запросы
    private static final String INSERT_SQL =
            "INSERT INTO points (function_id, x_value, y_value) VALUES (?, ?, ?)";
    private static final String SELECT_BY_FUNCTION_SQL =
            "SELECT function_id, x_value, y_value FROM points WHERE function_id = ?";
    private static final String SELECT_IN_X_RANGE_SQL =
            "SELECT function_id, x_value, y_value FROM points WHERE function_id = ? AND x_value BETWEEN ? AND ?";
    private static final String SELECT_BY_FUNCTION_AND_X_SQL =
            "SELECT function_id, x_value, y_value FROM points WHERE function_id = ? AND x_value = ?";
    private static final String UPDATE_SQL =
            "UPDATE points SET y_value = ? WHERE function_id = ? AND x_value = ?";
    private static final String DELETE_BY_FUNCTION_SQL =
            "DELETE FROM points WHERE function_id = ?";
    private static final String DELETE_SPECIFIC_SQL =
            "DELETE FROM points WHERE function_id = ? AND x_value = ?";

    // 1. Добавление точки
    public void insert(Point point) throws SQLException {
        logger.info("Начало операции: добавление точки для функции ID {}", point.getFunctionId());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            stmt.setInt(1, point.getFunctionId());
            stmt.setDouble(2, point.getXValue());
            stmt.setDouble(3, point.getYValue());

            stmt.executeUpdate();
            logger.info("Операция завершена: точка добавлена");
        }
    }

    // 2. Добавление нескольких точек за раз
    public void insertBatch(List<Point> points) throws SQLException {
        logger.info("Начало операции: добавление {} точек", points.size());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            for (Point point : points) {
                stmt.setInt(1, point.getFunctionId());
                stmt.setDouble(2, point.getXValue());
                stmt.setDouble(3, point.getYValue());
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            logger.info("Операция завершена: добавлено {} точек", results.length);
        }
    }

    // 3. Получение всех точек функции
    public List<Point> findByFunctionId(Integer functionId) throws SQLException {
        logger.info("Начало операции: получение точек для функции ID {}", functionId);

        List<Point> points = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_FUNCTION_SQL)) {

            stmt.setInt(1, functionId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    points.add(mapResultSetToPoint(rs));
                }
            }
        }

        logger.info("Операция завершена: найдено {} точек для функции {}", points.size(), functionId);
        return points;
    }

    // 4. Поиск точек в диапазоне X
    public List<Point> findInXRange(Integer functionId, Double minX, Double maxX) throws SQLException {
        logger.info("Начало операции: поиск точек функции {} в диапазоне X [{}, {}]", functionId, minX, maxX);

        List<Point> points = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_IN_X_RANGE_SQL)) {

            stmt.setInt(1, functionId);
            stmt.setDouble(2, minX);
            stmt.setDouble(3, maxX);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    points.add(mapResultSetToPoint(rs));
                }
            }
        }

        logger.info("Операция завершена: найдено {} точек в диапазоне", points.size());
        return points;
    }

    // 5. Получение конкретной точки
    public Point findByFunctionIdAndX(Integer functionId, Double xValue) throws SQLException {
        logger.info("Начало операции: поиск точки функции {} с X={}", functionId, xValue);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_FUNCTION_AND_X_SQL)) {

            stmt.setInt(1, functionId);
            stmt.setDouble(2, xValue);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("Операция завершена: точка найдена");
                    return mapResultSetToPoint(rs);
                }
            }
        }

        logger.info("Операция завершена: точка не найдена");
        return null;
    }

    // 6. Обновление Y значения точки
    public boolean update(Point point) throws SQLException {
        logger.info("Начало операции: обновление точки функции {} с X={}", point.getFunctionId(), point.getXValue());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setDouble(1, point.getYValue());
            stmt.setInt(2, point.getFunctionId());
            stmt.setDouble(3, point.getXValue());

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            logger.info("Операция завершена: точка {}обновлена", success ? "" : "не ");
            return success;
        }
    }

    // 7. Удаление всех точек функции
    public boolean deleteByFunctionId(Integer functionId) throws SQLException {
        logger.info("Начало операции: удаление всех точек функции ID {}", functionId);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_FUNCTION_SQL)) {

            stmt.setInt(1, functionId);
            int affectedRows = stmt.executeUpdate();

            logger.info("Операция завершена: удалено {} точек", affectedRows);
            return affectedRows > 0;
        }
    }

    // 8. Удаление конкретной точки
    public boolean delete(Integer functionId, Double xValue) throws SQLException {
        logger.info("Начало операции: удаление точки функции {} с X={}", functionId, xValue);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SPECIFIC_SQL)) {

            stmt.setInt(1, functionId);
            stmt.setDouble(2, xValue);

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            logger.info("Операция завершена: точка {}удалена", success ? "" : "не ");
            return success;
        }
    }

    // 9. Генерация точек для функции
    public void generatePointsForFunction(Integer functionId, String expression,
                                          Double startX, Double endX, Double step) throws SQLException {
        logger.info("Начало операции: генерация точек для функции {} в диапазоне X [{}, {}] с шагом {}",
                functionId, startX, endX, step);

        List<Point> points = new ArrayList<>();

        for (double x = startX; x <= endX; x += step) {
            double y = evaluateExpression(expression, x);
            points.add(new Point(functionId, x, y));
        }

        insertBatch(points);
        logger.info("Операция завершена: сгенерировано {} точек", points.size());
    }

    // Заглушка для вычисления выражения
    private double evaluateExpression(String expression, double x) {
        // Простейшая реализация для демо
        if (expression.contains("x^2")) {
            return x * x;
        } else if (expression.contains("sin")) {
            return Math.sin(x);
        } else {
            return x;
        }
    }

    // Вспомогательный метод для маппинга
    private Point mapResultSetToPoint(ResultSet rs) throws SQLException {
        Point point = new Point();
        point.setFunctionId(rs.getInt("function_id"));
        point.setXValue(rs.getDouble("x_value"));
        point.setYValue(rs.getDouble("y_value"));
        return point;
    }
}