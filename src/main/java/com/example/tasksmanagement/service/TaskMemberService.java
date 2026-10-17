
package com.example.tasksmanagement.service;

import com.example.tasksmanagement.dto.MyAssignmentUpdateRequest;
import com.example.tasksmanagement.dto.TaskMemberResponse;
import com.example.tasksmanagement.entity.ManagerMember;
import com.example.tasksmanagement.entity.Task;
import com.example.tasksmanagement.entity.TaskMember;
import com.example.tasksmanagement.entity.TaskRole;
import com.example.tasksmanagement.entity.User;
import com.example.tasksmanagement.lib.TaskStatus;
import com.example.tasksmanagement.repository.ManagerMemberRepository;
import com.example.tasksmanagement.repository.TaskMemberRepository;
import com.example.tasksmanagement.repository.TaskRepository;
import com.example.tasksmanagement.repository.TaskRoleRepository;
import com.example.tasksmanagement.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskMemberService {

    private final TaskMemberRepository memberRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskRoleRepository taskRoleRepository;
    private final ManagerMemberRepository managerMemberRepository;
    private final NotificationService notificationService;

    public TaskMemberService(
            TaskMemberRepository memberRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            TaskRoleRepository taskRoleRepository,
            ManagerMemberRepository managerMemberRepository,
            NotificationService notificationService) {

        this.memberRepository = memberRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskRoleRepository = taskRoleRepository;
        this.managerMemberRepository = managerMemberRepository;
        this.notificationService = notificationService;
    }

    // =====================================================
    // 1. PHÂN CÔNG USER VÀO TASK
    // =====================================================

    @Transactional
    public TaskMemberResponse assignUser(Long taskId, TaskMember request) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Task không tồn tại."));

        User assignedUser = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException("User không tồn tại."));

        if (request.getTaskRoleId() != null) {
            taskRoleRepository.findById(request.getTaskRoleId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Task Role không tồn tại."));
        }

        if (request.getAssignedBy() != null) {
            userRepository.findById(request.getAssignedBy())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Người phân công không tồn tại."));
        }

        if (memberRepository.existsByTaskIdAndUserId(
                taskId, request.getUserId())) {
            throw new IllegalArgumentException(
                    "User đã được phân công vào Task này.");
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

        // Thông báo cho Member vừa được phân công.
        notificationService.createNotification(
                assignedUser.getUserId(),
                task.getTaskId(),
                "TASK_ASSIGNED",
                "Bạn được giao công việc mới",
                "Bạn vừa được phân công vào task: "
                        + task.getTaskName()
        );

        // Đồng bộ trạng thái Task tổng thể.
        syncTaskStatus(taskId);

        return toResponse(saved);
    }

    // =====================================================
    // 2. LẤY DANH SÁCH MEMBER CỦA TASK
    // =====================================================

    @Transactional(readOnly = true)
    public List<TaskMemberResponse> getByTask(Long taskId) {

        return memberRepository.findByTaskId(taskId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // 3. LẤY TASK CỦA USER
    // =====================================================

    @Transactional(readOnly = true)
    public List<TaskMemberResponse> getMyTasks(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User không tồn tại."));

        return memberRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // 4. LẤY TASK CỦA USER THEO TRẠNG THÁI
    // =====================================================

    @Transactional(readOnly = true)
    public List<TaskMemberResponse> getMyTasksByStatus(
            Long userId,
            TaskStatus status) {

        return memberRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // 5. CẬP NHẬT PHÂN CÔNG TASK
    // =====================================================

    @Transactional
    public Optional<TaskMemberResponse> update(
            Long taskId,
            Long memberId,
            TaskMember request) {

        return memberRepository.findById(memberId)
                .filter(member -> member.getTaskId().equals(taskId))
                .map(member -> {

                    if (request.getTaskRoleId() != null) {
                        taskRoleRepository.findById(request.getTaskRoleId())
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Task Role không tồn tại."));
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

                    TaskMember saved = memberRepository.save(member);

                    // Thông báo cho Member về việc cập nhật phân công.
                    Task task = taskRepository.findById(taskId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Task không tồn tại."));

                    User assignedUser = userRepository
                            .findById(saved.getUserId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "User không tồn tại."));

                    notificationService.createNotification(
                            assignedUser.getUserId(),
                            task.getTaskId(),
                            "TASK_UPDATED",
                            "Phân công công việc đã được cập nhật",
                            "Thông tin phân công của bạn trong task "
                                    + task.getTaskName()
                                    + " vừa được cập nhật."
                    );

                    // Cập nhật trạng thái Task theo các thành viên.
                    syncTaskStatus(taskId);

                    return toResponse(saved);
                });
    }

    // =====================================================
    // 6. XÓA PHÂN CÔNG MEMBER KHỎI TASK
    // =====================================================

    @Transactional
    public boolean delete(Long taskId, Long memberId) {

        Optional<TaskMember> result = memberRepository.findById(memberId)
                .filter(member -> member.getTaskId().equals(taskId));

        if (result.isEmpty()) {
            return false;
        }

        TaskMember member = result.get();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Task không tồn tại."));

        // Thông báo trước khi xóa dữ liệu phân công.
        notificationService.createNotification(
                member.getUserId(),
                task.getTaskId(),
                "TASK_UNASSIGNED",
                "Bạn đã được gỡ khỏi công việc",
                "Bạn đã được gỡ khỏi task: " + task.getTaskName()
        );

        memberRepository.delete(member);

        // Tính lại trạng thái theo những thành viên còn lại.
        syncTaskStatus(taskId);

        return true;
    }

    // =====================================================
    // 7. VALIDATE PHÂN CÔNG
    // =====================================================

    private void validate(TaskMember member) {

        if (member.getAssignedWork() != null
                && member.getAssignedWork().isBlank()) {
            throw new IllegalArgumentException(
                    "assignedWork không được để trống.");
        }

        Integer progress = member.getProgress();

        if (progress != null && (progress < 0 || progress > 100)) {
            throw new IllegalArgumentException(
                    "Progress phải nằm trong khoảng 0 đến 100.");
        }
    }

    // =====================================================
    // 8. CHUYỂN TASK MEMBER SANG RESPONSE DTO
    // =====================================================

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
            taskRoleRepository.findById(member.getTaskRoleId())
                    .ifPresent(role -> {
                        response.setRoleCode(role.getRoleCode());
                        response.setRoleName(role.getRoleName());
                    });
        }

        return response;
    }

    // =====================================================
    // 9. MEMBER CẬP NHẬT CÔNG VIỆC CỦA CHÍNH MÌNH
    // =====================================================

    @Transactional
    public TaskMember updateMyAssignment(
            String username,
            Long memberId,
            MyAssignmentUpdateRequest request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        TaskMember member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Assignment not found"));

        // Không cho phép Member sửa phân công của người khác.
        if (!member.getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException(
                    "You cannot update another user's assignment");
        }

        Integer oldProgress = member.getProgress();
        TaskStatus oldStatus = member.getStatus();

        Task task = taskRepository.findById(member.getTaskId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Task không tồn tại."));

        if (request.getProgress() != null
                && (request.getProgress() < 0
                || request.getProgress() > 100)) {
            throw new IllegalArgumentException(
                    "Progress must be between 0 and 100");
        }

        if (request.getStatus() != null) {
            try {
                member.setStatus(TaskStatus.valueOf(
                        request.getStatus().trim().toUpperCase(Locale.ROOT)));
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

        TaskMember saved = memberRepository.save(member);

        boolean progressChanged =
                !Objects.equals(oldProgress, saved.getProgress());

        boolean statusChanged =
                !Objects.equals(oldStatus, saved.getStatus());

        // Chỉ thông báo nếu tiến độ hoặc trạng thái thực sự thay đổi.
        if (progressChanged || statusChanged) {

            List<Long> managerIds = managerMemberRepository
                    .findByMemberId(user.getUserId())
                    .stream()
                    .map(ManagerMember::getManagerId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            String message = "Thành viên "
                    + user.getFullName()
                    + " đã cập nhật task "
                    + task.getTaskName()
                    + ". Tiến độ: "
                    + saved.getProgress()
                    + "%, trạng thái: "
                    + saved.getStatus()
                    + ".";

            for (Long managerId : managerIds) {
                notificationService.createNotification(
                        managerId,
                        task.getTaskId(),
                        "TASK_PROGRESS_UPDATED",
                        "Thành viên cập nhật công việc",
                        message
                );
            }
        }

        // Đồng bộ Task sau khi Member cập nhật.
        syncTaskStatus(member.getTaskId());

        return saved;
    }

    // =====================================================
    // 10. PHÂN TRANG TASK CỦA USER
    // =====================================================

    @Transactional(readOnly = true)
    public Page<TaskMemberResponse> getMyTasksPage(
            Long userId,
            Pageable pageable) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User không tồn tại."));

        return memberRepository.findMyTasksWithDetails(userId, pageable)
                .map(this::toResponseFromRow);
    }

    // =====================================================
    // 11. PHÂN TRANG TASK THEO TRẠNG THÁI
    // =====================================================

    @Transactional(readOnly = true)
    public Page<TaskMemberResponse> getMyTasksByStatusPage(
            Long userId,
            TaskStatus status,
            Pageable pageable) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User không tồn tại."));

        return memberRepository
                .findMyTasksWithDetailsByStatus(userId, status, pageable)
                .map(this::toResponseFromRow);
    }

    // =====================================================
    // 12. ÁNH XẠ DỮ LIỆU TỪ TRUY VẤN JOIN
    // =====================================================

    private TaskMemberResponse toResponseFromRow(Object[] row) {

        TaskMember member = (TaskMember) row[0];
        Task task = (Task) row[1];
        User user = (User) row[2];
        TaskRole role = (TaskRole) row[3];

        return toResponse(member, task, user, role);
    }

    private TaskMemberResponse toResponse(
            TaskMember member,
            Task task,
            User user,
            TaskRole role) {

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

        if (task != null) {
            response.setTaskCode(task.getTaskCode());
            response.setTaskName(task.getTaskName());
        }

        if (user != null) {
            response.setUsername(user.getUsername());
            response.setFullName(user.getFullName());
        }

        if (role != null) {
            response.setRoleCode(role.getRoleCode());
            response.setRoleName(role.getRoleName());
        }

        return response;
    }

    // =====================================================
    // 13. ĐỒNG BỘ TRẠNG THÁI TASK TỔNG THỂ
    // =====================================================

    private void syncTaskStatus(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Task không tồn tại."));

        // Không tự động thay đổi Task đã bị hủy.
        if (task.getStatus() == TaskStatus.CANCELLED) {
            return;
        }

        List<TaskMember> members = memberRepository.findByTaskId(taskId);

        // Chưa có thành viên: giữ trạng thái chưa bắt đầu.
        if (members.isEmpty()) {
            task.setStatus(TaskStatus.NOT_STARTED);
            taskRepository.save(task);
            return;
        }

        boolean allCompleted = members.stream()
                .allMatch(member ->
                        member.getStatus() == TaskStatus.COMPLETED);

        boolean hasInProgress = members.stream()
                .anyMatch(member ->
                        member.getStatus() == TaskStatus.IN_PROGRESS);

        boolean hasCompleted = members.stream()
                .anyMatch(member ->
                        member.getStatus() == TaskStatus.COMPLETED);

        boolean hasPending = members.stream()
                .anyMatch(member ->
                        member.getStatus() == TaskStatus.PENDING);

        if (allCompleted) {
            task.setStatus(TaskStatus.COMPLETED);
        } else if (hasInProgress || hasCompleted) {
            // Có người đang làm hoặc đã hoàn thành một phần công việc.
            task.setStatus(TaskStatus.IN_PROGRESS);
        } else if (hasPending) {
            task.setStatus(TaskStatus.PENDING);
        } else {
            task.setStatus(TaskStatus.NOT_STARTED);
        }

        taskRepository.save(task);
    }
}
