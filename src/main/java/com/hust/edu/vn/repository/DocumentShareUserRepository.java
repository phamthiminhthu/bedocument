package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.DocumentShareUser;
import com.hust.edu.vn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentShareUserRepository extends JpaRepository<DocumentShareUser, Long> {
    DocumentShareUser findByDocumentAndUser(Document document, User user);

    boolean existsByUserAndDocument(User guest, Document document);
}