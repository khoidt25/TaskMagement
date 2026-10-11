package com.example.tasksmanagement.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "manager_projects")
@IdClass(ManagerProject.ManagerProjectId.class)
public class ManagerProject {

    @Id
    @Column(name = "manager_id")
    private Long managerId;

    @Id
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "assigned_by")
    private Long assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @PrePersist
    void prePersist() {
        if (assignedAt == null) {
            assignedAt = OffsetDateTime.now();
        }
    }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getAssignedBy() { return assignedBy; }
    public void setAssignedBy(Long assignedBy) { this.assignedBy = assignedBy; }

    public OffsetDateTime getAssignedAt() { return assignedAt; }

    public static class ManagerProjectId implements Serializable {
        private Long managerId;
        private Long projectId;

        public ManagerProjectId() {}

        public ManagerProjectId(Long managerId, Long projectId) {
            this.managerId = managerId;
            this.projectId = projectId;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ManagerProjectId other)) return false;
            return Objects.equals(managerId, other.managerId)
                    && Objects.equals(projectId, other.projectId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(managerId, projectId);
        }
    }
}