package repository;

import DTO.Function;
import JDBC.repository.FunctionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FunctionRepositoryTest {
    private FunctionRepository functionRepository;
    private Integer functionId1;
    private Integer functionId2;

        @BeforeEach
    void setUp() {
        functionRepository = new FunctionRepository();
        Integer userId1 = 1;
        // Очищаем таблицу перед тестами
        clearFunctionsTable();

        // Добавляем тестовые функции
        functionId1 = functionRepository.insert(new Function("sin(x)", "Math.sin(x)", userId1));
        functionId2 = functionRepository.insert(new Function("cos(x)", "Math.cos(x)", userId1));
    }

    @AfterEach
    void tearDown() {
        // Очищаем таблицу после тестов
        clearFunctionsTable();
    }

    @Test
    void testInsert() {
        Integer userId1 = 1;
        Function newFunction = new Function("tan(x)", "Math.tan(x)", userId1);
        Integer newId = functionRepository.insert(newFunction);
        assertNotNull(newId);
        assertTrue(newId > 0);

        Function foundFunction = functionRepository.findById(newId);
        assertNotNull(foundFunction);
        assertEquals("tan(x)", foundFunction.getName());
        assertEquals("Math.tan(x)", foundFunction.getExpression());
        assertEquals(userId1, foundFunction.getUserId());
    }

    @Test
    void testFindById() {
        Integer userId1 = 1;
        Function function = functionRepository.findById(functionId1);
        assertNotNull(function);
        assertEquals(functionId1, function.getId());
        assertEquals("sin(x)", function.getName());
        assertEquals("Math.sin(x)", function.getExpression());
        assertEquals(userId1, function.getUserId());
    }

    @Test
    void testFindByIdNonExistent() {
        Function function = functionRepository.findById(99999);
        assertNull(function);
    }

    @Test
    void testFindAll() {
        List<Function> functions = functionRepository.findAll();
        assertEquals(3, functions.size());

        // Проверяем, что все добавленные функции присутствуют
        boolean foundSin = false, foundCos = false, foundX2 = false;
        for (Function f : functions) {
            if ("sin(x)".equals(f.getName())) foundSin = true;
            if ("cos(x)".equals(f.getName())) foundCos = true;
            if ("x^2".equals(f.getName())) foundX2 = true;
        }

        assertTrue(foundSin);
        assertTrue(foundCos);
        assertTrue(foundX2);
    }

    @Test
    void testFindByUserId() {
        Integer userId1 = 1;
        Integer userId2 = 2;
        List<Function> user1Functions = functionRepository.findByUserId(userId1);
        assertEquals(2, user1Functions.size());

        List<Function> user2Functions = functionRepository.findByUserId(userId2);
        assertEquals(1, user2Functions.size());

        List<Function> nonExistentUserFunctions = functionRepository.findByUserId(999);
        assertNotNull(nonExistentUserFunctions);
        assertTrue(nonExistentUserFunctions.isEmpty());
    }

    @Test
    void testUpdate() {
        Integer userId2 = 2;
        Function functionToUpdate = functionRepository.findById(functionId1);
        assertNotNull(functionToUpdate);

        functionToUpdate.setName("updated_sin(x)");
        functionToUpdate.setExpression("Math.sin(x) * 2");
        functionToUpdate.setUserId(userId2);

        boolean updateResult = functionRepository.update(functionToUpdate);
        assertTrue(updateResult);

        Function updatedFunction = functionRepository.findById(functionId1);
        assertNotNull(updatedFunction);
        assertEquals("updated_sin(x)", updatedFunction.getName());
        assertEquals("Math.sin(x) * 2", updatedFunction.getExpression());
        assertEquals(userId2, updatedFunction.getUserId());
    }

    @Test
    void testUpdateNonExistent() {
        Function nonExistentFunction = new Function("nonexistent", "none", 999);
        nonExistentFunction.setId(99999);

        boolean updateResult = functionRepository.update(nonExistentFunction);
        assertFalse(updateResult);
    }

    @Test
    void testDelete() {
        boolean deleteResult = functionRepository.delete(functionId2);
        assertTrue(deleteResult);

        Function deletedFunction = functionRepository.findById(functionId2);
        assertNull(deletedFunction);

        List<Function> functions = functionRepository.findAll();
        assertEquals(2, functions.size());
    }

    @Test
    void testDeleteNonExistent() {
        boolean deleteResult = functionRepository.delete(99999);
        assertFalse(deleteResult);
    }

    @Test
    void testFindAllSortedByName() {
        Integer userId1 = 1;
        clearFunctionsTable();

        // Добавляем функции в разном порядке
        functionRepository.insert(new Function("cosine", "Math.cos(x)", userId1));
        functionRepository.insert(new Function("alpha", "x + 1", userId1));
        functionRepository.insert(new Function("beta", "x - 1", userId1));

        List<Function> sortedFunctions = functionRepository.findAllSortedByName();

        assertEquals(3, sortedFunctions.size());
        assertEquals("alpha", sortedFunctions.get(0).getName());
        assertEquals("beta", sortedFunctions.get(1).getName());
        assertEquals("cosine", sortedFunctions.get(2).getName());
    }

    @Test
    void testFindAllSortedByNameEmpty() {
        clearFunctionsTable();

        List<Function> sortedFunctions = functionRepository.findAllSortedByName();
        assertNotNull(sortedFunctions);
        assertTrue(sortedFunctions.isEmpty());
    }

    @Test
    void testFullCrudCycle() {
        Integer userId1 = 1;
        Integer userId2 = 2;
        // Create
        Function newFunction = new Function("test_func", "x + y", userId1);
        Integer newId = functionRepository.insert(newFunction);
        assertNotNull(newId);

        // Read
        Function createdFunction = functionRepository.findById(newId);
        assertNotNull(createdFunction);
        assertEquals("test_func", createdFunction.getName());
        assertEquals("x + y", createdFunction.getExpression());
        assertEquals(userId1, createdFunction.getUserId());

        // Update
        createdFunction.setName("updated_func");
        createdFunction.setExpression("x * y");
        createdFunction.setUserId(userId2);
        assertTrue(functionRepository.update(createdFunction));

        Function updatedFunction = functionRepository.findById(newId);
        assertEquals("updated_func", updatedFunction.getName());
        assertEquals("x * y", updatedFunction.getExpression());
        assertEquals(userId2, updatedFunction.getUserId());

        // Delete
        assertTrue(functionRepository.delete(newId));
        assertNull(functionRepository.findById(newId));
    }

    @Test
    void testFindAllAfterMultipleOperations() {
        Integer userId1 = 1;
        // Начальное количество
        List<Function> initialFunctions = functionRepository.findAll();
        int initialCount = initialFunctions.size();

        // Добавляем новую функцию
        functionRepository.insert(new Function("new_func", "x^3", userId1));
        List<Function> afterAdd = functionRepository.findAll();
        assertEquals(initialCount + 1, afterAdd.size());

        // Удаляем функцию
        functionRepository.delete(functionId1);
        List<Function> afterDelete = functionRepository.findAll();
        assertEquals(initialCount, afterDelete.size());
    }

    // Вспомогательный метод для очистки таблицы
    private void clearFunctionsTable() {
        List<Function> functions = functionRepository.findAll();
        for (Function function : functions) {
            if (function.getId() != null) {
                functionRepository.delete(function.getId());
            }
        }
    }
}