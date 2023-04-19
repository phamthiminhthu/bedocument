package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.CollectionHasDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionHasDocumentRepository extends JpaRepository<CollectionHasDocument, Long> {
}