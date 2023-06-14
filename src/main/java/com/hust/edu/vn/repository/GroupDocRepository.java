package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.GroupDoc;
import com.hust.edu.vn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupDocRepository extends JpaRepository<GroupDoc, Long> {
    List<GroupDoc> findAllByUser(User user);

    GroupDoc findByIdAndUser(Long groupId, User user);

    boolean existsByIdAndUser(Long groupId, User user);
}