package services;

import entities.FunctionEntity;
import entities.PointEntity;
import entities.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.FunctionRepository;
import repositories.PointRepository;
import repositories.UserRepository;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;

    public SearchService(UserRepository userRepository,
                         FunctionRepository functionRepository,
                         PointRepository pointRepository) {
        this.userRepository = userRepository;
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
    }

    // ---------- Single entity searches ----------

    public Optional<UserEntity> findUserByUsername(String username) {
        log.info("Searching user by username='{}'", username);
        return userRepository.findByUsername(username);
    }

    public Optional<FunctionEntity> findFunctionById(Long id) {
        log.info("Searching function by id={}", id);
        return functionRepository.findById(id);
    }

    public Optional<PointEntity> findPoint(Long functionId, Double xValue) {
        log.info("Searching point by functionId={}, x={}", functionId, xValue);
        if (functionId == null || xValue == null) {
            return Optional.empty();
        }
        return pointRepository.findById(new entities.PointId(functionId, xValue));
    }

    // ---------- Multiple entity searches with sorting ----------

    public List<UserEntity> findUsers(Collection<String> usernames, Sort sort) {
        log.info("Searching users by usernames={} with sort={}", usernames, sort);
        if (usernames == null || usernames.isEmpty()) {
            return List.of();
        }
        return userRepository.findByUsernameIn(usernames, sort);
    }

    public List<FunctionEntity> searchFunctions(Long userId, String nameLike, Sort sort) {
        log.info("Searching functions for userId={} nameLike='{}' sort={}", userId, nameLike, sort);
        if (userId == null) {
            return List.of();
        }
        String like = nameLike == null ? "" : nameLike;
        return functionRepository.findByUserIdAndNameContainingIgnoreCase(userId, like, sort);
    }

    public List<PointEntity> searchPoints(Long functionId, Double fromX, Double toX, Sort sort) {
        log.info("Searching points for functionId={} fromX={} toX={} sort={}", functionId, fromX, toX, sort);
        if (functionId == null) {
            return List.of();
        }
        double from = fromX == null ? Double.NEGATIVE_INFINITY : fromX;
        double to = toX == null ? Double.POSITIVE_INFINITY : toX;
        return pointRepository.findByFunctionIdAndIdXValueBetween(functionId, from, to, sort);
    }

    // ---------- Hierarchical traversals ----------

    public List<Object> breadthFirstHierarchy(Long userId) {
        log.info("Breadth-first traversal for userId={}", userId);
        return traverseHierarchy(userId, false);
    }

    public List<Object> depthFirstHierarchy(Long userId) {
        log.info("Depth-first traversal for userId={}", userId);
        return traverseHierarchy(userId, true);
    }

    private List<Object> traverseHierarchy(Long userId, boolean depthFirst) {
        Optional<UserEntity> userOpt = userId == null ? Optional.empty() : userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.warn("User not found, traversal aborted for userId={}", userId);
            return List.of();
        }
        UserEntity user = userOpt.get();
        List<FunctionEntity> functions = functionRepository.findByUserId(user.getId());
        List<PointEntity> points = pointRepository.findByIdFunctionIdIn(
                functions.stream().map(FunctionEntity::getId).collect(Collectors.toList()),
                Sort.unsorted()
        );

        List<Object> result = new ArrayList<>();
        if (depthFirst) {
            depthFirstCollect(user, functions, points, result);
        } else {
            breadthFirstCollect(user, functions, points, result);
        }
        return result;
    }

    private void depthFirstCollect(UserEntity user, List<FunctionEntity> functions, List<PointEntity> points, List<Object> sink) {
        sink.add(user);
        for (FunctionEntity f : functions) {
            sink.add(f);
            points.stream()
                    .filter(p -> p.getFunction().getId().equals(f.getId()))
                    .forEach(sink::add);
        }
    }

    private void breadthFirstCollect(UserEntity user, List<FunctionEntity> functions, List<PointEntity> points, List<Object> sink) {
        Deque<Object> queue = new ArrayDeque<>();
        queue.add(user);
        while (!queue.isEmpty()) {
            Object node = queue.poll();
            sink.add(node);
            if (node instanceof UserEntity u) {
                functions.stream()
                        .filter(f -> f.getUser().getId().equals(u.getId()))
                        .forEach(queue::add);
            } else if (node instanceof FunctionEntity f) {
                points.stream()
                        .filter(p -> p.getFunction().getId().equals(f.getId()))
                        .forEach(queue::add);
            }
        }
    }

    // ---------- Utility filters ----------

    public List<FunctionEntity> filterFunctions(Collection<FunctionEntity> source, Predicate<FunctionEntity> predicate, Sort sort) {
        log.info("Filtering {} functions with custom predicate and sort={}", source == null ? 0 : source.size(), sort);
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        return source.stream()
                .filter(predicate)
                .sorted((a, b) -> {
                    if (sort.isUnsorted()) return 0;
                    return sort.get()
                            .map(order -> compareFunctions(order, a, b))
                            .findFirst()
                            .orElse(0);
                })
                .toList();
    }

    private int compareFunctions(Sort.Order order, FunctionEntity a, FunctionEntity b) {
        int cmp = switch (order.getProperty()) {
            case "name" -> a.getName().compareToIgnoreCase(b.getName());
            case "id" -> Long.compare(
                    a.getId() == null ? Long.MIN_VALUE : a.getId(),
                    b.getId() == null ? Long.MIN_VALUE : b.getId()
            );
            default -> 0;
        };
        return order.isAscending() ? cmp : -cmp;
    }
}




