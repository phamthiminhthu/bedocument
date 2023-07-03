package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    User findByUsername(String username);

    List<User> findByUsernameNot(String username);
}