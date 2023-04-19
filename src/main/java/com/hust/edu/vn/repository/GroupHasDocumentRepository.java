package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.GroupHasDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupHasDocumentRepository extends JpaRepository<GroupHasDocument, Long> {
}