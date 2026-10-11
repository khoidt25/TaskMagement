package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.entity.TaskRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRoleRepository
        extends JpaRepository<TaskRole, Long> {
    Page<TaskRole> findByActiveTrue(Pageable pageable);
}