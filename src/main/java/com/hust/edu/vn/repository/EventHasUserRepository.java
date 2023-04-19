package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.EventHasUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventHasUserRepository extends JpaRepository<EventHasUser, Long> {
}