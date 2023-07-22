package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

//    List<Document> findTop20ByStatusDeleteAndDocsPublicAndUserNotOrderByQuantityLikeDesc(byte b, byte b1, User user);


    @Query(""" 
            SELECT DISTINCT d FROM Document d
            LEFT JOIN Tag t
            ON t.document = d
            LEFT JOIN TypeDocument td
            ON td.document = d
            WHERE
            d.user.id NOT IN :usersId
            AND d.statusDelete = 0
            AND d.docsPublic = 1
            AND (
            EXISTS (
            SELECT 1 FROM Tag t2 WHERE LOWER(t2.tagName) IN :listTagNameFollow
            AND LOWER(t.tagName)  LIKE CONCAT('%', LOWER(t2.tagName), '%')
            )
            OR
            EXISTS (
            SELECT 1 FROM TypeDocument td2 WHERE LOWER(td2.typeName) IN :listTypeDocumentFollow
            AND LOWER(td.typeName) LIKE CONCAT('%', LOWER(td2.typeName), '%')
            )
            )
        """)
    List<Document> findAllByTagsAndTypedocumentNotInUsers(List<String> listTagNameFollow, List<String> listTypeDocumentFollow, List<Long> usersId);


    @Query(""" 
            SELECT DISTINCT d FROM Document d
            WHERE d.user.id NOT IN :usersId
            AND d.statusDelete = 0
            AND d.docsPublic = 1
            ORDER BY d.quantityLike DESC, d.updatedAt DESC
            LIMIT 10
        """)
    List<Document> findTop10ByOrderByQuantityLikeDescUpdatedAtDescAndNotUsers(List<Long> usersId);
}