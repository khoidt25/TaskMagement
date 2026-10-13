
package com.example.tasksmanagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "task_work_logs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_task_work_logs_member_date",
                        columnNames = {"task_member_id", "work_date"}
                )
        }
)
public class TaskWorkLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_log_id")
    private Long workLogId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_member_id", nullable = false)
    private TaskMember taskMember;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "planned_hours", precision = 5, scale = 2)
    private BigDecimal plannedHours;

    @Column(name = "actual_hours", precision = 5, scale = 2)
    private BigDecimal actualHours;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getWorkLogId() {
        return workLogId;
    }

    public void setWorkLogId(Long workLogId) {
        this.workLogId = workLogId;
    }

    public TaskMember getTaskMember() {
        return taskMember;
    }

    public void setTaskMember(TaskMember taskMember) {
        this.taskMember = taskMember;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public BigDecimal getPlannedHours() {
        return plannedHours;
    }

    public void setPlannedHours(BigDecimal plannedHours) {
        this.plannedHours = plannedHours;
    }

    public BigDecimal getActualHours() {
        return actualHours;
    }

    public void setActualHours(BigDecimal actualHours) {
        this.actualHours = actualHours;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
