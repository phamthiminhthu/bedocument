package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.LikeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeDocumentRepository extends JpaRepository<LikeDocument, Long> {
}