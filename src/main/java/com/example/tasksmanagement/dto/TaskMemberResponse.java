package com.example.tasksmanagement.dto;

import com.example.tasksmanagement.lib.TaskStatus;

import java.time.LocalDate;

public class TaskMemberResponse {

    private Long taskMemberId;
    private Long taskId;
    private String taskCode;
    private String taskName;

    private Long userId;
    private String username;
    private String fullName;

    private Long taskRoleId;
    private String roleCode;
    private String roleName;

    private String assignedWork;
    private TaskStatus status;
    private Integer progress;
    private LocalDate deadline;
    private String note;
    private Long assignedBy;

    public Long getTaskMemberId() {
        return taskMemberId;
    }

    public void setTaskMemberId(Long value) {
        taskMemberId = value;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long value) {
        taskId = value;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String value) {
        taskCode = value;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String value) {
        taskName = value;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long value) {
        userId = value;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String value) {
        username = value;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String value) {
        fullName = value;
    }

    public Long getTaskRoleId() {
        return taskRoleId;
    }

    public void setTaskRoleId(Long value) {
        taskRoleId = value;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String value) {
        roleCode = value;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String value) {
        roleName = value;
    }

    public String getAssignedWork() {
        return assignedWork;
    }

    public void setAssignedWork(String value) {
        assignedWork = value;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus value) {
        status = value;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer value) {
        progress = value;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate value) {
        deadline = value;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String value) {
        note = value;
    }

    public Long getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Long value) {
        assignedBy = value;
    }
}