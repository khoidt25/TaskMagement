
package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByProjectCode(String projectCode);

    List<Project> findByProjectStatus(String projectStatus);

    boolean existsByProjectCode(String projectCode);

    List<Project> findAllByOrderByProjectNameAsc();
}
