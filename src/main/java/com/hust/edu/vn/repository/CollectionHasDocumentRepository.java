package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Collection;
import com.hust.edu.vn.entity.CollectionHasDocument;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
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

    List<CollectionHasDocument> findAllByCollection(Collection collection);
}