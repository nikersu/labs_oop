package repository;

import models.Function;
import database.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FunctionRepository {
    private static final Logger logger = LoggerFactory.getLogger(FunctionRepository.class);

    // SQL запросы
    private static final String INSERT_SQL =
            "INSERT INTO functions (name, expression, user_id) VALUES (?, ?, ?)";
    private static final String SELECT_ALL_SQL =
            "SELECT id, name, expression, user_id FROM functions";
    private static final String SELECT_BY_ID_SQL =
            "SELECT id, name, expression, user_id FROM functions WHERE id = ?";
    private static final String UPDATE_SQL =
            "UPDATE functions SET name = ?, expression = ?, user_id = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM functions WHERE id = ?";

    // добавление функции
    public Integer insert(Function function) throws SQLException {
        logger.info("Operation start: adding a function {}", function.getName());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, function.getName());
            stmt.setString(2, function.getExpression());
            stmt.setInt(3, function.getUserId());

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    logger.info("Operation completed: function added with ID {}", id);
                    return id;
                } else {
                    logger.error("Critical error: failed to get function ID");
                    throw new SQLException("Couldn't get the function ID");
                }
            }
        }
    }

    // получение всех функций
    public List<Function> findAll() throws SQLException {
        logger.info("Operation start: getting all the functions");

        List<Function> functions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            while (rs.next()) {
                functions.add(mapResultSetToFunction(rs));
            }
        }
        logger.info("Operation completed: {} functions found", functions.size());
        return functions;
    }

    // поиск функции по ID
    public Function findById(Integer id) throws SQLException {
        logger.info("Operation start: function search by ID {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("Operation completed: function found");
                    return mapResultSetToFunction(rs);
                }
            }
        }
        logger.info("Operation completed: function with ID {} not found", id);
        return null;
    }

    // обновление имени и выражения функции
    public boolean update(Function function) throws SQLException {
        logger.info("Operation start: updating the function with ID {}", function.getId());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, function.getName());
            stmt.setString(2, function.getExpression());
            stmt.setInt(3, function.getUserId());
            stmt.setInt(4, function.getId());

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            logger.info("Operation completed: the {} function has been updated", success ? "" : "не ");
            return success;
        }
    }

    // удаление функции
    public boolean delete(Integer id) throws SQLException {
        logger.info("Start of the operation: deleting the function from the ID {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            logger.info("Операция завершена: функция {}удалена", success ? "" : "не ");
            return success;
        }
    }

    private Function mapResultSetToFunction(ResultSet rs) throws SQLException {
        Function function = new Function();
        function.setId(rs.getInt("id"));
        function.setName(rs.getString("name"));
        function.setExpression(rs.getString("expression"));
        function.setUserId(rs.getInt("user_id"));
        return function;
    }
}