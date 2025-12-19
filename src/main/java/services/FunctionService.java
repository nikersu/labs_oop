package services;

import entities.FunctionEntity;
import entities.UserEntity;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.FunctionRepository;
import repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FunctionService {

    private final FunctionRepository functionRepository;
    private final UserRepository userRepository;

    public FunctionService(FunctionRepository functionRepository, UserRepository userRepository) {
        this.functionRepository = functionRepository;
        this.userRepository = userRepository;
    }

    public FunctionEntity createFunction(String name, String expression, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        FunctionEntity function = new FunctionEntity(name, expression, user);
        return functionRepository.save(function);
    }

    public List<FunctionEntity> getAllFunctions() {
        return functionRepository.findAll();
    }

    public List<FunctionEntity> getAllFunctionsSorted() {
        return functionRepository.findAll(Sort.by("name"));
    }

    public Optional<FunctionEntity> getFunctionById(Long id) {
        return functionRepository.findById(id);
    }

    public List<FunctionEntity> getFunctionsByUserId(Long userId) {
        return functionRepository.findByUserId(userId);
    }

    public FunctionEntity updateFunction(Long id, String name, String expression, Long userId) {
        FunctionEntity function = functionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Function not found with id: " + id));
        if (name != null) {
            function.setName(name);
        }
        if (expression != null) {
            function.setExpression(expression);
        }
        if (userId != null) {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            function.setUser(user);
        }
        return functionRepository.save(function);
    }

    public void deleteFunction(Long id) {
        functionRepository.deleteById(id);
    }
}

