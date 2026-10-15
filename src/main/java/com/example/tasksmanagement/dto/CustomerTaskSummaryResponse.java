
package com.example.tasksmanagement.dto;

public class CustomerTaskSummaryResponse {

    private Long customerId;
    private String customerName;
    private Long totalTasks;
    private Long inProgressTasks;
    private Long completedTasks;
    private Long overdueTasks;

    public CustomerTaskSummaryResponse(
            Long customerId,
            String customerName,
            Long totalTasks,
            Long inProgressTasks,
            Long completedTasks,
            Long overdueTasks) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.totalTasks = totalTasks;
        this.inProgressTasks = inProgressTasks;
        this.completedTasks = completedTasks;
        this.overdueTasks = overdueTasks;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Long getTotalTasks() {
        return totalTasks;
    }

    public Long getInProgressTasks() {
        return inProgressTasks;
    }

    public Long getCompletedTasks() {
        return completedTasks;
    }

    public Long getOverdueTasks() {
        return overdueTasks;
    }
}
