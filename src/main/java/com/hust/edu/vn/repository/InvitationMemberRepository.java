package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.GroupDoc;
import com.hust.edu.vn.entity.InvitationMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvitationMemberRepository extends JpaRepository<InvitationMember, Long> {
    InvitationMember findByEmailAndGroupId(String email, Long groupId);

    List<InvitationMember> findByGroup(GroupDoc groupDoc);

    boolean existsByEmailAndGroupId(String emailUser, Long groupId);

    List<InvitationMember> findAllByEmail(String email);
}