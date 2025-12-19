package controllers;

import dto.UserDto;
import entities.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.SearchService;
import services.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final SearchService searchService;

    public UserController(UserService userService, SearchService searchService) {
        this.userService = userService;
        this.searchService = searchService;
    }

    // ========== CREATE OPERATIONS ==========
    // POST /api/users - Создать нового пользователя (любой пользователь)
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        log.info("POST /api/users - Creating new user with username='{}', role='{}'", 
                userDto.getUsername(), userDto.getRole());
        try {
            UserEntity user = userService.createUser(
                    userDto.getUsername(),
                    userDto.getPasswordHash(),
                    userDto.getRole()
            );
            log.info("User created successfully with id={}, username='{}'", user.getId(), user.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(user));
        } catch (Exception e) {
            log.error("Error creating user with username='{}': {}", userDto.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    // ========== READ OPERATIONS ==========
    // GET /api/users - Получить список всех пользователей (ADMIN)
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("GET /api/users - Retrieving all users");
        // TODO: Проверка доступа - только ADMIN
        try {
            List<UserDto> users = userService.getAllUsers().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            log.info("Retrieved {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error retrieving all users: {}", e.getMessage(), e);
            throw e;
        }
    }

    // GET /api/users/{id} - Получить пользователя по ID (ADMIN или пользователь)
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{} - Retrieving user by id", id);
        // TODO: Проверка доступа - ADMIN или сам пользователь
        return userService.getUserById(id)
                .map(user -> {
                    log.info("User found: id={}, username='{}'", user.getId(), user.getUsername());
                    return ResponseEntity.ok(toDto(user));
                })
                .orElseGet(() -> {
                    log.warn("User not found with id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // GET /api/users/sorted - Получить всех пользователей, отсортированных по 'username' (ADMIN)
    @GetMapping("/sorted")
    public ResponseEntity<List<UserDto>> getSortedUsers() {
        log.info("GET /api/users/sorted - Retrieving all users sorted by username");
        // TODO: Проверка доступа - только ADMIN
        try {
            List<UserDto> users = userService.getAllUsersSorted().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            log.info("Retrieved {} users sorted by username", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error retrieving sorted users: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ========== UPDATE OPERATIONS ==========
    // PUT /api/users/{id} - Обновить 'username', 'passwordHash', 'role' (ADMIN или сам пользователь)
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @RequestBody UserDto userDto) {
        log.info("PUT /api/users/{} - Updating user with username='{}', role='{}'", 
                id, userDto.getUsername(), userDto.getRole());
        // TODO: Проверка доступа - ADMIN или сам пользователь
        try {
            UserEntity updated = userService.updateUser(
                    id,
                    userDto.getUsername(),
                    userDto.getPasswordHash(),
                    userDto.getRole()
            );
            log.info("User updated successfully: id={}, username='{}'", updated.getId(), updated.getUsername());
            return ResponseEntity.ok(toDto(updated));
        } catch (RuntimeException e) {
            log.error("Error updating user with id={}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ========== DELETE OPERATIONS ==========
    // DELETE /api/users/{id} - Удалить пользователя (ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{} - Deleting user", id);
        // TODO: Проверка доступа - только ADMIN
        try {
            userService.deleteUser(id);
            log.info("User deleted successfully: id={}", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting user with id={}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private UserDto toDto(UserEntity user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getRole()
        );
    }
}

