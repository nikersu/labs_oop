package services;

import entities.UserEntity;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity createUser(String username, String passwordHash, String role) {
        UserEntity user = new UserEntity(username, passwordHash);
        if (role != null) {
            user.setRole(role);
        }
        return userRepository.save(user);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public List<UserEntity> getAllUsersSorted() {
        return userRepository.findAll(Sort.by("username"));
    }

    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public UserEntity updateUser(Long id, String username, String passwordHash, String role) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        if (username != null) {
            user.setUsername(username);
        }
        if (passwordHash != null) {
            user.setPasswordHash(passwordHash);
        }
        if (role != null) {
            user.setRole(role);
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}

