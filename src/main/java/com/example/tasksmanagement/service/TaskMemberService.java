package com.example.tasksmanagement.service;

import com.example.tasksmanagement.dto.TaskMemberResponse;
import com.example.tasksmanagement.entity.Task;
import com.example.tasksmanagement.entity.TaskMember;
import com.example.tasksmanagement.entity.TaskRole;
import com.example.tasksmanagement.entity.User;
import com.example.tasksmanagement.lib.TaskStatus;
import com.example.tasksmanagement.repository.TaskMemberRepository;
import com.example.tasksmanagement.repository.TaskRoleRepository;
import com.example.tasksmanagement.repository.TaskRepository;
import com.example.tasksmanagement.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.tasksmanagement.dto.MyAssignmentUpdateRequest;
import com.example.tasksmanagement.entity.User;
import com.example.tasksmanagement.lib.TaskStatus;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskMemberService {

    private final TaskMemberRepository memberRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskRoleRepository taskRoleRepository;

    public TaskMemberService(TaskMemberRepository memberRepository, TaskRepository taskRepository, UserRepository userRepository, TaskRoleRepository taskRoleRepository) {
        this.memberRepository = memberRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskRoleRepository = taskRoleRepository;
    }

    @Transactional
    public TaskMemberResponse assignUser(Long taskId, TaskMember request) {
        taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task không tồn tại."));

        userRepository.findById(request.getUserId()).orElseThrow(() -> new IllegalArgumentException("User không tồn tại."));

        if (request.getTaskRoleId() != null) {
            taskRoleRepository.findById(request.getTaskRoleId()).orElseThrow(() -> new IllegalArgumentException("Task Role không tồn tại."));
        }

        if (request.getAssignedBy() != null) {
            userRepository.findById(request.getAssignedBy()).orElseThrow(() -> new IllegalArgumentException("Người phân công không tồn tại."));
        }

        if (memberRepository.existsByTaskIdAndUserId(taskId, request.getUserId())) {
            throw new IllegalArgumentException("User đã được phân công vào Task này.");
        }

        validate(request);

        request.setTaskMemberId(null);
        request.setTaskId(taskId);

        if (request.getStatus() == null) {
            request.setStatus(TaskStatus.NOT_STARTED);
        }
        if (request.getProgress() == null) {
            request.setProgress(0);
        }

        TaskMember saved = memberRepository.save(request);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskMemberResponse> getByTask(Long taskId) {
        return memberRepository.findByTaskId(taskId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskMemberResponse> getMyTasks(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User không tồn tại."));

        return memberRepository.findByUserId(userId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskMemberResponse> getMyTasksByStatus(Long userId, TaskStatus status) {

        return memberRepository.findByUserIdAndStatus(userId, status).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public Optional<TaskMemberResponse> update(Long taskId, Long memberId, TaskMember request) {

        return memberRepository.findById(memberId).filter(member -> member.getTaskId().equals(taskId)).map(member -> {
            if (request.getTaskRoleId() != null) {
                taskRoleRepository.findById(request.getTaskRoleId()).orElseThrow(() -> new IllegalArgumentException("Task Role không tồn tại."));
            }

            validate(request);

            if (request.getTaskRoleId() != null) {
                member.setTaskRoleId(request.getTaskRoleId());
            }
            if (request.getAssignedWork() != null) {
                member.setAssignedWork(request.getAssignedWork());
            }
            if (request.getStatus() != null) {
                member.setStatus(request.getStatus());
            }
            if (request.getProgress() != null) {
                member.setProgress(request.getProgress());
            }
            if (request.getDeadline() != null) {
                member.setDeadline(request.getDeadline());
            }

            // Cho phép xóa note bằng chuỗi rỗng.
            if (request.getNote() != null) {
                member.setNote(request.getNote());
            }

            return toResponse(memberRepository.save(member));
        });
    }

    @Transactional
    public boolean delete(Long taskId, Long memberId) {
        Optional<TaskMember> result = memberRepository.findById(memberId).filter(member -> member.getTaskId().equals(taskId));

        if (result.isEmpty()) {
            return false;
        }

        memberRepository.delete(result.get());
        return true;
    }

    private void validate(TaskMember member) {
        if (member.getAssignedWork() != null && member.getAssignedWork().isBlank()) {
            throw new IllegalArgumentException("assignedWork không được để trống.");
        }

        Integer progress = member.getProgress();
        if (progress != null && (progress < 0 || progress > 100)) {
            throw new IllegalArgumentException("Progress phải nằm trong khoảng 0 đến 100.");
        }
    }

    private TaskMemberResponse toResponse(TaskMember member) {
        TaskMemberResponse response = new TaskMemberResponse();

        response.setTaskMemberId(member.getTaskMemberId());
        response.setTaskId(member.getTaskId());
        response.setUserId(member.getUserId());
        response.setTaskRoleId(member.getTaskRoleId());
        response.setAssignedWork(member.getAssignedWork());
        response.setStatus(member.getStatus());
        response.setProgress(member.getProgress());
        response.setDeadline(member.getDeadline());
        response.setNote(member.getNote());
        response.setAssignedBy(member.getAssignedBy());

        taskRepository.findById(member.getTaskId()).ifPresent(task -> {
            response.setTaskCode(task.getTaskCode());
            response.setTaskName(task.getTaskName());
        });

        userRepository.findById(member.getUserId()).ifPresent(user -> {
            response.setUsername(user.getUsername());
            response.setFullName(user.getFullName());
        });

        if (member.getTaskRoleId() != null) {
            taskRoleRepository.findById(member.getTaskRoleId()).ifPresent(role -> {
                response.setRoleCode(role.getRoleCode());
                response.setRoleName(role.getRoleName());
            });
        }

        return response;
    }

    @Transactional
    public TaskMember updateMyAssignment(String username, Long memberId, MyAssignmentUpdateRequest request) {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));

        TaskMember member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        if (!member.getUserId().equals(user.getUserId())) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot update another user's assignment");
        }

        if (request.getProgress() != null && (request.getProgress() < 0 || request.getProgress() > 100)) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }

        if (request.getStatus() != null) {
            try {
                member.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid task status");
            }
        }

        if (request.getProgress() != null) {
            member.setProgress(request.getProgress());
        }

        if (request.getAssignedWork() != null) {
            member.setAssignedWork(request.getAssignedWork());
        }

        if (request.getNote() != null) {
            member.setNote(request.getNote());
        }

        return memberRepository.save(member);
    }

}