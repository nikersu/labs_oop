package repository;

import models.User;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Тесты репозитория пользователей с генерацией данных")
public class UserRepositoryTest {
    private static UserRepository repository;
    private static Integer testUserId;

    @BeforeAll
    static void setUp() {
        repository = new UserRepository();
    }

    @Test
    @Order(1)
    @DisplayName("Генерация и добавление разнообразных пользователей")
    void testInsertDiverseUsers() throws SQLException {
        // Генерация разнообразных данных пользователей
        String[] usernames = {
                "alice_smith",
                "bob_jones_89",
                "charlie.brown",
                "diana-prince",
                "evan_tech2024"
        };

        String[] passwordHashes = {
                "$2a$10$N9qo8uLOickgx2ZMRZoMye7G7o6B7R6ZQJ5YcJm6V5Jt9VJm6V5Jt9", // bcrypt hash 1
                "$2a$12$L9qo8uLOickgx2ZMRZoMye3G7o6B7R6ZQJ5YcJm6V5Jt9VJm6V5Jt9", // bcrypt hash 2
                "sha256_akjf83h7f8h38fh83hf", // sha256 hash
                "md5_93jd93jd93jd93jd",      // md5 hash
                "pbkdf2_483hf483hf483hf"     // pbkdf2 hash
        };

        // Добавляем нескольких пользователей
        for (int i = 0; i < usernames.length; i++) {
            User user = new User(
                    usernames[i],
                    passwordHashes[i]
            );

            Integer id = repository.insert(user);
            assertNotNull(id, "Пользователь должен быть добавлен с ID");
            assertTrue(id > 0, "ID должен быть положительным");

            if (i == 0) {
                testUserId = id; // Сохраняем для других тестов
            }

            System.out.println("Добавлен пользователь: " + usernames[i] + " с ID: " + id +
                    ", хэш: " + passwordHashes[i].substring(0, 20) + "...");
        }
    }

    @Test
    @Order(2)
    @DisplayName("Поиск всех пользователей")
    void testFindAll() throws SQLException {
        List<User> users = repository.findAll();

        assertNotNull(users, "Список пользователей не должен быть null");
        assertFalse(users.isEmpty(), "Должен быть хотя бы один пользователь");
        assertTrue(users.size() >= 5, "Должно быть минимум 5 пользователей (мы добавили 5)");

        System.out.println("Найдено пользователей: " + users.size());
        users.forEach(u -> System.out.println("  - ID: " + u.getId() + ", username: " + u.getUsername()));
    }

    @Test
    @Order(3)
    @DisplayName("Поиск пользователя по ID")
    void testFindById() throws SQLException {
        assertNotNull(testUserId, "Должен быть сохранен testUserId");

        User user = repository.findById(testUserId);

        assertNotNull(user, "Пользователь должен быть найден");
        assertEquals(testUserId, user.getId(), "ID должны совпадать");
        assertEquals("alice_smith", user.getUsername(), "Имя пользователя должно совпадать");
        assertTrue(user.getPasswordHash().startsWith("$2a$10$"), "Хэш пароля должен соответствовать");

        System.out.println("Найден пользователь по ID " + testUserId + ": " + user.getUsername());
    }

    @Test
    @Order(4)
    @DisplayName("Обновление пользователя")
    void testUpdate() throws SQLException {
        User user = repository.findById(testUserId);
        assertNotNull(user, "Пользователь должен существовать перед обновлением");

        String originalUsername = user.getUsername();
        String newUsername = "alice_smith_updated";
        String newPasswordHash = "$2a$12$updatedhashupdatedhashupdated";

        user.setUsername(newUsername);
        user.setPasswordHash(newPasswordHash);

        boolean success = repository.update(user);
        assertTrue(success, "Обновление должно быть успешным");

        // Проверяем, что обновилось
        User updated = repository.findById(testUserId);
        assertNotNull(updated);
        assertEquals(newUsername, updated.getUsername());
        assertEquals(newPasswordHash, updated.getPasswordHash());

        System.out.println("Пользователь обновлен: " + originalUsername + " → " + newUsername);
    }

    @Test
    @Order(5)
    @DisplayName("Удаление пользователя")
    void testDelete() throws SQLException {
        // Создаем временного пользователя для удаления
        User tempUser = new User(
                "temp_user_for_deletion",
                "$2a$10$tempHashForDeletionTest"
        );

        Integer tempId = repository.insert(tempUser);
        assertNotNull(tempId);

        // Удаляем
        boolean deleted = repository.delete(tempId);
        assertTrue(deleted, "Удаление должно быть успешным");

        // Проверяем, что удален
        User found = repository.findById(tempId);
        assertNull(found, "Пользователь должен быть удален из БД");

        System.out.println("Пользователь с ID " + tempId + " успешно удален");
    }

    @Test
    @Order(6)
    @DisplayName("Поиск несуществующего пользователя")
    void testFindNonExistentUser() throws SQLException {
        User user = repository.findById(999999);
        assertNull(user, "Несуществующий пользователь должен возвращать null");

        System.out.println("Поиск несуществующего пользователя возвращает null (корректно)");
    }

    @Test
    @Order(7)
    @DisplayName("Обновление несуществующего пользователя")
    void testUpdateNonExistentUser() throws SQLException {
        User nonExistent = new User("non_existent", "hash");
        nonExistent.setId(999999);

        boolean success = repository.update(nonExistent);
        assertFalse(success, "Обновление несуществующего пользователя должно возвращать false");

        System.out.println("Обновление несуществующего пользователя возвращает false (корректно)");
    }
    @Test
    @Order(8)
    @DisplayName("Генерация пользователей с крайними значениями")
    void testInsertEdgeCases() throws SQLException {
        // Тест с разными edge case данными

        // 1. Очень короткое имя
        User shortUser = new User("a", "$2a$10$short");
        Integer shortId = repository.insert(shortUser);
        assertNotNull(shortId);

        // 2. Имя с максимальной длиной (предполагаем 50 chars)
        String longUsername = "u".repeat(50);
        User longUser = new User(longUsername, "$2a$10$long");
        Integer longId = repository.insert(longUser);
        assertNotNull(longId);

        // 3. Имя со спецсимволами
        User specialUser = new User("user-name.with_dots", "$2a$10$special");
        Integer specialId = repository.insert(specialUser);
        assertNotNull(specialId);

        // 4. Длинный хэш пароля
        String longHash = "$2a$10$" + "x".repeat(200);
        User longHashUser = new User("long_hash_user", longHash);
        Integer longHashId = repository.insert(longHashUser);
        assertNotNull(longHashId);

        System.out.println("Добавлены пользователи с крайними значениями:");
        System.out.println("  - Короткое имя: ID " + shortId);
        System.out.println("  - Длинное имя (50 chars): ID " + longId);
        System.out.println("  - Спецсимволы в имени: ID " + specialId);
        System.out.println("  - Длинный хэш: ID " + longHashId);
    }
}