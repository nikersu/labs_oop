package services;

import entities.FunctionEntity;
import entities.PointEntity;
import entities.PointId;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.FunctionRepository;
import repositories.PointRepository;

import java.util.List;

@Service
@Transactional
public class PointService {

    private final PointRepository pointRepository;
    private final FunctionRepository functionRepository;

    public PointService(PointRepository pointRepository, FunctionRepository functionRepository) {
        this.pointRepository = pointRepository;
        this.functionRepository = functionRepository;
    }

    public PointEntity createPoint(Long functionId, Double xValue, Double yValue) {
        FunctionEntity function = functionRepository.findById(functionId)
                .orElseThrow(() -> new RuntimeException("Function not found with id: " + functionId));
        PointEntity point = new PointEntity(function, xValue, yValue);
        return pointRepository.save(point);
    }

    public List<PointEntity> getPointsByFunctionId(Long functionId) {
        return pointRepository.findByFunctionId(functionId);
    }

    public List<PointEntity> getAllPointsSortedByX() {
        return pointRepository.findAll(Sort.by("id.xValue"));
    }

    public List<PointEntity> getAllPointsSortedByY() {
        return pointRepository.findAll(Sort.by("yValue"));
    }

    public PointEntity updatePoint(Long functionId, Double xValue, Double yValue) {
        PointId pointId = new PointId(functionId, xValue);
        PointEntity point = pointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException("Point not found"));
        point.setYValue(yValue);
        return pointRepository.save(point);
    }

    public void deletePoint(Long functionId, Double xValue) {
        PointId pointId = new PointId(functionId, xValue);
        pointRepository.deleteById(pointId);
    }
}

