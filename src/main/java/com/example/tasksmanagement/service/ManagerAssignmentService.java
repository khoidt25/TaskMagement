
package com.example.tasksmanagement.service;

import com.example.tasksmanagement.dto.AssignMembersRequest;
import com.example.tasksmanagement.dto.AssignProjectsRequest;
import com.example.tasksmanagement.dto.MemberOptionResponse;
import com.example.tasksmanagement.entity.*;
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
    private final NotificationService notificationService;

    public ManagerAssignmentService(
            ManagerProjectRepository managerProjectRepository,
            ManagerMemberRepository managerMemberRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            AppRoleRepository appRoleRepository,
            NotificationService notificationService) {

        this.managerProjectRepository = managerProjectRepository;
        this.managerMemberRepository = managerMemberRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.appRoleRepository = appRoleRepository;
        this.notificationService = notificationService;
    }

    // 1. Kiểm tra tài khoản và quyền
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
                    HttpStatus.FORBIDDEN,
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

    // 2. Admin giao Project cho Manager
    // Admin giao Project cho Manager
    @Transactional
    public void assignProjects(
            AssignProjectsRequest request,
            Long adminId) {

        requireRole(adminId, "ADMIN");

        Long managerId = request.managerId();
        User manager = requireRole(managerId, "MANAGER");

        List<Long> projectIds = request.projectIds()
                .stream()
                .distinct()
                .toList();

        for (Long projectId : projectIds) {

            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Không tìm thấy Project ID: " + projectId
                    ));

            boolean alreadyAssigned =
                    managerProjectRepository
                            .existsByManagerIdAndProjectId(
                                    managerId, projectId);

            if (alreadyAssigned) {
                continue;
            }

            ManagerProject assignment = new ManagerProject();
            assignment.setManagerId(managerId);
            assignment.setProjectId(projectId);
            assignment.setAssignedBy(adminId);

            managerProjectRepository.save(assignment);

            notificationService.createNotification(
                    manager.getUserId(),
                    null,
                    "PROJECT_ASSIGNED",
                    "Được giao Project mới",
                    "Bạn vừa được giao quản lý Project: "
                            + project.getProjectName()
            );
        }

    }

    // 3. Admin giao Member cho Manager
    @Transactional
    public void assignMembers(
            AssignMembersRequest request,
            Long adminId) {

        requireRole(adminId, "ADMIN");

        Long managerId = request.managerId();
        User manager = requireRole(managerId, "MANAGER");

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

            User member = requireRole(memberId, "MEMBER");

            boolean alreadyAssigned =
                    managerMemberRepository
                            .existsByManagerIdAndMemberId(
                                    managerId, memberId);

            if (alreadyAssigned) {
                continue;
            }

            ManagerMember assignment = new ManagerMember();
            assignment.setManagerId(managerId);
            assignment.setMemberId(memberId);
            assignment.setAssignedBy(adminId);

            managerMemberRepository.save(assignment);

            notificationService.createNotification(
                    manager.getUserId(),
                    null,
                    "MANAGER_MEMBER_ASSIGNED",
                    "Thêm Member vào nhóm quản lý",
                    "Bạn vừa được phân công quản lý Member: "
                            + member.getFullName()
            );

            notificationService.createNotification(
                    member.getUserId(),
                    null,
                    "MANAGER_ASSIGNED",
                    "Bạn có Manager mới",
                    "Manager " + manager.getFullName()
                            + " đã được phân công quản lý bạn."
            );
        }
    }

    // 4. Lấy Project IDs của Manager
    @Transactional(readOnly = true)
    public List<Long> getProjectIds(Long managerId) {

        requireRole(managerId, "MANAGER");

        return managerProjectRepository.findByManagerId(managerId)
                .stream()
                .map(ManagerProject::getProjectId)
                .toList();
    }

    // 5. Lấy Member IDs của Manager
    @Transactional(readOnly = true)
    public List<Long> getMemberIds(Long managerId) {

        requireRole(managerId, "MANAGER");

        return managerMemberRepository.findByManagerId(managerId)
                .stream()
                .map(ManagerMember::getMemberId)
                .toList();
    }

    // 6. Thu hồi Project của Manager
    @Transactional
    public void removeProject(Long managerId, Long projectId) {

        User manager = requireRole(managerId, "MANAGER");

        boolean assigned =
                managerProjectRepository
                        .existsByManagerIdAndProjectId(managerId, projectId);

        if (!assigned) {
            return;
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy Project ID: " + projectId
                ));

        String projectName = project.getProjectName();

        managerProjectRepository.deleteByManagerIdAndProjectId(
                managerId, projectId
        );

        notificationService.createNotification(
                manager.getUserId(),
                null,
                "PROJECT_UNASSIGNED",
                "Project đã bị thu hồi",
                "Quyền quản lý Project: " + projectName
                        + " đã được thu hồi."
        );
    }

    // 7. Gỡ Member khỏi Manager
    @Transactional
    public void removeMember(Long managerId, Long memberId) {

        boolean assigned =
                managerMemberRepository
                        .existsByManagerIdAndMemberId(
                                managerId, memberId);

        if (!assigned) {
            return;
        }

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy Manager ID: " + managerId));

        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy Member ID: " + memberId));

        managerMemberRepository.deleteByManagerIdAndMemberId(
                managerId, memberId);

        notificationService.createNotification(
                manager.getUserId(),
                null,
                "MANAGER_MEMBER_UNASSIGNED",
                "Member đã được gỡ khỏi nhóm quản lý",
                "Member " + member.getFullName()
                        + " không còn thuộc nhóm quản lý của bạn."
        );

        notificationService.createNotification(
                member.getUserId(),
                null,
                "MANAGER_UNASSIGNED",
                "Quan hệ quản lý đã thay đổi",
                "Bạn không còn thuộc nhóm quản lý của Manager "
                        + manager.getFullName() + "."
        );
    }

    // 8. Lấy danh sách Member options
    @Transactional(readOnly = true)
    public List<MemberOptionResponse> getMembersOption(Long managerId) {

        requireRole(managerId, "MANAGER");

        return managerMemberRepository.findByManagerId(managerId)
                .stream()
                .map(managerMember -> {
                    Long memberId = managerMember.getMemberId();

                    User member = userRepository.findById(memberId)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Không tìm thấy Member ID: " + memberId));

                    return new MemberOptionResponse(
                            member.getUserId(),
                            member.getFullName()
                    );
                })
                .toList();
    }
}
