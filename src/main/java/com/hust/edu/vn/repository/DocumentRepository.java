package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    ArrayList<Document> findByUserIdAndDocsPublic(Long id, int i);
}