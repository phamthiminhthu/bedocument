package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.GroupDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupDocRepository extends JpaRepository<GroupDoc, Long> {
}