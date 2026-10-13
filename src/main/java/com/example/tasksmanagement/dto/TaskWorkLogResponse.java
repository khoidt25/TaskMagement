
package com.example.tasksmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class TaskWorkLogResponse {

    private Long workLogId;
    private Long taskMemberId;
    private Long taskId;
    private String taskCode;
    private String taskName;
    private LocalDate workDate;
    private BigDecimal plannedHours;
    private BigDecimal actualHours;
    private String note;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Long getWorkLogId() { return workLogId; }
    public void setWorkLogId(Long workLogId) { this.workLogId = workLogId; }

    public Long getTaskMemberId() { return taskMemberId; }
    public void setTaskMemberId(Long taskMemberId) { this.taskMemberId = taskMemberId; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String taskCode) { this.taskCode = taskCode; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public BigDecimal getPlannedHours() { return plannedHours; }
    public void setPlannedHours(BigDecimal plannedHours) { this.plannedHours = plannedHours; }

    public BigDecimal getActualHours() { return actualHours; }
    public void setActualHours(BigDecimal actualHours) { this.actualHours = actualHours; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
