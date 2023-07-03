package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.DocumentShareUser;
import com.hust.edu.vn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentShareUserRepository extends JpaRepository<DocumentShareUser, Long> {
    DocumentShareUser findByDocumentAndUser(Document document, User user);

    boolean existsByUserAndDocument(User guest, Document document);

    List<DocumentShareUser> findAllByUser(User user);

    boolean existsByDocument(Document document);

    List<DocumentShareUser> findAllByDocument(Document document);

}