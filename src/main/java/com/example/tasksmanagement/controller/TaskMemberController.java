package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.dto.TaskMemberResponse;
import com.example.tasksmanagement.entity.TaskMember;
import com.example.tasksmanagement.lib.TaskStatus;
import com.example.tasksmanagement.service.TaskMemberService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    @PostMapping("/{taskId}/members")
    public ResponseEntity<TaskMemberResponse> assignUser(@PathVariable Long taskId, @RequestBody TaskMember request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(taskMemberService.assignUser(taskId, request));
    }

    @GetMapping("/{taskId}/members")
    public List<TaskMemberResponse> getMembers(@PathVariable Long taskId) {

        return taskMemberService.getByTask(taskId);
    }

    @PutMapping("/{taskId}/members/{memberId}")
    public ResponseEntity<TaskMemberResponse> updateMember(@PathVariable Long taskId, @PathVariable Long memberId, @RequestBody TaskMember request) {

        return taskMemberService.update(taskId, memberId, request).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{taskId}/members/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long taskId, @PathVariable Long memberId) {

        if (!taskMemberService.delete(taskId, memberId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my/{userId}")
    public List<TaskMemberResponse> getMyTasks(@PathVariable Long userId) {

        return taskMemberService.getMyTasks(userId);
    }

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {

        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    /**
     * Lấy danh sách task được giao cho user có phân trang.
     *
     * GET /api/tasks/my/{userId}/page?page=0&size=10&sort=taskMemberId,desc
     */
    @GetMapping("/my/{userId}/page")
    public ResponseEntity<Page<TaskMemberResponse>> getMyTasksPage(
            @PathVariable Long userId,
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "taskMemberId",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return ResponseEntity.ok(
                taskMemberService.getMyTasksPage(userId, pageable)
        );
    }

    /**
     * Lấy danh sách task theo trạng thái có phân trang.
     *
     * GET /api/tasks/my/{userId}/status/{status}/page?page=0&size=10
     */
    @GetMapping("/my/{userId}/status/{status}/page")
    public ResponseEntity<Page<TaskMemberResponse>> getMyTasksByStatusPage(
            @PathVariable Long userId,
            @PathVariable TaskStatus status,
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "taskMemberId",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return ResponseEntity.ok(
                taskMemberService.getMyTasksByStatusPage(
                        userId,
                        status,
                        pageable
                )
        );
    }

}