package com.example.tasksmanagement.dto;

public class MyAssignmentUpdateRequest {

    private String status;
    private Integer progress;
    private String assignedWork;
    private String note;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getAssignedWork() {
        return assignedWork;
    }

    public void setAssignedWork(String assignedWork) {
        this.assignedWork = assignedWork;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

