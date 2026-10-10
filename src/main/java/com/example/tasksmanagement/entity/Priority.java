
package com.example.tasksmanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "priorities")
public class Priority {

    @Id
    @Column(name = "priority_id")
    private Integer priorityId;

    @Column(name = "priority_code", nullable = false, length = 30)
    private String priorityCode;

    @Column(name = "priority_name", nullable = false, length = 100)
    private String priorityName;

    public Integer getPriorityId() {
        return priorityId;
    }

    public String getPriorityCode() {
        return priorityCode;
    }

    public String getPriorityName() {
        return priorityName;
    }

    public void setPriorityId(Integer priorityId) {
        this.priorityId = priorityId;
    }

    public void setPriorityCode(String priorityCode) {
        this.priorityCode = priorityCode;
    }

    public void setPriorityName(String priorityName) {
        this.priorityName = priorityName;
    }
}
