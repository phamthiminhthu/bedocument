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


    List<Document> findByUserAndStatusDeleteOrderByCreatedAtDesc(User user, byte b);

    List<Document> findAllByUserAndDocsPublicOrderByCreatedAtDesc(User user, byte b);

    List<Document> findAllByUserAndDocsStatusOrderByCreatedAtDesc(User user, byte b);

    List<Document> findAllByUserAndDocsPublicOrderByUpdatedAtDesc(User following, byte b);


    @Query(value = """ 
            SELECT DISTINCT d.* FROM document d LEFT JOIN tags t
            ON t.document_id = d.document_id
            LEFT JOIN type_document td
            ON td.document_id = d.document_id
            WHERE d.user_id != :userId
            AND d.status_delete = 0 AND d.docs_public = 1
            AND (
                EXISTS 
                (SELECT 1
                FROM tags t2
                WHERE LOWER(t2.tag_name) IN :tagsSuggest
                AND LOWER(t.tag_name) LIKE CONCAT('%', LOWER(t2.tag_name), '%')
                )
                OR 
                EXISTS 
                (SELECT 1
                FROM type_document td2
                WHERE LOWER(td2.type_name) IN :typeDocumentSuggest
                AND LOWER(td.type_name) LIKE CONCAT('%', LOWER(td2.type_name), '%')
                )
                OR MATCH(d.authors) AGAINST( :authorsSuggest IN NATURAL LANGUAGE MODE)
            )
            ORDER BY d.quantity_like DESC, d.created_at DESC
        """, nativeQuery = true)
    List<Document> findAllByTagsAndTypesAndAuthorsNotInUsers(List<String> tagsSuggest, List<String> typeDocumentSuggest, String authorsSuggest, Long userId);

    @Query(""" 
            SELECT DISTINCT d FROM Document d
            WHERE d.user.id NOT IN :usersId
            AND d.statusDelete = 0
            AND d.docsPublic = 1
            ORDER BY d.quantityLike DESC, d.updatedAt DESC
            LIMIT 10
        """)
    List<Document> findTop10ByOrderByQuantityLikeDescUpdatedAtDescAndNotUsers(List<Long> usersId);
    List<Document> findByUserAndStatusDeleteAndDocsHashcode(User user, byte b, String docsHashCode);

    List<Document> findTop20ByUserAndStatusDeleteOrderByCreatedAtDesc(User user, byte b);

    @Query(""" 
            SELECT DISTINCT d FROM Document d
            WHERE d.user.id IN :usersId
            AND d.statusDelete = :b
            AND d.docsPublic = :b1
            ORDER BY d.updatedAt DESC,  d.quantityLike DESC
            LIMIT 20
        """)
    List<Document> findTop20ByStatusDeleteAndDocsPublicInUsersIdOrderByCreatedAtDescAndQuantityLikeDesc(byte b, byte b1, List<Long> usersId);

    List<Document> findAllByUserAndStatusDeleteAndLovedOrderByCreatedAtDesc(User user, byte b, byte b1);
}