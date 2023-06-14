package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.LikeDocument;
import com.hust.edu.vn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeDocumentRepository extends JpaRepository<LikeDocument, Long> {
    boolean existsByUserAndDocument(User user, Document document);

    List<LikeDocument> findAllByDocument(Document document);

    LikeDocument findByUserAndDocument(User user, Document document);
}