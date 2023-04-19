package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.FileDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileDocumentRepository extends JpaRepository<FileDocument, Long>{
}