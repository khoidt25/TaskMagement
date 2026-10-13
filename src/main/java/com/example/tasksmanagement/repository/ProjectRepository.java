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
                 OR p.projectStatus = :status) ORDER BY projectName ASC
            """)
    Page<Project> searchProjects(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);

    @Query("""
        SELECT p FROM Project p
        WHERE p.projectId IN (
            SELECT mp.projectId
            FROM ManagerProject mp
            WHERE mp.managerId = :managerId
        )
        AND (:keyword IS NULL OR :keyword = ''
             OR LOWER(p.projectCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:status IS NULL OR :status = ''
             OR p.projectStatus = :status)
        """)
    Page<Project> searchProjectsByManager(
            @Param("managerId") Long managerId,
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("""
        SELECT p FROM Project p
        WHERE p.projectStatus = 'ACTIVE'
        AND p.projectId IN (
            SELECT mp.projectId
            FROM ManagerProject mp
            WHERE mp.managerId = :managerId
        )
        ORDER BY p.projectName ASC
        """)
    List<Project> findActiveProjectsByManager(
            @Param("managerId") Long managerId
    );

    boolean existsByProjectCodeAndProjectIdNot(String projectCode, Long projectId);
}
