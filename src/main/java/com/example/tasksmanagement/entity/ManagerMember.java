package com.example.tasksmanagement.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "manager_members")
@IdClass(ManagerMember.ManagerMemberId.class)
public class ManagerMember {

    @Id
    @Column(name = "manager_id")
    private Long managerId;

    @Id
    @Column(name = "member_id")
    private Long memberId;

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

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public Long getAssignedBy() { return assignedBy; }
    public void setAssignedBy(Long assignedBy) { this.assignedBy = assignedBy; }

    public OffsetDateTime getAssignedAt() { return assignedAt; }

    public static class ManagerMemberId implements Serializable {
        private Long managerId;
        private Long memberId;

        public ManagerMemberId() {}

        public ManagerMemberId(Long managerId, Long memberId) {
            this.managerId = managerId;
            this.memberId = memberId;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ManagerMemberId other)) return false;
            return Objects.equals(managerId, other.managerId)
                    && Objects.equals(memberId, other.memberId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(managerId, memberId);
        }
    }
}