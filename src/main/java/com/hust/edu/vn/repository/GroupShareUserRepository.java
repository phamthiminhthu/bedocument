package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.GroupShareUser;
import com.hust.edu.vn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupShareUserRepository extends JpaRepository<GroupShareUser, Long> {
    void deleteByGroupId(Long id);
    List<GroupShareUser> findAllByUser(User user);
    GroupShareUser findByUserAndGroupId(User user, Long groupId);
    boolean existsByGroupIdAndUser(Long groupId, User user);
    List<GroupShareUser> findAllByGroupId(Long groupId);
}