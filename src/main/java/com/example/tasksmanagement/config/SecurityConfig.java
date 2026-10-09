package com.example.tasksmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // REST API không sử dụng CSRF token
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Swagger
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // Login
                        .requestMatchers("/login").permitAll()

                        // Các API cần đăng nhập
                        .anyRequest().authenticated())

                // Cho phép Basic Auth
                .httpBasic(httpBasic -> {
                })

                // Cho phép form login
                .formLogin(form -> form.permitAll());

        return http.build();
    }
}