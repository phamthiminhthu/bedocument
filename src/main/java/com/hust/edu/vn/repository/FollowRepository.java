package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Follow;
import com.hust.edu.vn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowingIdAndFollower(Long id, User user);

    Follow findByFollowingIdAndFollower(Long id, User user);

    List<Follow> findByFollowingId(Long id);

    List<Follow> findByFollower(User user);
}