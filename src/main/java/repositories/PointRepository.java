package repositories;

import entities.PointEntity;
import entities.PointId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointRepository extends JpaRepository<PointEntity, PointId> {
    List<PointEntity> findByFunctionId(Long functionId);
}

