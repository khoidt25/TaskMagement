
package com.example.tasksmanagement.service;

import com.example.tasksmanagement.dto.TaskWorkLogRequest;
import com.example.tasksmanagement.dto.TaskWorkLogResponse;
import com.example.tasksmanagement.entity.TaskMember;
import com.example.tasksmanagement.entity.TaskWorkLog;
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

@Service
public class TaskWorkLogService {

    private final TaskWorkLogRepository workLogRepository;
    private final TaskMemberRepository taskMemberRepository;
    private final TaskRepository taskRepository;

    public TaskWorkLogService(
            TaskWorkLogRepository workLogRepository,
            TaskMemberRepository taskMemberRepository, TaskRepository taskRepository) {
        this.workLogRepository = workLogRepository;
        this.taskMemberRepository = taskMemberRepository;
        this.taskRepository = taskRepository;
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

        // Chỉ cho phép lưu nhật ký cho task được giao cho chính mình.
        TaskMember taskMember = taskMemberRepository
                .findAssignedTaskForUser(
                        request.getTaskMemberId(), username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy task được giao hoặc bạn không có quyền truy cập"));

        // Cùng task_member_id và work_date thì cập nhật bản ghi cũ.
        // Nếu chưa tồn tại thì tạo bản ghi mới.
        TaskWorkLog workLog = workLogRepository
                .findByTaskMember_TaskMemberIdAndWorkDate(
                        taskMember.getTaskMemberId(),
                        request.getWorkDate())
                .orElseGet(TaskWorkLog::new);

        workLog.setTaskMember(taskMember);
        workLog.setWorkDate(request.getWorkDate());
        workLog.setPlannedHours(request.getPlannedHours());
        workLog.setActualHours(request.getActualHours());
        workLog.setNote(request.getNote());

        TaskWorkLog saved = workLogRepository.save(workLog);

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
