package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.TypeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TypeDocumentRepository extends JpaRepository<TypeDocument, Long> {
    boolean existsByDocumentAndTypeName(Document document, String type);

    boolean existsByDocument(Document document);

    List<TypeDocument> findAllByDocument(Document document);
}