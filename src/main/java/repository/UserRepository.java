package repository;

import models.User;
import database.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    // SQL запросы
    private static final String INSERT_SQL =
            "INSERT INTO users (username, password_hash) VALUES (?, ?)";
    private static final String SELECT_ALL_SQL =
            "SELECT id, username, password_hash FROM users";
    private static final String SELECT_BY_ID_SQL =
            "SELECT id, username, password_hash FROM users WHERE id = ?";
    private static final String UPDATE_SQL =
            "UPDATE users SET username = ?, password_hash = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM users WHERE id = ?";

    // добавление пользователя
    public Integer insert(User user) throws SQLException {
        logger.info("Operation start: adding a user {}", user.getUsername());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    logger.info("The operation is completed: the user has been added with the ID {}", id);
                    return id;
                } else {
                    logger.error("Critical error: failed to retrieve user ID");
                    throw new SQLException("Couldn't get user ID");
                }
            }
        }
    }

    // получение всех пользователей
    public List<User> findAll() throws SQLException {
        logger.info("Operation start: getting all users");

        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                users.add(user);
            }
        }
        logger.info("Operation completed: {} users found", users.size());
        return users;
    }

    // поиск пользователя по ID
    public User findById(Integer id) throws SQLException {
        logger.info("Operation start: user ID search {}", id);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("Operation completed: the user has been found");
                    return mapResultSetToUser(rs);
                }
            }
        }
        logger.info("The operation was completed: the user with ID {} was not found", id);
        return null;
    }

    // обновление пользователя
    public boolean update(User user) throws SQLException {
        logger.info("Start of the operation: updating the user with the ID {}", user.getId());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setInt(3, user.getId());

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            logger.info("Operation completed: user {}updated", success ? "" : "not ");
            return success;
        }
    }

    // 5. Удаление пользователя
    public boolean delete(Integer id) throws SQLException {
        logger.info("Start of the operation: deleting a user with an ID {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;
            logger.info("Operation completed: user {}deleted", success ? "" : "не ");
            return success;
        }
    }

    // метод для маппинга
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        return user;
    }
}