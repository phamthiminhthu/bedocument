package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionHasDocumentRepository extends JpaRepository<CollectionHasDocument, Long> {
    boolean existsByDocumentId(Long id);

    List<CollectionHasDocument> findByDocumentId(Long id);

    List<CollectionHasDocument> findByCollectionIdAndDocumentStatusDelete(Long collectionId, byte b);

    CollectionHasDocument findByCollectionAndDocument(Collection collection, Document document);

    boolean existsByCollectionAndDocument(Collection newCollection, Document document);

    boolean existsByDocumentDocumentKeyAndCollectionIdAndCollectionUser(String key, Long id, User user);

    List<CollectionHasDocument> findAllByCollectionOrderByCreatedAtDesc(Collection collection);

    List<CollectionHasDocument> findByCollection(Collection collection);


    List<CollectionHasDocument> findByDocument(Document document);

    @Query(""" 
            SELECT COUNT(chd) > 1 FROM CollectionHasDocument chd
            JOIN Collection c
            ON chd.collection = c
            WHERE chd.document = :document and c.groupDoc = :groupDoc
        """)
    boolean existsMultipleCollectionHasDocumentsByDocumentAndGroup(Document document, GroupDoc groupDoc);

    CollectionHasDocument findByDocumentAndCollectionId(Document document, Long collectionId);
}