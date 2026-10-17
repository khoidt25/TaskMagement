package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.entity.ManagerProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManagerProjectRepository
        extends JpaRepository<ManagerProject, ManagerProject.ManagerProjectId> {

    List<ManagerProject> findByManagerId(Long managerId);

    boolean existsByManagerIdAndProjectId(
            Long managerId, Long projectId);

    void deleteByManagerIdAndProjectId(
            Long managerId, Long projectId);

    void deleteByManagerId(Long managerId);

    List<ManagerProject> findByProjectId(Long projectId);
}