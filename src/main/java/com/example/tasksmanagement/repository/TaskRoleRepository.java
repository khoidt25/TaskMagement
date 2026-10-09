package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.entity.TaskRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRoleRepository
        extends JpaRepository<TaskRole, Long> {
}