package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByProjectCode(String projectCode);

    List<Project> findByProjectStatus(String projectStatus);

    boolean existsByProjectCode(String projectCode);

    List<Project> findAllByOrderByProjectNameAsc();

    @Query("""
            SELECT p FROM Project p
            WHERE
                (:keyword IS NULL OR :keyword = ''
                 OR LOWER(p.projectCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:status IS NULL OR :status = ''
                 OR p.projectStatus = :status) ORDER BY projectName DESC
            """)
    Page<Project> searchProjects(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);

    boolean existsByProjectCodeAndProjectIdNot(String projectCode, Long projectId);
}
