package controllers;

import dto.PointDto;
import entities.PointEntity;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.PointService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @GetMapping
    public ResponseEntity<List<PointDto>> getAllPoints() {
        List<PointDto> points = pointService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(points);
    }

    @GetMapping("/function/{functionId}")
    public ResponseEntity<List<PointDto>> getPointsByFunctionId(@PathVariable Long functionId) {
        List<PointDto> points = pointService.findByFunctionId(functionId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(points);
    }

    @GetMapping("/function/{functionId}/range")
    public ResponseEntity<List<PointDto>> getPointsByFunctionIdAndRange(
            @PathVariable Long functionId,
            @RequestParam(required = false) Double fromX,
            @RequestParam(required = false) Double toX,
            @RequestParam(required = false, defaultValue = "id.xValue") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        List<PointDto> points = pointService.findByFunctionIdAndXValueBetween(functionId, fromX, toX, sort).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(points);
    }

    @GetMapping("/function/{functionId}/x/{xValue}")
    public ResponseEntity<PointDto> getPoint(@PathVariable Long functionId, @PathVariable Double xValue) {
        return pointService.findById(functionId, xValue)
                .map(point -> ResponseEntity.ok(toDto(point)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PointDto> createPoint(@RequestBody PointDto pointDto) {
        PointEntity point = pointService.createPoint(
                pointDto.getFunctionId(),
                pointDto.getXValue(),
                pointDto.getYValue()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(point));
    }

    @PutMapping("/function/{functionId}/x/{xValue}")
    public ResponseEntity<PointDto> updatePoint(
            @PathVariable Long functionId,
            @PathVariable Double xValue,
            @RequestBody PointDto pointDto) {
        return pointService.findById(functionId, xValue)
                .map(point -> {
                    point.setYValue(pointDto.getYValue());
                    PointEntity updated = pointService.save(point);
                    return ResponseEntity.ok(toDto(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/function/{functionId}/x/{xValue}")
    public ResponseEntity<Void> deletePoint(@PathVariable Long functionId, @PathVariable Double xValue) {
        if (pointService.existsById(functionId, xValue)) {
            pointService.deleteById(functionId, xValue);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private PointDto toDto(PointEntity point) {
        return new PointDto(
                point.getFunction().getId(),
                point.getXValue(),
                point.getYValue()
        );
    }
}



