package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.entity.ManagerMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManagerMemberRepository
        extends JpaRepository<ManagerMember, ManagerMember.ManagerMemberId> {

    List<ManagerMember> findByManagerId(Long managerId);

    List<ManagerMember> findByMemberId(Long memberId);

    boolean existsByManagerIdAndMemberId(
            Long managerId, Long memberId);

    void deleteByManagerIdAndMemberId(
            Long managerId, Long memberId);

    void deleteByManagerId(Long managerId);

}