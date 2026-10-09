package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.dto.TaskMemberResponse;
import com.example.tasksmanagement.entity.TaskMember;
import com.example.tasksmanagement.lib.TaskStatus;
import com.example.tasksmanagement.service.TaskMemberService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.example.tasksmanagement.dto.MyAssignmentUpdateRequest;
import org.springframework.security.core.Authentication;

import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskMemberController {

    private final TaskMemberService taskMemberService;

    public TaskMemberController(TaskMemberService taskMemberService) {
        this.taskMemberService = taskMemberService;
    }

    // 1. Assign một User vào Task
    @PostMapping("/{taskId}/members")
    public ResponseEntity<TaskMemberResponse> assignUser(@PathVariable Long taskId, @RequestBody TaskMember request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(taskMemberService.assignUser(taskId, request));
    }

    // 2. Xem tất cả thành viên của Task
    @GetMapping("/{taskId}/members")
    public List<TaskMemberResponse> getMembers(@PathVariable Long taskId) {

        return taskMemberService.getByTask(taskId);
    }

    // 3. Sửa Role, công việc, Status, Progress, Deadline, Note
    @PutMapping("/{taskId}/members/{memberId}")
    public ResponseEntity<TaskMemberResponse> updateMember(@PathVariable Long taskId, @PathVariable Long memberId, @RequestBody TaskMember request) {

        return taskMemberService.update(taskId, memberId, request).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 4. Xóa thành viên khỏi Task
    @DeleteMapping("/{taskId}/members/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long taskId, @PathVariable Long memberId) {

        if (!taskMemberService.delete(taskId, memberId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    // 5. My Task
    @GetMapping("/my/{userId}")
    public List<TaskMemberResponse> getMyTasks(@PathVariable Long userId) {

        return taskMemberService.getMyTasks(userId);
    }

    // 6. My Task theo trạng thái
    @GetMapping("/my/{userId}/status/{status}")
    public List<TaskMemberResponse> getMyTasksByStatus(@PathVariable Long userId, @PathVariable TaskStatus status) {

        return taskMemberService.getMyTasksByStatus(userId, status);
    }

    @PutMapping("/my-members/{memberId}")
    public ResponseEntity<?> updateMyAssignment(@PathVariable Long memberId, @RequestBody MyAssignmentUpdateRequest request, Authentication authentication) {

        try {
            return ResponseEntity.ok(taskMemberService.updateMyAssignment(authentication.getName(), memberId, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Xử lý lỗi validation nghiệp vụ
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {

        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }
}