package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.TypeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TypeDocumentRepository extends JpaRepository<TypeDocument, Long> {
    boolean existsByDocumentAndTypeName(Document document, String type);

    boolean existsByDocument(Document document);

    List<TypeDocument> findAllByDocument(Document document);

    TypeDocument findByDocumentAndTypeName(Document document, String typeName);

    List<TypeDocument> findByTypeNameContainingIgnoreCase(String typeName);


    @Query(""" 
            SELECT DISTINCT LOWER(td.typeName) FROM TypeDocument td
            JOIN Document d
            ON td.document = d
            WHERE d.user.id IN :usersId
        """)
    List<String> findAllByUsers(List<Long> usersId);

}