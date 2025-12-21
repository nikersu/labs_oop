package controllers;

import dto.FunctionDto;
import dto.PointDto;
import dto.UserDto;
import entities.FunctionEntity;
import entities.PointEntity;
import entities.UserEntity;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.SearchService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/users/username/{username}")
    public ResponseEntity<UserDto> findUserByUsername(@PathVariable String username) {
        return searchService.findUserByUsername(username)
                .map(user -> ResponseEntity.ok(new UserDto(user.getId(), user.getUsername(), user.getPasswordHash())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/functions/{id}")
    public ResponseEntity<FunctionDto> findFunctionById(@PathVariable Long id) {
        return searchService.findFunctionById(id)
                .map(func -> ResponseEntity.ok(toFunctionDto(func)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/points/function/{functionId}/x/{xValue}")
    public ResponseEntity<PointDto> findPoint(@PathVariable Long functionId, @PathVariable Double xValue) {
        return searchService.findPoint(functionId, xValue)
                .map(point -> ResponseEntity.ok(new PointDto(point.getFunction().getId(), point.getXValue(), point.getYValue())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/functions/user/{userId}")
    public ResponseEntity<List<FunctionDto>> searchFunctions(
            @PathVariable Long userId,
            @RequestParam(required = false) String nameLike,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        List<FunctionDto> functions = searchService.searchFunctions(userId, nameLike, sort).stream()
                .map(this::toFunctionDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(functions);
    }

    @GetMapping("/points/function/{functionId}")
    public ResponseEntity<List<PointDto>> searchPoints(
            @PathVariable Long functionId,
            @RequestParam(required = false) Double fromX,
            @RequestParam(required = false) Double toX,
            @RequestParam(required = false, defaultValue = "id.xValue") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        List<PointDto> points = searchService.searchPoints(functionId, fromX, toX, sort).stream()
                .map(point -> new PointDto(point.getFunction().getId(), point.getXValue(), point.getYValue()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(points);
    }

    @GetMapping("/hierarchy/user/{userId}/breadth-first")
    public ResponseEntity<List<Object>> breadthFirstHierarchy(@PathVariable Long userId) {
        List<Object> hierarchy = searchService.breadthFirstHierarchy(userId);
        return ResponseEntity.ok(hierarchy);
    }

    @GetMapping("/hierarchy/user/{userId}/depth-first")
    public ResponseEntity<List<Object>> depthFirstHierarchy(@PathVariable Long userId) {
        List<Object> hierarchy = searchService.depthFirstHierarchy(userId);
        return ResponseEntity.ok(hierarchy);
    }

    private FunctionDto toFunctionDto(FunctionEntity function) {
        FunctionDto dto = new FunctionDto(
                function.getId(),
                function.getName(),
                function.getExpression(),
                function.getUser().getId()
        );
        if (function.getPoints() != null && !function.getPoints().isEmpty()) {
            List<PointDto> points = function.getPoints().stream()
                    .map(p -> new PointDto(p.getFunction().getId(), p.getXValue(), p.getYValue()))
                    .collect(Collectors.toList());
            dto.setPoints(points);
        }
        return dto;
    }
}

