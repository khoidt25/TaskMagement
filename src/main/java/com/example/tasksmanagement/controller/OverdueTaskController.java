
package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.dto.OverdueTaskProjection;
import com.example.tasksmanagement.service.OverdueTaskService;
import com.example.tasksmanagement.service.UserAdminService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/overdue-tasks")
public class OverdueTaskController {

    private final OverdueTaskService overdueTaskService;
    private final UserAdminService userAdminService;

    public OverdueTaskController(
            OverdueTaskService overdueTaskService,
            UserAdminService userAdminService) {
        this.overdueTaskService = overdueTaskService;
        this.userAdminService = userAdminService;
    }

    /**
     * Manager xem task quá hạn của các Member mình quản lý.
     */
    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Page<OverdueTaskProjection>>
    getManagerOverdueTasks(
            Authentication authentication,
            Pageable pageable) {

        Long managerId = userAdminService.getUserIdByUsername(
                authentication.getName()
        );

        return ResponseEntity.ok(
                overdueTaskService.getManagerOverdueTasks(
                        managerId,
                        pageable
                )
        );
    }

    /**
     * Member xem task quá hạn của chính mình.
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Page<OverdueTaskProjection>>
    getMyOverdueTasks(
            Authentication authentication,
            Pageable pageable) {

        Long memberId = userAdminService.getUserIdByUsername(
                authentication.getName()
        );

        return ResponseEntity.ok(
                overdueTaskService.getMyOverdueTasks(
                        memberId,
                        pageable
                )
        );
    }
}
