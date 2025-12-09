package repositories;

import entities.PointEntity;
import entities.PointId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Collection;

public interface PointRepository extends JpaRepository<PointEntity, PointId> {
    List<PointEntity> findByFunctionId(Long functionId);

    List<PointEntity> findByFunctionIdAndIdXValueBetween(Long functionId, Double from, Double to, Sort sort);

    List<PointEntity> findByIdFunctionIdIn(Collection<Long> functionIds, Sort sort);
}

