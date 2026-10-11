
        package com.example.tasksmanagement.service;

import com.example.tasksmanagement.dto.AssignMembersRequest;
import com.example.tasksmanagement.dto.AssignProjectsRequest;
import com.example.tasksmanagement.entity.AppRole;
import com.example.tasksmanagement.entity.ManagerMember;
import com.example.tasksmanagement.entity.ManagerProject;
import com.example.tasksmanagement.entity.User;
import com.example.tasksmanagement.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class ManagerAssignmentService {

    private final AppRoleRepository appRoleRepository;
    private final ManagerProjectRepository managerProjectRepository;
    private final ManagerMemberRepository managerMemberRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public ManagerAssignmentService(
            ManagerProjectRepository managerProjectRepository,
            ManagerMemberRepository managerMemberRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            AppRoleRepository appRoleRepository) {

        this.managerProjectRepository = managerProjectRepository;
        this.managerMemberRepository = managerMemberRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.appRoleRepository = appRoleRepository;
    }

    // =====================================================
    // 1. KIỂM TRA USER VÀ ROLE
    // =====================================================

    private User requireRole(Long userId, String expectedRole) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy User ID: " + userId));

        if (user.getRoleId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tài khoản chưa được gán Role");
        }

        AppRole role = appRoleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Role không tồn tại"));

        String actualRole = role.getRoleCode() == null
                ? ""
                : role.getRoleCode().trim().toUpperCase(Locale.ROOT);

        if (!expectedRole.equalsIgnoreCase(actualRole)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tài khoản ID " + userId
                            + " phải có Role " + expectedRole);
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getUserStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tài khoản ID " + userId
                            + " không ở trạng thái ACTIVE");
        }

        return user;
    }

    // =====================================================
    // 2. ADMIN ASSIGN PROJECT CHO MANAGER
    // =====================================================

    @Transactional
    public void assignProjects(
            AssignProjectsRequest request,
            Long adminId) {

        // Kiểm tra người thực hiện phải là Admin ACTIVE
        requireRole(adminId, "ADMIN");

        // Kiểm tra người nhận phải là Manager ACTIVE
        Long managerId = request.managerId();
        requireRole(managerId, "MANAGER");

        List<Long> projectIds = request.projectIds()
                .stream()
                .distinct()
                .toList();

        for (Long projectId : projectIds) {

            if (!projectRepository.existsById(projectId)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy Project ID: " + projectId);
            }

            // Không tạo bản ghi trùng
            boolean alreadyAssigned =
                    managerProjectRepository
                            .existsByManagerIdAndProjectId(
                                    managerId, projectId);

            if (!alreadyAssigned) {
                ManagerProject assignment = new ManagerProject();

                assignment.setManagerId(managerId);
                assignment.setProjectId(projectId);
                assignment.setAssignedBy(adminId);

                managerProjectRepository.save(assignment);
            }
        }
    }

    // =====================================================
    // 3. ADMIN ASSIGN MEMBER CHO MANAGER
    // =====================================================

    @Transactional
    public void assignMembers(
            AssignMembersRequest request,
            Long adminId) {

        // Kiểm tra người thực hiện phải là Admin ACTIVE
        requireRole(adminId, "ADMIN");

        Long managerId = request.managerId();

        // Kiểm tra người quản lý
        requireRole(managerId, "MANAGER");

        List<Long> memberIds = request.memberIds()
                .stream()
                .distinct()
                .toList();

        for (Long memberId : memberIds) {

            if (managerId.equals(memberId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Manager không thể tự assign chính mình làm Member");
            }

            // Chỉ cho phép tài khoản có Role MEMBER
            requireRole(memberId, "MEMBER");

            boolean alreadyAssigned =
                    managerMemberRepository
                            .existsByManagerIdAndMemberId(
                                    managerId, memberId);

            if (!alreadyAssigned) {
                ManagerMember assignment = new ManagerMember();

                assignment.setManagerId(managerId);
                assignment.setMemberId(memberId);
                assignment.setAssignedBy(adminId);

                managerMemberRepository.save(assignment);
            }
        }
    }

    // =====================================================
    // 4. LẤY DANH SÁCH PROJECT CỦA MANAGER
    // =====================================================

    @Transactional(readOnly = true)
    public List<Long> getProjectIds(Long managerId) {

        requireRole(managerId, "MANAGER");

        return managerProjectRepository.findByManagerId(managerId)
                .stream()
                .map(ManagerProject::getProjectId)
                .toList();
    }

    // =====================================================
    // 5. LẤY DANH SÁCH MEMBER CỦA MANAGER
    // =====================================================

    @Transactional(readOnly = true)
    public List<Long> getMemberIds(Long managerId) {

        requireRole(managerId, "MANAGER");

        return managerMemberRepository.findByManagerId(managerId)
                .stream()
                .map(ManagerMember::getMemberId)
                .toList();
    }

    // =====================================================
    // 6. ADMIN THU HỒI QUYỀN PROJECT
    // =====================================================

    @Transactional
    public void removeProject(Long managerId, Long projectId) {

        managerProjectRepository.deleteByManagerIdAndProjectId(
                managerId, projectId);
    }

    // =====================================================
    // 7. ADMIN THU HỒI QUYỀN MEMBER
    // =====================================================

    @Transactional
    public void removeMember(Long managerId, Long memberId) {

        managerMemberRepository.deleteByManagerIdAndMemberId(
                managerId, memberId);
    }
}
