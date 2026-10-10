package com.example.tasksmanagement.entity;

import com.example.tasksmanagement.lib.StatusSource;
import com.example.tasksmanagement.lib.TaskStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "task_code", length = 50, unique = true)
    private String taskCode;

    @Column(name = "task_name", nullable = false, length = 1000)
    private String taskName;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "redmine_issue_id", unique = true)
    private Long redmineIssueId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "priority_id", nullable = false)
    private Integer priorityId = 2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "priority_id",
            referencedColumnName = "priority_id",
            insertable = false,
            updatable = false
    )
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status = TaskStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_source", nullable = false, length = 10)
    private StatusSource statusSource = StatusSource.AUTO;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long v) { taskId = v; }
    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String v) { taskCode = v; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String v) { taskName = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { projectId = v; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long v) { customerId = v; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long v) { createdBy = v; }
    public Long getRedmineIssueId() { return redmineIssueId; }
    public void setRedmineIssueId(Long v) { redmineIssueId = v; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate v) { startDate = v; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate v) { deadline = v; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate v) { releaseDate = v; }
    public Integer getPriorityId() { return priorityId; }
    public void setPriorityId(Integer v) { priorityId = v; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus v) { status = v; }
    public StatusSource getStatusSource() { return statusSource; }
    public void setStatusSource(StatusSource v) { statusSource = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getNote() { return note; }
    public void setNote(String v) { note = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public String getPriorityCode() {
        return priority != null ? priority.getPriorityCode() : null;
    }

    public String getPriorityName() {
        return priority != null ? priority.getPriorityName() : null;
    }
}

