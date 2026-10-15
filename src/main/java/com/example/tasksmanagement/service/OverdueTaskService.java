
package com.example.tasksmanagement.service;

import com.example.tasksmanagement.dto.OverdueTaskProjection;
import com.example.tasksmanagement.repository.OverdueTaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OverdueTaskService {

    private final OverdueTaskRepository overdueTaskRepository;

    public OverdueTaskService(
            OverdueTaskRepository overdueTaskRepository) {
        this.overdueTaskRepository = overdueTaskRepository;
    }

    /**
     * Danh sách task quá hạn của các Member thuộc Manager.
     */
    @Transactional(readOnly = true)
    public Page<OverdueTaskProjection> getManagerOverdueTasks(
            Long managerId,
            Pageable pageable) {

        return overdueTaskRepository.findManagerOverdueTasks(
                managerId,
                pageable
        );
    }

    /**
     * Danh sách task quá hạn của chính Member.
     */
    @Transactional(readOnly = true)
    public Page<OverdueTaskProjection> getMyOverdueTasks(
            Long memberId,
            Pageable pageable) {

        return overdueTaskRepository.findMemberOverdueTasks(
                memberId,
                pageable
        );
    }
}
