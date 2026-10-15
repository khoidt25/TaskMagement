
package com.example.tasksmanagement.dto;

import java.time.LocalDate;

public interface OverdueTaskProjection {

    Long getTaskMemberId();

    Long getTaskId();

    String getTaskCode();

    String getTaskName();

    Long getUserId();

    String getUsername();

    String getFullName();

    String getAssignedWork();

    String getStatus();

    Integer getProgress();

    LocalDate getDeadline();

    String getProjectName();

    String getPriorityName();

    Integer getOverdueDays();
}
