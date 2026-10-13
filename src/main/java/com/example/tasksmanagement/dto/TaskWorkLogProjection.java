package com.example.tasksmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface TaskWorkLogProjection {

    Long getWorkLogId();

    Long getTaskMemberId();

    Long getTaskId();

    String getTaskCode();

    String getTaskName();

    LocalDate getWorkDate();

    BigDecimal getPlannedHours();

    BigDecimal getActualHours();

    String getNote();

    OffsetDateTime getCreatedAt();

    OffsetDateTime getUpdatedAt();
}
