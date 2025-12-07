package repository;

import models.Function;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FunctionRepositoryTest {
    private static FunctionRepository repository;
    private static Integer testFunctionId;

    @BeforeAll
    static void setUp() {
        repository = new FunctionRepository();
    }

    @Test
    @Order(1)
    @DisplayName("Генерация и добавление разнообразных функций")
    void testInsertDiverseFunctions() throws SQLException {
        // Генерация разнообразных данных
        String[] functionNames = {
                "Квадратичная функция",
                "Линейная зависимость",
                "Синусоидальное колебание",
                "Экспоненциальный рост"
        };

        String[] expressions = {
                "x^2 + 2x + 1",
                "3.5*x - 2.1",
                "sin(x) * amplitude",
                "e^(k*x)"
        };

        // Добавляем несколько функций
        for (int i = 0; i < functionNames.length; i++) {
            Function function = new Function(
                    functionNames[i],
                    expressions[i],
                    1 // test user_id
            );

            Integer id = repository.insert(function);
            assertNotNull(id, "Функция должна быть добавлена с ID");
            assertTrue(id > 0, "ID должен быть положительным");

            if (i == 0) {
                testFunctionId = id; // Сохраняем для других тестов
            }

            System.out.println("Добавлена функция: " + functionNames[i] + " с ID: " + id);
        }
    }

    @Test
    @Order(2)
    @DisplayName("Поиск всех функций")
    void testFindAll() throws SQLException {
        List<Function> functions = repository.findAll();

        assertNotNull(functions, "Список функций не должен быть null");
        assertFalse(functions.isEmpty(), "Должна быть хотя бы одна функция");
        assertTrue(functions.size() >= 4, "Должно быть минимум 4 функции (мы добавили 4)");

        System.out.println("Найдено функций: " + functions.size());
        functions.forEach(f -> System.out.println("  - " + f.getName()));
    }

    @Test
    @Order(3)
    @DisplayName("Поиск функции по ID")
    void testFindById() throws SQLException {
        assertNotNull(testFunctionId, "Должен быть сохранен testFunctionId");

        Function function = repository.findById(testFunctionId);

        assertNotNull(function, "Функция должна быть найдена");
        assertEquals(testFunctionId, function.getId(), "ID должны совпадать");
        assertEquals("Квадратичная функция", function.getName(), "Имя должно совпадать");
        assertEquals("x^2 + 2x + 1", function.getExpression(), "Выражение должно совпадать");
        assertEquals(1, function.getUserId(), "User ID должен быть 1");

        System.out.println("Найдена функция по ID " + testFunctionId + ": " + function.getName());
    }

    @Test
    @Order(4)
    @DisplayName("Обновление функции")
    void testUpdate() throws SQLException {
        Function function = repository.findById(testFunctionId);
        assertNotNull(function, "Функция должна существовать перед обновлением");

        String originalName = function.getName();
        String newName = "Обновленная квадратичная функция";
        String newExpression = "2*x^2 + 3*x + 5";

        function.setName(newName);
        function.setExpression(newExpression);

        boolean success = repository.update(function);
        assertTrue(success, "Обновление должно быть успешным");

        // Проверяем, что обновилось
        Function updated = repository.findById(testFunctionId);
        assertNotNull(updated);
        assertEquals(newName, updated.getName());
        assertEquals(newExpression, updated.getExpression());

        System.out.println("Функция обновлена: " + originalName + " → " + newName);
    }

    @Test
    @Order(6)
    @DisplayName("Удаление функции")
    void testDelete() throws SQLException {
        // создаем временную функцию для удаления
        Function tempFunction = new Function(
                "Временная функция для удаления",
                "x",
                1
        );

        Integer tempId = repository.insert(tempFunction);
        assertNotNull(tempId);

        // удаляем
        boolean deleted = repository.delete(tempId);
        assertTrue(deleted, "Удаление должно быть успешным");

        // проверяем, что удалена
        Function found = repository.findById(tempId);
        assertNull(found, "Функция должна быть удалена из БД");

        System.out.println("Функция с ID " + tempId + " успешно удалена");
    }

    @Test
    @Order(7)
    @DisplayName("Поиск несуществующей функции")
    void testFindNonExistentFunction() throws SQLException {
        Function function = repository.findById(999999);
        assertNull(function, "Несуществующая функция должна возвращать null");

        System.out.println("Поиск несуществующей функции возвращает null (корректно)");
    }

    @Test
    @Order(8)
    @DisplayName("Обновление несуществующей функции")
    void testUpdateNonExistentFunction() throws SQLException {
        Function nonExistent = new Function("Несуществующая", "x", 1);
        nonExistent.setId(999999);

        boolean success = repository.update(nonExistent);
        assertFalse(success, "Обновление несуществующей функции должно возвращать false");

        System.out.println("Обновление несуществующей функции возвращает false (корректно)");
    }
}