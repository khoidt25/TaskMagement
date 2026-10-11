package com.example.tasksmanagement.service;

import com.example.tasksmanagement.entity.Priority;
import com.example.tasksmanagement.repository.PriorityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PriorityService {

    private final PriorityRepository priorityRepository;

    public PriorityService(PriorityRepository priorityRepository) {
        this.priorityRepository = priorityRepository;
    }

    public List<Priority> findAll() {
        return priorityRepository.findAll();
    }

    public Optional<Priority> findById(Integer id) {
        return priorityRepository.findById(id);
    }

    @Transactional
    public Priority create(Priority priority) {
        if (priority.getPriorityId() == null) {
            throw new IllegalArgumentException(
                    "Priority ID không được để trống."
            );
        }

        if (priorityRepository.existsById(priority.getPriorityId())) {
            throw new IllegalArgumentException(
                    "Priority ID đã tồn tại."
            );
        }

        validate(priority);

        if (priorityRepository.existsByPriorityCode(
                priority.getPriorityCode().trim())) {
            throw new IllegalArgumentException(
                    "Priority code đã tồn tại."
            );
        }

        priority.setPriorityCode(priority.getPriorityCode().trim());
        priority.setPriorityName(priority.getPriorityName().trim());

        return priorityRepository.save(priority);
    }

    @Transactional
    public Optional<Priority> update(Integer id, Priority request) {
        return priorityRepository.findById(id).map(existing -> {
            validate(request);

            String newCode = request.getPriorityCode().trim();

            boolean duplicateCode = priorityRepository
                    .findByPriorityCode(newCode)
                    .filter(p -> !p.getPriorityId().equals(id))
                    .isPresent();

            if (duplicateCode) {
                throw new IllegalArgumentException(
                        "Priority code đã được sử dụng."
                );
            }

            existing.setPriorityCode(newCode);
            existing.setPriorityName(
                    request.getPriorityName().trim()
            );

            return priorityRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Integer id) {
        if (!priorityRepository.existsById(id)) {
            return false;
        }

        priorityRepository.deleteById(id);
        return true;
    }

    private void validate(Priority priority) {
        if (priority.getPriorityCode() == null
                || priority.getPriorityCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Priority code không được để trống."
            );
        }

        if (priority.getPriorityName() == null
                || priority.getPriorityName().isBlank()) {
            throw new IllegalArgumentException(
                    "Priority name không được để trống."
            );
        }
    }
}