package services;

import entities.FunctionEntity;
import entities.UserEntity;
import repositories.FunctionRepository;
import repositories.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<FunctionEntity> findAll() {
        return functionRepository.findAll();
    }

    public Optional<FunctionEntity> findById(Long id) {
        return functionRepository.findById(id);
    }

    public List<FunctionEntity> findByUserId(Long userId) {
        return functionRepository.findByUserId(userId);
    }

    public List<FunctionEntity> findByUserIdAndNameContaining(Long userId, String nameLike, Sort sort) {
        return functionRepository.findByUserIdAndNameContainingIgnoreCase(userId, nameLike, sort);
    }

    public FunctionEntity save(FunctionEntity function) {
        return functionRepository.save(function);
    }

    public FunctionEntity createFunction(String name, String expression, Long userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        FunctionEntity function = new FunctionEntity(name, expression, userOpt.get());
        return functionRepository.save(function);
    }

    public void deleteById(Long id) {
        functionRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return functionRepository.existsById(id);
    }
}



