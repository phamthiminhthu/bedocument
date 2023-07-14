package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Document findByDocumentKeyAndUserAndStatusDelete(String documentKey, User user, Byte statusDelete);

    Document findByDocumentKeyAndStatusDelete(String documentKey, byte b);

    List<Document> findAllByUserAndLovedOrderByCreatedAtDesc(User user, byte b);

    List<Document> findByUserAndStatusDeleteOrderByCreatedAtDesc(User user, byte b);

    List<Document> findAllByUserAndDocsPublicOrderByCreatedAtDesc(User user, byte b);

    List<Document> findAllByUserAndDocsStatusOrderByCreatedAtDesc(User user, byte b);

    List<Document> findAllByUserAndDocsPublicOrderByUpdatedAtDesc(User following, byte b);

//    Document findByDocumentKeyAndStatusDeleteAndDocsPublic(String documentKey, byte b, byte b1);

    List<Document> findTop20ByStatusDeleteAndDocsPublicAndUserNotOrderByQuantityLikeDesc(byte b, byte b1, User user);
}