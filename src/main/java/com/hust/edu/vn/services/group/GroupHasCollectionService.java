package com.hust.edu.vn.services.group;

import com.hust.edu.vn.dto.CollectionDto;
import com.hust.edu.vn.model.CollectionModel;

import java.util.List;
import java.util.TreeMap;

public interface GroupHasCollectionService {
    boolean createCollectionGroupDoc(Long groupId, CollectionModel collectionModel);

    TreeMap<Long, List<CollectionDto>> showAllCollectionGroupDoc(Long groupId);

    boolean updateCollectionGroupDoc(Long groupId, Long collectionId, CollectionModel collectionModel);

    boolean deleteCollectionGroupDoc(Long groupId, Long collectionId);
}
