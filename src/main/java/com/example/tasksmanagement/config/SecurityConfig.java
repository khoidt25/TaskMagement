package com.example.tasksmanagement.config;

import com.example.tasksmanagement.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Mã hóa mật khẩu bằng BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Xác thực tài khoản thông qua CustomUserDetailsService.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    /**
     * Dùng cho API đăng nhập.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    /**
     * Lưu thông tin đăng nhập vào HTTP Session.
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider, SecurityContextRepository securityContextRepository) throws Exception {

        http.authenticationProvider(authenticationProvider)

                // Sử dụng session, không sử dụng JWT
                .securityContext(context -> context.securityContextRepository(securityContextRepository))

                // Giữ cấu hình hiện tại để tránh làm thay đổi luồng gọi API.
                // Khi triển khai thực tế, nên cấu hình CSRF phù hợp với Angular.
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // API đăng nhập và tài liệu Swagger không cần đăng nhập
                        .requestMatchers("/api/auth/login", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Chỉ ADMIN được quản lý tài khoản người dùng
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // MEMBER cập nhật phần việc được giao cho chính mình
                        .requestMatchers(HttpMethod.PUT, "/api/tasks/my-members/**").hasAnyRole("ADMIN", "LEADER", "MANAGER", "MEMBER")

                        // ADMIN, LEADER, MANAGER được giao việc cho thành viên
                        .requestMatchers(HttpMethod.POST, "/api/tasks/*/members").hasAnyRole("ADMIN", "LEADER", "MANAGER")

                        // Chỉ người quản lý được cập nhật assignment của thành viên
                        .requestMatchers(HttpMethod.PUT, "/api/tasks/*/members/*").hasAnyRole("ADMIN", "LEADER", "MANAGER")

                        // Chỉ người quản lý được tạo task
                        .requestMatchers(HttpMethod.POST, "/api/tasks").hasAnyRole("ADMIN", "LEADER", "MANAGER")

                        // Chỉ người quản lý được sửa thông tin task
                        .requestMatchers(HttpMethod.PUT, "/api/tasks/*").hasAnyRole("ADMIN", "LEADER", "MANAGER")

                        // Chỉ người quản lý được xóa task
                        .requestMatchers(HttpMethod.DELETE, "/api/tasks/**").hasAnyRole("ADMIN", "LEADER", "MANAGER")

                        // Dashboard cá nhân
                        .requestMatchers("/api/dashboard/my-summary").hasAnyRole("ADMIN", "LEADER", "MANAGER", "MEMBER")

                        // Dashboard tổng hợp
                        .requestMatchers("/api/dashboard/**").hasAnyRole("ADMIN", "LEADER", "MANAGER")

                        // Các API task còn lại yêu cầu đăng nhập
                        .requestMatchers("/api/tasks/**").authenticated()

                        // Những API còn lại cũng yêu cầu đăng nhập
                        .anyRequest().authenticated())

                // Dùng API login riêng, không dùng trang login mặc định
                .formLogin(form -> form.disable())

                // Không bật HTTP Basic vì ứng dụng đang dùng session
                .httpBasic(basic -> basic.disable())

                // Đăng xuất và hủy session
                .logout(logout -> logout.logoutUrl("/api/auth/logout").invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID"));

        return http.build();
    }
}