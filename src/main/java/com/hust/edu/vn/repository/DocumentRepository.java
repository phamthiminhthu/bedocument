package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    ArrayList<Document> findByUserIdAndDocsPublic(Long id, int i);

    Document findFirstByDocumentKeyAndUserAndStatusDelete(String documentKey, User user, Byte statusDelete);

    List<Document> findByUserAndStatusDelete(User user, byte b);
}