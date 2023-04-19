package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.EventHasDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventHasDocumentRepository extends JpaRepository<EventHasDocument, Long> {
}