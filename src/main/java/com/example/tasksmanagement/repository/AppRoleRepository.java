package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.entity.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppRoleRepository extends JpaRepository<AppRole, Long> {

    Optional<AppRole> findByRoleCode(String roleCode);

    boolean existsByRoleCode(String roleCode);
}
