package controllers;

import dto.FunctionDto;
import entities.FunctionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.FunctionService;
import services.SearchService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/functions")
public class FunctionController {

    private static final Logger log = LoggerFactory.getLogger(FunctionController.class);

    private final FunctionService functionService;
    private final SearchService searchService;

    public FunctionController(FunctionService functionService, SearchService searchService) {
        this.functionService = functionService;
        this.searchService = searchService;
    }

    // ========== CREATE OPERATIONS ==========
    // POST /api/functions - Создать новую функцию (привязана к 'userId') (Любой авторизованный, userId = текущий пользователь)
    @PostMapping
    public ResponseEntity<FunctionDto> createFunction(@RequestBody FunctionDto functionDto) {
        log.info("POST /api/functions - Creating new function with name='{}', expression='{}', userId={}", 
                functionDto.getName(), functionDto.getExpression(), functionDto.getUserId());
        // TODO: Проверка доступа - авторизованный пользователь, userId = текущий пользователь
        try {
            FunctionEntity function = functionService.createFunction(
                    functionDto.getName(),
                    functionDto.getExpression(),
                    functionDto.getUserId()
            );
            log.info("Function created successfully: id={}, name='{}', userId={}", 
                    function.getId(), function.getName(), function.getUser().getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(function));
        } catch (Exception e) {
            log.error("Error creating function with name='{}', userId={}: {}", 
                    functionDto.getName(), functionDto.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    // ========== READ OPERATIONS ==========
    // GET /api/functions - Получить все функции (Только ADMIN)
    @GetMapping
    public ResponseEntity<List<FunctionDto>> getAllFunctions() {
        log.info("GET /api/functions - Retrieving all functions");
        // TODO: Проверка доступа - только ADMIN
        try {
            List<FunctionDto> functions = functionService.getAllFunctions().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            log.info("Retrieved {} functions", functions.size());
            return ResponseEntity.ok(functions);
        } catch (Exception e) {
            log.error("Error retrieving all functions: {}", e.getMessage(), e);
            throw e;
        }
    }

    // GET /api/functions/{id} - Получить функцию по ID (Владелец функции или ADMIN)
    @GetMapping("/{id}")
    public ResponseEntity<FunctionDto> getFunctionById(@PathVariable Long id) {
        log.info("GET /api/functions/{} - Retrieving function by id", id);
        // TODO: Проверка доступа - владелец функции или ADMIN
        return searchService.findFunctionById(id)
                .map(function -> {
                    log.info("Function found: id={}, name='{}', userId={}", 
                            function.getId(), function.getName(), function.getUser().getId());
                    return ResponseEntity.ok(toDto(function));
                })
                .orElseGet(() -> {
                    log.warn("Function not found with id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // GET /api/functions/user/{userId} - Получить все функции пользователя (ADMIN или пользователь, userId = текущий)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FunctionDto>> getFunctionsByUserId(@PathVariable Long userId) {
        log.info("GET /api/functions/user/{} - Retrieving functions by userId", userId);
        // TODO: Проверка доступа - ADMIN или пользователь (userId = текущий)
        try {
            List<FunctionDto> functions = functionService.getFunctionsByUserId(userId).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            log.info("Retrieved {} functions for userId={}", functions.size(), userId);
            return ResponseEntity.ok(functions);
        } catch (Exception e) {
            log.error("Error retrieving functions for userId={}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    // GET /api/functions/sorted - Получить все функции, отсортированные по 'name' (Только ADMIN)
    @GetMapping("/sorted")
    public ResponseEntity<List<FunctionDto>> getSortedFunctions() {
        log.info("GET /api/functions/sorted - Retrieving all functions sorted by name");
        // TODO: Проверка доступа - только ADMIN
        try {
            List<FunctionDto> functions = functionService.getAllFunctionsSorted().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            log.info("Retrieved {} functions sorted by name", functions.size());
            return ResponseEntity.ok(functions);
        } catch (Exception e) {
            log.error("Error retrieving sorted functions: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ========== UPDATE OPERATIONS ==========
    // PUT /api/functions/{id} - Обновить 'name', 'expression', 'userId' (Владелец функции или ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<FunctionDto> updateFunction(
            @PathVariable Long id,
            @RequestBody FunctionDto functionDto) {
        log.info("PUT /api/functions/{} - Updating function with name='{}', expression='{}', userId={}", 
                id, functionDto.getName(), functionDto.getExpression(), functionDto.getUserId());
        // TODO: Проверка доступа - владелец функции или ADMIN
        try {
            FunctionEntity updated = functionService.updateFunction(
                    id,
                    functionDto.getName(),
                    functionDto.getExpression(),
                    functionDto.getUserId()
            );
            log.info("Function updated successfully: id={}, name='{}'", updated.getId(), updated.getName());
            return ResponseEntity.ok(toDto(updated));
        } catch (RuntimeException e) {
            log.error("Error updating function with id={}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ========== DELETE OPERATIONS ==========
    // DELETE /api/functions/{id} - Удалить функцию (Владелец функции или ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Long id) {
        log.info("DELETE /api/functions/{} - Deleting function", id);
        // TODO: Проверка доступа - владелец функции или ADMIN
        try {
            functionService.deleteFunction(id);
            log.info("Function deleted successfully: id={}", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting function with id={}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private FunctionDto toDto(FunctionEntity function) {
        return new FunctionDto(
                function.getId(),
                function.getName(),
                function.getExpression(),
                function.getUser().getId()
        );
    }
}

