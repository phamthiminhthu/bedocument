package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Document findByDocumentKeyAndUserAndStatusDelete(String documentKey, User user, Byte statusDelete);

    List<Document> findByUserAndStatusDelete(User user, byte b);

    List<Document> findAllByUserIdAndStatusDelete(Long id, byte b);

    Document findByDocumentKeyAndStatusDelete(String documentKey, byte b);

    List<Document> findAllByUserAndLoved(User user, byte b);

    Document findByIdAndUserAndStatusDelete(Long documentId, User user, byte b);
}