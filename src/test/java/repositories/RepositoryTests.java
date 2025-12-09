package repositories;

import config.AppConfig;
import entities.FunctionEntity;
import entities.PointEntity;
import entities.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureTestDatabase
@EntityScan(basePackages = "entities")
@EnableJpaRepositories(basePackages = "repositories")
@Import(AppConfig.class)
class RepositoryTests {

    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;

    @Autowired
    RepositoryTests(UserRepository userRepository,
                    FunctionRepository functionRepository,
                    PointRepository pointRepository) {
        this.userRepository = userRepository;
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
    }

    @Test
    void createSearchAndDeleteEntities() {
        // create user
        UserEntity user = new UserEntity("user_" + UUID.randomUUID(), "hash_" + UUID.randomUUID());
        user = userRepository.save(user);

        // create functions for user
        FunctionEntity f1 = functionRepository.save(new FunctionEntity("f1", "x^2", user));
        FunctionEntity f2 = functionRepository.save(new FunctionEntity("f2", "sin(x)", user));

        // create points for f1
        List<PointEntity> points = new ArrayList<>();
        points.add(new PointEntity(f1, 0.0, 0.0));
        points.add(new PointEntity(f1, 1.0, 1.0));
        points.add(new PointEntity(f1, 2.0, 4.0));
        pointRepository.saveAll(points);

        // search checks
        assertThat(userRepository.findByUsername(user.getUsername())).isPresent();
        assertThat(functionRepository.findByUserId(user.getId()))
                .extracting(FunctionEntity::getName)
                .containsExactlyInAnyOrder("f1", "f2");
        assertThat(pointRepository.findByFunctionId(f1.getId()))
                .hasSize(3)
                .allMatch(p -> p.getFunction().getId().equals(f1.getId()));

        // delete points, functions, user
        pointRepository.deleteAll(points);
        functionRepository.delete(f2);
        functionRepository.delete(f1);
        userRepository.delete(user);

        assertThat(pointRepository.findByFunctionId(f1.getId())).isEmpty();
        assertThat(functionRepository.findByUserId(user.getId())).isEmpty();
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }
}

