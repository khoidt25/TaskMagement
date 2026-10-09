package com.example.tasksmanagement.specification;

import com.example.tasksmanagement.entity.Task;
import com.example.tasksmanagement.lib.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class TaskSpecification {

    private TaskSpecification() {
    }

    public static Specification<Task> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + keyword.trim().toLowerCase() + "%";

            return cb.or(cb.like(cb.lower(root.<String>get("taskCode")), pattern), cb.like(cb.lower(root.<String>get("taskName")), pattern), cb.like(cb.lower(root.<String>get("description")), pattern));
        };
    }

    public static Specification<Task> status(TaskStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> projectId(Long projectId) {
        return (root, query, cb) -> projectId == null ? cb.conjunction() : cb.equal(root.get("projectId"), projectId);
    }

    public static Specification<Task> customerId(Long customerId) {
        return (root, query, cb) -> customerId == null ? cb.conjunction() : cb.equal(root.get("customerId"), customerId);
    }

    public static Specification<Task> createdBy(Long userId) {
        return (root, query, cb) -> userId == null ? cb.conjunction() : cb.equal(root.get("createdBy"), userId);
    }

    public static Specification<Task> deadlineFrom(LocalDate date) {
        return (root, query, cb) -> date == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("deadline"), date);
    }

    public static Specification<Task> deadlineTo(LocalDate date) {
        return (root, query, cb) -> date == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("deadline"), date);
    }
}

