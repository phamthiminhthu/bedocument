package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Collection;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.GroupCollectionHasDocument;
import com.hust.edu.vn.entity.GroupDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupCollectionHasDocumentRepository extends JpaRepository<GroupCollectionHasDocument, Long> {

    List<GroupCollectionHasDocument> findByGroupAndCollectionId(GroupDoc groupDoc, Long collectionId);

    GroupCollectionHasDocument findByGroupAndCollectionIdAndDocumentId(GroupDoc groupDoc, Long collectionId, Long documentId);

    List<GroupCollectionHasDocument> findAllByGroup(GroupDoc groupDoc);

    boolean existsByDocumentAndGroupIdAndCollection(Document document, Long id, Collection collection);

    boolean existsByDocumentAndCollectionId(Document document, Long id);
}