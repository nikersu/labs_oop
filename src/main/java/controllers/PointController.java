package controllers;

import dto.PointDto;
import entities.PointEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.PointService;
import services.SearchService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/points")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final PointService pointService;
    private final SearchService searchService;

    public PointController(PointService pointService, SearchService searchService) {
        this.pointService = pointService;
        this.searchService = searchService;
    }

    // ========== CREATE OPERATIONS ==========
    // POST /api/points - Добавить точку (Владелец функции или ADMIN)
    @PostMapping
    public ResponseEntity<PointDto> createPoint(@RequestBody PointDto pointDto) {
        log.info("POST /api/points - Creating new point with functionId={}, xValue={}, yValue={}", 
                pointDto.getFunctionId(), pointDto.getXValue(), pointDto.getYValue());
        // TODO: Проверка доступа - владелец функции или ADMIN
        try {
            PointEntity point = pointService.createPoint(
                    pointDto.getFunctionId(),
                    pointDto.getXValue(),
                    pointDto.getYValue()
            );
            log.info("Point created successfully: functionId={}, xValue={}, yValue={}", 
                    point.getFunction().getId(), point.getXValue(), point.getYValue());
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(point));
        } catch (Exception e) {
            log.error("Error creating point with functionId={}, xValue={}: {}", 
                    pointDto.getFunctionId(), pointDto.getXValue(), e.getMessage(), e);
            throw e;
        }
    }

    // ========== READ OPERATIONS ==========
    // GET /api/points/function/{functionId} - Получить все точки функции (Владелец функции или ADMIN)
    @GetMapping("/function/{functionId}")
    public ResponseEntity<List<PointDto>> getPointsByFunctionId(@PathVariable Long functionId) {
        log.info("GET /api/points/function/{} - Retrieving points by functionId", functionId);
        // TODO: Проверка доступа - владелец функции или ADMIN
        try {
            List<PointDto> points = pointService.getPointsByFunctionId(functionId).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            log.info("Retrieved {} points for functionId={}", points.size(), functionId);
            return ResponseEntity.ok(points);
        } catch (Exception e) {
            log.error("Error retrieving points for functionId={}: {}", functionId, e.getMessage(), e);
            throw e;
        }
    }

    // GET /api/points/function/{functionId}/x/{xValue} - Получить конкретную точку по X (Владелец функции или ADMIN)
    @GetMapping("/function/{functionId}/x/{xValue}")
    public ResponseEntity<PointDto> getPointByFunctionIdAndX(
            @PathVariable Long functionId,
            @PathVariable Double xValue) {
        log.info("GET /api/points/function/{}/x/{} - Retrieving point by functionId and xValue", 
                functionId, xValue);
        // TODO: Проверка доступа - владелец функции или ADMIN
        return searchService.findPoint(functionId, xValue)
                .map(point -> {
                    log.info("Point found: functionId={}, xValue={}, yValue={}", 
                            functionId, xValue, point.getYValue());
                    return ResponseEntity.ok(toDto(point));
                })
                .orElseGet(() -> {
                    log.warn("Point not found with functionId={}, xValue={}", functionId, xValue);
                    return ResponseEntity.notFound().build();
                });
    }

    // GET /api/points/sorted/x - Все точки, отсортированные по 'xValue' (Только ADMIN)
    @GetMapping("/sorted/x")
    public ResponseEntity<List<PointDto>> getAllPointsSortedByX() {
        log.info("GET /api/points/sorted/x - Retrieving all points sorted by xValue");
        // TODO: Проверка доступа - только ADMIN
        try {
            List<PointDto> points = pointService.getAllPointsSortedByX().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            log.info("Retrieved {} points sorted by xValue", points.size());
            return ResponseEntity.ok(points);
        } catch (Exception e) {
            log.error("Error retrieving points sorted by xValue: {}", e.getMessage(), e);
            throw e;
        }
    }

    // GET /api/points/sorted/y - Все точки, отсортированные по 'yValue' (Только ADMIN)
    @GetMapping("/sorted/y")
    public ResponseEntity<List<PointDto>> getAllPointsSortedByY() {
        log.info("GET /api/points/sorted/y - Retrieving all points sorted by yValue");
        // TODO: Проверка доступа - только ADMIN
        try {
            List<PointDto> points = pointService.getAllPointsSortedByY().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            log.info("Retrieved {} points sorted by yValue", points.size());
            return ResponseEntity.ok(points);
        } catch (Exception e) {
            log.error("Error retrieving points sorted by yValue: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ========== UPDATE OPERATIONS ==========
    // PUT /api/points/function/{functionId}/x/{xValue} - Обновить 'yValue' у точки (Владелец функции или ADMIN)
    @PutMapping("/function/{functionId}/x/{xValue}")
    public ResponseEntity<PointDto> updatePoint(
            @PathVariable Long functionId,
            @PathVariable Double xValue,
            @RequestBody PointDto pointDto) {
        log.info("PUT /api/points/function/{}/x/{} - Updating point with new yValue={}", 
                functionId, xValue, pointDto.getYValue());
        // TODO: Проверка доступа - владелец функции или ADMIN
        try {
            PointEntity updated = pointService.updatePoint(
                    functionId,
                    xValue,
                    pointDto.getYValue()
            );
            log.info("Point updated successfully: functionId={}, xValue={}, yValue={}", 
                    functionId, xValue, updated.getYValue());
            return ResponseEntity.ok(toDto(updated));
        } catch (RuntimeException e) {
            log.error("Error updating point with functionId={}, xValue={}: {}", 
                    functionId, xValue, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ========== DELETE OPERATIONS ==========
    // DELETE /api/points/function/{functionId}/x/{xValue} - Удалить конкретную точку (Владелец функции или ADMIN)
    @DeleteMapping("/function/{functionId}/x/{xValue}")
    public ResponseEntity<Void> deletePoint(
            @PathVariable Long functionId,
            @PathVariable Double xValue) {
        log.info("DELETE /api/points/function/{}/x/{} - Deleting point", functionId, xValue);
        // TODO: Проверка доступа - владелец функции или ADMIN
        try {
            pointService.deletePoint(functionId, xValue);
            log.info("Point deleted successfully: functionId={}, xValue={}", functionId, xValue);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting point with functionId={}, xValue={}: {}", 
                    functionId, xValue, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private PointDto toDto(PointEntity point) {
        return new PointDto(
                point.getFunction().getId(),
                point.getXValue(),
                point.getYValue()
        );
    }
}

