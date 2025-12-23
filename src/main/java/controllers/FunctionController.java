package controllers;

import dto.FunctionDto;
import dto.PointDto;
import entities.FunctionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import services.FunctionService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/functions")
public class FunctionController {

    private static final Logger logger = LoggerFactory.getLogger(FunctionController.class);

    private final FunctionService functionService;

    public FunctionController(FunctionService functionService) {
        this.functionService = functionService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FunctionDto>> getAllFunctions() {
        List<FunctionDto> functions = functionService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(functions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@accessService.canAccessFunction(#id, authentication)")
    public ResponseEntity<FunctionDto> getFunctionById(@PathVariable Long id) {
        return functionService.findById(id)
                .map(func -> ResponseEntity.ok(toDto(func)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("@accessService.canAccessUserFunctions(#userId, authentication)")
    public ResponseEntity<List<FunctionDto>> getFunctionsByUserId(@PathVariable Long userId) {
        List<FunctionDto> functions = functionService.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(functions);
    }

    @GetMapping("/user/{userId}/search")
    @PreAuthorize("@accessService.canAccessUserFunctions(#userId, authentication)")
    public ResponseEntity<List<FunctionDto>> searchFunctions(
            @PathVariable Long userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        String nameLike = name == null ? "" : name;
        List<FunctionDto> functions = functionService.findByUserIdAndNameContaining(userId, nameLike, sort).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(functions);
    }

    @PostMapping
    @PreAuthorize("@accessService.canAccessUser(#functionDto.userId, authentication)")
    public ResponseEntity<FunctionDto> createFunction(@RequestBody FunctionDto functionDto) {
        FunctionEntity function = functionService.createFunction(
                functionDto.getName(),
                functionDto.getExpression(),
                functionDto.getUserId()
        );
        logger.info("Function created via API. id={}, userId={}", function.getId(), function.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(function));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessService.canAccessFunction(#id, authentication)")
    public ResponseEntity<FunctionDto> updateFunction(@PathVariable Long id, @RequestBody FunctionDto functionDto) {
        return functionService.findById(id)
                .map(func -> {
                    func.setName(functionDto.getName());
                    func.setExpression(functionDto.getExpression());
                    FunctionEntity updated = functionService.save(func);
                    logger.info("Function updated via API. id={}", updated.getId());
                    return ResponseEntity.ok(toDto(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessService.canAccessFunction(#id, authentication)")
    public ResponseEntity<Void> deleteFunction(@PathVariable Long id) {
        if (functionService.existsById(id)) {
            functionService.deleteById(id);
            logger.info("Function deleted via API. id={}", id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private FunctionDto toDto(FunctionEntity function) {
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
