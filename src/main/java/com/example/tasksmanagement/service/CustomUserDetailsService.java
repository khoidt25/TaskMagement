
package com.example.tasksmanagement.service;

import com.example.tasksmanagement.entity.AppRole;
import com.example.tasksmanagement.entity.User;
import com.example.tasksmanagement.repository.AppRoleRepository;
import com.example.tasksmanagement.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AppRoleRepository appRoleRepository;

    public CustomUserDetailsService(
            UserRepository userRepository,
            AppRoleRepository appRoleRepository) {
        this.userRepository = userRepository;
        this.appRoleRepository = appRoleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Username or password is incorrect"
                        ));

        AppRole role = appRoleRepository.findById(user.getRoleId())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Role is not configured for this user"
                        ));

        String roleCode = role.getRoleCode()
                .trim()
                .toUpperCase(Locale.ROOT);

        boolean enabled = user.getUserStatus() == null
                || "ACTIVE".equalsIgnoreCase(user.getUserStatus());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(new SimpleGrantedAuthority("ROLE_" + roleCode))
                .disabled(!enabled)
                .build();
    }
}

