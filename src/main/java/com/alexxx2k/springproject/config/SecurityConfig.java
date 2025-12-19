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
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("/personal/delete/**", "/autos/delete/**", "/routes/delete/**").hasRole("ADMIN")
                        .requestMatchers("/personal/create", "/personal/edit/**", "/personal/update/**",
                                "/autos/create", "/autos/edit/**", "/autos/update/**",
                                "/routes/create", "/routes/edit/**", "/routes/update/**",
                                "/journal/start", "/journal/end", "/journal/edit/**", "/journal/update/**","/journal/delete/**"
                        ).hasRole("ADMIN")
                        // temp. toDelete in future:
                        .requestMatchers("/mythologies/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/mythologies/create", "/mythologies/edit/**", "/mythologies/update/**", "/mythologies/delete/**").hasRole("ADMIN")
                        // В SecurityConfig.java в методе securityFilterChain добавьте:
                        .requestMatchers("/mythologies/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/mythologies/create", "/mythologies/edit/**", "/mythologies/update/**", "/mythologies/delete/**").hasRole("ADMIN")
                        .requestMatchers("/personal/**", "/autos/**", "/routes/**", "/journal/**").hasAnyRole("ADMIN", "USER")
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
