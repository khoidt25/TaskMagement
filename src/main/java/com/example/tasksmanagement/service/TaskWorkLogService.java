
package com.example.tasksmanagement.service;

import com.example.tasksmanagement.dto.TaskWorkLogRequest;
import com.example.tasksmanagement.dto.TaskWorkLogResponse;
import com.example.tasksmanagement.entity.*;
import com.example.tasksmanagement.repository.TaskMemberRepository;
import com.example.tasksmanagement.repository.TaskRepository;
import com.example.tasksmanagement.repository.TaskWorkLogRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import com.example.tasksmanagement.dto.TaskWorkLogProjection;
import com.example.tasksmanagement.repository.ManagerMemberRepository;
import com.example.tasksmanagement.repository.UserRepository;

@Service
public class TaskWorkLogService {

    private final TaskWorkLogRepository workLogRepository;
    private final TaskMemberRepository taskMemberRepository;
    private final TaskRepository taskRepository;

    private final NotificationService notificationService;
    private final ManagerMemberRepository managerMemberRepository;
    private final UserRepository userRepository;

    public TaskWorkLogService(
            TaskWorkLogRepository workLogRepository,
            TaskMemberRepository taskMemberRepository,
            TaskRepository taskRepository,
            NotificationService notificationService,
            ManagerMemberRepository managerMemberRepository,
            UserRepository userRepository) {
        this.workLogRepository = workLogRepository;
        this.taskMemberRepository = taskMemberRepository;
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
        this.managerMemberRepository = managerMemberRepository;
        this.userRepository = userRepository;

    }

    @Transactional(readOnly = true)
    public Page<TaskWorkLogProjection> getByDate(
            String username,
            LocalDate workDate,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        return workLogRepository.findLogsByDate(
                username,
                workDate,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<TaskWorkLogProjection> getByDateRange(
            String username,
            LocalDate from,
            LocalDate to,
            int page,
            int size) {

        if (from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ngày bắt đầu không được lớn hơn ngày kết thúc"
            );
        }

        Pageable pageable = PageRequest.of(page, size);

        return workLogRepository.findLogsByDateRange(
                username,
                from,
                to,
                pageable
        );
    }

    @Transactional
    public TaskWorkLogResponse save(
            String username,
            TaskWorkLogRequest request) {

        TaskMember taskMember = taskMemberRepository
                .findAssignedTaskForUser(
                        request.getTaskMemberId(), username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy task được giao hoặc bạn không có quyền truy cập"));

        var existing = workLogRepository
                .findByTaskMember_TaskMemberIdAndWorkDate(
                        taskMember.getTaskMemberId(),
                        request.getWorkDate());

        boolean isNew = existing.isEmpty();

        TaskWorkLog workLog = existing.orElseGet(TaskWorkLog::new);

        boolean changed = isNew
                || !java.util.Objects.equals(
                workLog.getPlannedHours(), request.getPlannedHours())
                || !java.util.Objects.equals(
                workLog.getActualHours(), request.getActualHours())
                || !java.util.Objects.equals(
                workLog.getNote(), request.getNote());

        workLog.setTaskMember(taskMember);
        workLog.setWorkDate(request.getWorkDate());
        workLog.setPlannedHours(request.getPlannedHours());
        workLog.setActualHours(request.getActualHours());
        workLog.setNote(request.getNote());

        TaskWorkLog saved = workLogRepository.save(workLog);

        if (changed) {
            String memberName = userRepository
                    .findByUsername(username)
                    .map(User::getFullName)
                    .filter(name -> name != null && !name.isBlank())
                    .orElse(username);

            String taskName = "Task ID " + taskMember.getTaskId();

            if (taskMember.getTaskId() != null) {
                taskName = taskRepository.findById(taskMember.getTaskId())
                        .map(Task::getTaskName)
                        .filter(name -> name != null && !name.isBlank())
                        .orElse(taskName);
            }

            String title = isNew
                    ? "Member ghi nhận công việc"
                    : "Member cập nhật nhật ký công việc";

            String message = memberName
                    + (isNew ? " đã ghi nhận công việc cho task \""
                    : " đã cập nhật nhật ký công việc cho task \"")
                    + taskName
                    + "\" ngày " + request.getWorkDate() + ".";

            List<ManagerMember> managers =
                    managerMemberRepository.findByMemberId(
                            taskMember.getUserId());

            for (ManagerMember manager : managers) {
                notificationService.createNotification(
                        manager.getManagerId(),
                        taskMember.getTaskId(),
                        "WORK_LOG_UPDATED",
                        title,
                        message
                );
            }
        }

        return toResponse(saved);
    }


    private TaskWorkLogResponse toResponse(TaskWorkLog log) {
        TaskWorkLogResponse response = new TaskWorkLogResponse();

        response.setWorkLogId(log.getWorkLogId());
        response.setTaskMemberId(
                log.getTaskMember().getTaskMemberId());

        response.setWorkDate(log.getWorkDate());
        response.setPlannedHours(log.getPlannedHours());
        response.setActualHours(log.getActualHours());
        response.setNote(log.getNote());
        response.setCreatedAt(log.getCreatedAt());
        response.setUpdatedAt(log.getUpdatedAt());

        // TaskMember đang lưu taskId dưới dạng Long.
        Long taskId = log.getTaskMember().getTaskId();
        response.setTaskId(taskId);

        if (taskId != null) {
            taskRepository.findById(taskId).ifPresent(task -> {
                response.setTaskCode(task.getTaskCode());
                response.setTaskName(task.getTaskName());
            });
        }

        return response;
    }

}
