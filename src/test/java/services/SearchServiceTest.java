package services;

import entities.FunctionEntity;
import entities.PointEntity;
import entities.PointId;
import entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import repositories.FunctionRepository;
import repositories.PointRepository;
import repositories.UserRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FunctionRepository functionRepository;

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private SearchService searchService;

    private UserEntity user;
    private FunctionEntity f1;
    private FunctionEntity f2;
    private PointEntity p1;
    private PointEntity p2;

    @BeforeEach
    void setUp() {
        user = new UserEntity("user1", "hash");
        setId(user, "id", 1L);

        f1 = new FunctionEntity("alpha", "x^2", user);
        f2 = new FunctionEntity("beta", "sin(x)", user);
        setId(f1, "id", 10L);
        setId(f2, "id", 20L);

        p1 = new PointEntity();
        p1.setFunction(f1);
        p1.setId(new PointId(f1.getId(), 1.0));
        p1.setY(1.0);

        p2 = new PointEntity();
        p2.setFunction(f1);
        p2.setId(new PointId(f1.getId(), 2.0));
        p2.setY(4.0);
    }

    // ---------- Одиночный поиск ----------

    @Test
    void findSingleEntities() {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(functionRepository.findById(10L)).thenReturn(Optional.of(f1));
        when(pointRepository.findById(new PointId(10L, 1.0))).thenReturn(Optional.of(p1));

        assertThat(searchService.findUserByUsername("user1")).contains(user);
        assertThat(searchService.findFunctionById(10L)).contains(f1);
        assertThat(searchService.findPoint(10L, 1.0)).contains(p1);

        // guard-ветка для null параметров
        assertThat(searchService.findPoint(null, 1.0)).isEmpty();
        assertThat(searchService.findPoint(10L, null)).isEmpty();
    }

    // ---------- Множественный поиск с сортировкой ----------

    @Test
    void multipleSearchWithSorting() {
        when(userRepository.findByUsernameIn(eq(List.of("user1")), any(Sort.class)))
                .thenReturn(List.of(user));
        when(functionRepository.findByUserIdAndNameContainingIgnoreCase(eq(1L), eq("a"), any(Sort.class)))
                .thenReturn(List.of(f1));
        when(pointRepository.findByFunctionIdAndIdXValueBetween(eq(10L), eq(0.0), eq(5.0), any(Sort.class)))
                .thenReturn(List.of(p1, p2));

        var users = searchService.findUsers(List.of("user1"), Sort.by("username"));
        var functions = searchService.searchFunctions(1L, "a", Sort.by("name"));
        var points = searchService.searchPoints(10L, 0.0, 5.0, Sort.by(Sort.Order.desc("id.xValue")));

        assertThat(users).containsExactly(user);
        assertThat(functions).containsExactly(f1);
        assertThat(points).containsExactly(p1, p2);

        // Пустой / null ввод -> пустой результат без обращений к репозиторию пользователей
        assertThat(searchService.findUsers(List.of(), Sort.unsorted())).isEmpty();
        assertThat(searchService.findUsers(null, Sort.unsorted())).isEmpty();
        verify(userRepository, times(1)).findByUsernameIn(anyList(), any(Sort.class));
    }

    // ---------- Поиск по иерархии: в ширину и в глубину ----------

    @Test
    void breadthFirstAndDepthFirstHierarchy() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(functionRepository.findByUserId(1L)).thenReturn(List.of(f1, f2));
        when(pointRepository.findByIdFunctionIdIn(eq(List.of(10L, 20L)), any(Sort.class)))
                .thenReturn(List.of(p1, p2));

        var bfs = searchService.breadthFirstHierarchy(1L);
        var dfs = searchService.depthFirstHierarchy(1L);

        // BFS: пользователь, потом функции, потом точки
        assertThat(bfs).contains(user);
        assertThat(bfs.stream().filter(FunctionEntity.class::isInstance).count()).isEqualTo(2);
        assertThat(bfs.stream().filter(PointEntity.class::isInstance).count()).isEqualTo(2);

        // DFS: пользователь, затем каждая функция со своими точками
        assertThat(dfs.get(0)).isInstanceOf(UserEntity.class);
        assertThat(dfs.stream().anyMatch(PointEntity.class::isInstance)).isTrue();

        // Если пользователь не найден – пустой результат
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThat(searchService.breadthFirstHierarchy(2L)).isEmpty();
        assertThat(searchService.depthFirstHierarchy(2L)).isEmpty();
    }

    // ---------- Фильтрация коллекции функций с сортировкой ----------

    @Test
    void filterFunctionsWithPredicateAndSorting() {
        var src = List.of(f2, f1); // заведомо «перемешанный» список

        // Фильтруем функции с именем, содержащим 'a', сортируем по имени
        var sorted = searchService.filterFunctions(
                src,
                f -> f.getName().contains("a"),
                Sort.by(Sort.Order.asc("name"))
        );

        assertThat(sorted).containsExactly(f1, f2); // alpha, beta

        // Пустой / null источник -> пустой результат
        assertThat(searchService.filterFunctions(List.of(), f -> true, Sort.unsorted())).isEmpty();
        assertThat(searchService.filterFunctions(null, f -> true, Sort.unsorted())).isEmpty();
    }

    // ---------- Вспомогательный метод для установки ID через reflection ----------

    private static void setId(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set id via reflection", e);
        }
    }
}



