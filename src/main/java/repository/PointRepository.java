


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
    private static final String SELECT_BY_FUNCTION_AND_X_SQL =
            "SELECT function_id, x_value, y_value FROM points WHERE function_id = ? AND x_value = ?";
    private static final String UPDATE_SQL =
            "UPDATE points SET y_value = ? WHERE function_id = ? AND x_value = ?";
    private static final String DELETE_SPECIFIC_SQL =
            "DELETE FROM points WHERE function_id = ? AND x_value = ?";

    // добавление точки
    public void insert(Point point) throws SQLException {
        logger.info("Starting the operation: adding a point for the ID function {}", point.getFunctionId());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            stmt.setInt(1, point.getFunctionId());
            stmt.setDouble(2, point.getXValue());
            stmt.setDouble(3, point.getYValue());
            stmt.executeUpdate();
            logger.info("The operation is completed: the point has been added");
        }
    }

    // получение всех точек функции
    public List<Point> findByFunctionId(Integer functionId) throws SQLException {
        logger.info("Start of operation: obtaining points for the ID function {}", functionId);

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
        logger.info("The operation is complete: {} points have been found for the function {}", points.size(), functionId);
        return points;
    }

    // получение конкретной точки
    public Point findByFunctionIdAndX(Integer functionId, Double xValue) throws SQLException {
        logger.info("Starting the operation: finding the point of the function {} with X={}", functionId, xValue);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_FUNCTION_AND_X_SQL)) {
            stmt.setInt(1, functionId);
            stmt.setDouble(2, xValue);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("Operation completed: the point has been found");
                    return mapResultSetToPoint(rs);
                }
            }
        }
        logger.info("Operation completed: point not found");
        return null;
    }

    // обновление Y значения точки
    public boolean update(Point point) throws SQLException {
        logger.info("Starting the operation: updating the function point {} with X={}", point.getFunctionId(), point.getXValue());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setDouble(1, point.getYValue());
            stmt.setInt(2, point.getFunctionId());
            stmt.setDouble(3, point.getXValue());
            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            logger.info("Operation completed: point {}updated", success ? "" : "not ");
            return success;
        }
    }

    // удаление конкретной точки
    public boolean delete(Integer functionId, Double xValue) throws SQLException {
        logger.info("Start of operation: deleting the point of function {} with X={}", functionId, xValue);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SPECIFIC_SQL)) {

            stmt.setInt(1, functionId);
            stmt.setDouble(2, xValue);

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            logger.info("Operation completed: point {} has been deleted", success ? "" : "not ");
            return success;
        }
    }

    private Point mapResultSetToPoint(ResultSet rs) throws SQLException {
        Point point = new Point();
        point.setFunctionId(rs.getInt("function_id"));
        point.setXValue(rs.getDouble("x_value"));
        point.setYValue(rs.getDouble("y_value"));
        return point;
    }
}