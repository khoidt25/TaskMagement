package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.entity.Customer;
import com.example.tasksmanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByUserStatusIgnoreCase(String userStatus);

    List<User> findByRoleIdAndUserStatusIgnoreCase(Long roleId, String userStatus);

//    @Query("""
//            SELECT u
//            FROM Users u
//            WHERE
//                (
//                    :keyword IS NULL OR :keyword = ''
//                    OR LOWER(u.username)
//                        LIKE LOWER(CONCAT('%', :keyword, '%'))
//                )
//                AND (
//                    :status IS NULL OR :status = ''
//                    OR u.userStatus = :status
//                )
//            """)
//    Page<User> searchUser(
//            @Param("keyword") String keyword,
//            @Param("status") String status,
//            Pageable pageable
//    );
}