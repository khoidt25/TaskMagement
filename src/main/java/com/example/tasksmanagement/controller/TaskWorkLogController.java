
package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.dto.TaskWorkLogProjection;
import com.example.tasksmanagement.dto.TaskWorkLogRequest;
import com.example.tasksmanagement.dto.TaskWorkLogResponse;
import com.example.tasksmanagement.service.TaskWorkLogService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/task-work-logs")
@Validated
public class TaskWorkLogController {

    private final TaskWorkLogService workLogService;

    public TaskWorkLogController(TaskWorkLogService workLogService) {
        this.workLogService = workLogService;
    }

    @GetMapping
    public ResponseEntity<Page<TaskWorkLogProjection>> getByDate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate workDate,

            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "10")
            @Min(1) @Max(100) int size,

            Authentication authentication) {

        return ResponseEntity.ok(
                workLogService.getByDate(
                        authentication.getName(),
                        workDate,
                        page,
                        size
                )
        );
    }

    @GetMapping("/range")
    public ResponseEntity<Page<TaskWorkLogProjection>> getByDateRange(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "10")
            @Min(1) @Max(100) int size,

            Authentication authentication) {

        return ResponseEntity.ok(
                workLogService.getByDateRange(
                        authentication.getName(),
                        from,
                        to,
                        page,
                        size
                )
        );
    }

    @PostMapping
    public ResponseEntity<TaskWorkLogResponse> save(
            @Valid @RequestBody TaskWorkLogRequest request,
            Authentication authentication) {

        TaskWorkLogResponse response =
                workLogService.save(authentication.getName(), request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/manager/range")
    public ResponseEntity<Page<TaskWorkLogProjection>> getTeamWorkLogs(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "10")
            @Min(1) @Max(100) int size,

            Authentication authentication) {

        return ResponseEntity.ok(
                workLogService.getTeamLogsByDateRange(
                        authentication.getName(),
                        from,
                        to,
                        page,
                        size
                )
        );
    }
}
