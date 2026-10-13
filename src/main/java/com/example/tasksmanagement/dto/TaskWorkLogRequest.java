
package com.example.tasksmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class TaskWorkLogRequest {

    @NotNull(message = "taskMemberId không được để trống")
    private Long taskMemberId;

    @NotNull(message = "workDate không được để trống")
    private LocalDate workDate;

    @DecimalMin(value = "0.00", message = "plannedHours phải >= 0")
    @DecimalMax(value = "24.00", message = "plannedHours phải <= 24")
    private BigDecimal plannedHours;

    @DecimalMin(value = "0.00", message = "actualHours phải >= 0")
    @DecimalMax(value = "24.00", message = "actualHours phải <= 24")
    private BigDecimal actualHours;

    private String note;

    public Long getTaskMemberId() {
        return taskMemberId;
    }

    public void setTaskMemberId(Long taskMemberId) {
        this.taskMemberId = taskMemberId;
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
}
