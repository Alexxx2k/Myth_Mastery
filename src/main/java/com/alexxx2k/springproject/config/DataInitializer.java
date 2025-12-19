package com.alexxx2k.springproject.config;

import com.alexxx2k.springproject.domain.entities.UserEntity;
import com.alexxx2k.springproject.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            createUser("admin", "admin123", "ADMIN");
            createUser("user", "admin123", "USER");
        }
    }

    private void createUser(String username, String password, String role) {
        if (userRepository.findByUsername(username).isEmpty()) {
            String passwordHash = passwordEncoder.encode(password);
            UserEntity user = new UserEntity(username, passwordHash, role, true);
            userRepository.save(user);
        }
    }
}