package com.example.tasksmanagement.dto;

public record ProjectOptionResponse(
        Long projectId,
        String projectCode,
        String projectName
) {
}
