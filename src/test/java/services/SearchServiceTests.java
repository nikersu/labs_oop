package services;

import config.AppConfig;
import entities.FunctionEntity;
import entities.PointEntity;
import entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Transactional;
import repositories.FunctionRepository;
import repositories.PointRepository;
import repositories.UserRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureTestDatabase
@EntityScan(basePackages = "entities")
@EnableJpaRepositories(basePackages = "repositories")
@Import(AppConfig.class)
@Transactional
class SearchServiceTests {

    @Autowired
    private SearchService searchService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FunctionRepository functionRepository;
    @Autowired
    private PointRepository pointRepository;

    private UserEntity user;
    private FunctionEntity f1;
    private FunctionEntity f2;

    @BeforeEach
    void setUp() {
        pointRepository.deleteAll();
        functionRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(new UserEntity("user_" + UUID.randomUUID(), "hash"));
        f1 = functionRepository.save(new FunctionEntity("alpha", "x^2", user));
        f2 = functionRepository.save(new FunctionEntity("beta", "sin(x)", user));
        pointRepository.saveAll(List.of(
                new PointEntity(f1, 0.0, 0.0),
                new PointEntity(f1, 1.0, 1.0),
                new PointEntity(f1, 2.0, 4.0),
                new PointEntity(f2, 1.0, 0.84)
        ));
    }

    @Test
    void singleAndMultipleSearchWithSorting() {
        assertThat(searchService.findUserByUsername(user.getUsername())).isPresent();
        assertThat(searchService.findFunctionById(f1.getId())).isPresent();
        assertThat(searchService.findPoint(f1.getId(), 1.0)).isPresent();

        var users = searchService.findUsers(List.of(user.getUsername()), Sort.by("username"));
        assertThat(users).hasSize(1);

        var functions = searchService.searchFunctions(user.getId(), "a", Sort.by(Sort.Order.asc("name")));
        assertThat(functions).extracting(FunctionEntity::getName).containsExactly("alpha");

        var points = searchService.searchPoints(f1.getId(), 0.5, 2.0, Sort.by(Sort.Order.desc("id.xValue")));
        assertThat(points).extracting(PointEntity::getXValue).containsExactly(2.0, 1.0);
    }

    @Test
    void breadthFirstAndDepthFirstTraversal() {
        var bfs = searchService.breadthFirstHierarchy(user.getId());
        var dfs = searchService.depthFirstHierarchy(user.getId());

        // BFS must start with user then functions then points
        assertThat(bfs).isNotEmpty();
        assertThat(bfs.get(0)).isInstanceOf(UserEntity.class);
        assertThat(bfs.stream().filter(FunctionEntity.class::isInstance).count()).isEqualTo(2);

        // DFS must have user then function f1 before its points, etc.
        assertThat(dfs).isNotEmpty();
        assertThat(dfs.get(0)).isInstanceOf(UserEntity.class);
        assertThat(dfs.stream().anyMatch(PointEntity.class::isInstance)).isTrue();
    }
}


