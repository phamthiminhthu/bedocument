package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Collection;
import com.hust.edu.vn.entity.GroupDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    Collection findByIdAndUserId(Long id, Long userId);

    boolean existsByCollectionNameAndParentCollectionIdAndUserId(String collectionName, Long parentCollectionId,  Long userId);

    boolean existsByIdAndUserId(Long parentCollectionId, Long id);

    List<Collection> findByUserIdAndGroupDoc(Long id, Object o);


    boolean existsByGroupDocAndCollectionNameAndParentCollectionId(GroupDoc groupDoc, String collectionName, Long id);

    void deleteByGroupDocId(Long id);

    List<Collection> findAllByGroupDocId(Long groupId);

    Collection findByIdAndGroupDoc(Long collectionId, GroupDoc groupDoc);
}