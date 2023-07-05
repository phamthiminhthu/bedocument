package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Collection;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.GroupHasDocument;
import com.hust.edu.vn.entity.GroupDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupHasDocumentRepository extends JpaRepository<GroupHasDocument, Long> {

    List<GroupHasDocument> findByGroup(GroupDoc groupDoc);

    GroupHasDocument findByGroupAndDocumentId(GroupDoc groupDoc, Long documentId);

    List<GroupHasDocument> findAllByGroup(GroupDoc groupDoc);

    boolean existsByDocumentAndGroupId(Document document, Long id);

    boolean existsByDocument(Document document);
}