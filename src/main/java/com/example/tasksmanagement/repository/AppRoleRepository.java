package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.entity.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {

    Optional<AppRole> findByRoleCodeIgnoreCase(String roleCode);
}


