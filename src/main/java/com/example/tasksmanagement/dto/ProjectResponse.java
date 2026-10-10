package com.example.tasksmanagement.dto;

public class ProjectResponse {

    private Long projectId;
    private String projectName;

    public ProjectResponse() {
    }

    public ProjectResponse(Long projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}