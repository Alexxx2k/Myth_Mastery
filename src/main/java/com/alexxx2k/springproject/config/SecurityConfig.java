package com.alexxx2k.springproject.config;

import com.alexxx2k.springproject.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;

        System.out.println("=== SECURITY CONFIG INIT ===");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String testHash = encoder.encode("admin123");
        System.out.println("Test hash for 'admin123': " + testHash);
        System.out.println("Test verification: " + encoder.matches("admin123", testHash));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println("=== CREATING PasswordEncoder BEAN ===");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/register").permitAll()
                        .requestMatchers("/admin/customers/**").hasRole("ADMIN")
                        .requestMatchers("/mythologies/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/mythologies/create", "/mythologies/edit/**", "/mythologies/update/**", "/mythologies/delete/**").hasRole("ADMIN")
                        .requestMatchers("/mythologies/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/mythologies/create", "/mythologies/edit/**", "/mythologies/update/**", "/mythologies/delete/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendRedirect("/access-denied");
        };
    }
}
