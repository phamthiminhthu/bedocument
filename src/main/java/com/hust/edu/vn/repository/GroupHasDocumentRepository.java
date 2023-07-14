package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.GroupHasDocument;
import com.hust.edu.vn.entity.GroupDoc;
import com.hust.edu.vn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupHasDocumentRepository extends JpaRepository<GroupHasDocument, Long> {

    List<GroupHasDocument> findByGroup(GroupDoc groupDoc);

    GroupHasDocument findByGroupAndDocumentId(GroupDoc groupDoc, Long documentId);

    boolean existsByDocumentAndGroupId(Document document, Long id);

    boolean existsByDocument(Document document);

    List<GroupHasDocument> findAllByGroupId(Long groupId);

    void deleteByGroupId(Long id);

    List<GroupHasDocument> findAllByDocument(Document document);

    GroupHasDocument findByDocumentAndGroupId(Document document, Long groupId);

    @Query("""
            SELECT COUNT(gd) > 0
            FROM GroupHasDocument gd
            JOIN GroupShareUser gu
            ON gu.group = gd.group
            WHERE gd.document = :document AND gu.user = :user
            """)
    boolean existsUserInGroupWithDocument(User user, Document document);
}