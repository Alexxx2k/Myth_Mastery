package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.entities.UserEntity;
import com.alexxx2k.springproject.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        if (!userEntity.getEnabled()) {
            throw new UsernameNotFoundException("Пользователь заблокирован: " + username);
        }

        return new User(userEntity.getUsername(), userEntity.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole())));
    }
}