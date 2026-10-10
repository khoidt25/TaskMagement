package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.entity.TaskMember;
import com.example.tasksmanagement.lib.TaskStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskMemberRepository extends JpaRepository<TaskMember, Long> {

    List<TaskMember> findByTaskId(Long taskId);

    List<TaskMember> findByUserId(Long userId);

    List<TaskMember> findByUserIdAndStatus(Long userId, TaskStatus status);

    Optional<TaskMember> findByTaskIdAndUserId(Long taskId, Long userId);

    boolean existsByTaskIdAndUserId(Long taskId, Long userId);
}