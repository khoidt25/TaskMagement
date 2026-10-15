
package com.example.tasksmanagement.dto;

import java.util.List;

public class ProjectMemberTaskSummaryResponse {

    private Long managerId;
    private List<ProjectSummary> projects;

    public ProjectMemberTaskSummaryResponse() {
    }

    public ProjectMemberTaskSummaryResponse(
            Long managerId,
            List<ProjectSummary> projects) {
        this.managerId = managerId;
        this.projects = projects;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public List<ProjectSummary> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectSummary> projects) {
        this.projects = projects;
    }

    public static class ProjectSummary {

        private Long projectId;
        private long totalTasks;
        private List<MemberSummary> members;

        public ProjectSummary() {
        }

        public ProjectSummary(
                Long projectId,
                long totalTasks,
                List<MemberSummary> members) {
            this.projectId = projectId;
            this.totalTasks = totalTasks;
            this.members = members;
        }

        public Long getProjectId() {
            return projectId;
        }

        public void setProjectId(Long projectId) {
            this.projectId = projectId;
        }

        public long getTotalTasks() {
            return totalTasks;
        }

        public void setTotalTasks(long totalTasks) {
            this.totalTasks = totalTasks;
        }

        public List<MemberSummary> getMembers() {
            return members;
        }

        public void setMembers(List<MemberSummary> members) {
            this.members = members;
        }
    }

    public static class MemberSummary {

        private Long memberId;
        private long totalTasks;

        public MemberSummary() {
        }

        public MemberSummary(Long memberId, long totalTasks) {
            this.memberId = memberId;
            this.totalTasks = totalTasks;
        }

        public Long getMemberId() {
            return memberId;
        }

        public void setMemberId(Long memberId) {
            this.memberId = memberId;
        }

        public long getTotalTasks() {
            return totalTasks;
        }

        public void setTotalTasks(long totalTasks) {
            this.totalTasks = totalTasks;
        }
    }
}

