package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.GroupDoc;
import com.hust.edu.vn.entity.TokenInviteGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenInviteGroupRepository extends JpaRepository<TokenInviteGroup, Long> {
    TokenInviteGroup findByEmailAndGroupId(String email, Long groupId);

    List<TokenInviteGroup> findByGroup(GroupDoc groupDoc);

    boolean existsByEmailAndGroupId(String emailUser, Long groupId);
}