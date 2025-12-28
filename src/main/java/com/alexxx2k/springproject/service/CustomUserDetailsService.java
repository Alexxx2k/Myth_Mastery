package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.entities.CustomerEntity;
import com.alexxx2k.springproject.repository.CustomerRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    // Встроенный админ с ПРАВИЛЬНЫМ статическим хэшем
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD_HASH = "$2a$10$EjbsKoRVAo5bd2HTw0k3ceIP0nhqlrgtrh53BbBxv/LF/mDRIxrRG"; // admin123

    // УБРАТЬ BCryptPasswordEncoder из конструктора
    public CustomUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Проверяем встроенного админа
        if (ADMIN_USERNAME.equals(username)) {
            System.out.println("DEBUG: Admin login attempt");
            return new User(
                    ADMIN_USERNAME,
                    ADMIN_PASSWORD_HASH,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }

        // 2. Ищем в customer (по email)
        CustomerEntity customer = customerRepository.findByEmail(username)
                .orElseThrow(() -> {
                    System.out.println("DEBUG: Customer not found: " + username);
                    return new UsernameNotFoundException("Пользователь не найден: " + username);
                });

        System.out.println("DEBUG: Customer found: " + customer.getEmail());
        return new User(
                customer.getEmail(),
                customer.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
