package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.User;
import com.alexxx2k.springproject.domain.entities.UserEntity;
import com.alexxx2k.springproject.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDomainUser)
                .toList();
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id)
                .map(this::toDomainUser);
    }

    public User createUser(User user, String rawPassword) {
        if (userRepository.existsByUsername(user.username())) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        String passwordHash = passwordEncoder.encode(rawPassword);
        UserEntity entityToSave = new UserEntity(user.username(), passwordHash, user.role(), user.enabled());
        UserEntity savedEntity = userRepository.save(entityToSave);
        return toDomainUser(savedEntity);
    }

    public User updateUser(Integer id, User user, String rawPassword) {
        UserEntity existingEntity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        if (!existingEntity.getUsername().equals(user.username()) &&
                userRepository.existsByUsername(user.username())) {
            throw new IllegalArgumentException("Пользователь с именем '" + user.username() + "' уже существует");
        }
        existingEntity.setUsername(user.username());
        existingEntity.setRole(user.role());
        existingEntity.setEnabled(user.enabled());
        if (rawPassword != null && !rawPassword.trim().isEmpty()) {
            existingEntity.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
        UserEntity savedEntity = userRepository.save(existingEntity);
        return toDomainUser(savedEntity);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    private User toDomainUser(UserEntity entity) {
        return new User(entity.getId(), entity.getUsername(), entity.getPasswordHash(),
                entity.getRole(), entity.getEnabled(), entity.getCreatedAt());
    }
}