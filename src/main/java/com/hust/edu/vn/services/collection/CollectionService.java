package com.hust.edu.vn.services.collection;

import com.hust.edu.vn.dto.CollectionDto;
import com.hust.edu.vn.dto.CollectionTreeDto;
import com.hust.edu.vn.dto.GroupDocTreeDto;
import com.hust.edu.vn.model.CollectionModel;

import java.util.List;

public interface CollectionService {
    boolean createCollection(CollectionModel collectionModel);
//    TreeMap<Long, List<Collection>> showCollection();
//    boolean updateCollection(Long id, CollectionModel collectionModel);
    boolean deleteCollection(Long id);

    CollectionDto showAllDetailsCollectionById(Long id, Long groupId);

    List<CollectionDto> getAllCollectionsByUser();

    boolean renameCollection(Long id, String name);

    CollectionDto showCollectionById(Long id);

    List<CollectionDto> showAllNameCollectionWithoutGroupDoc();

    List<CollectionTreeDto> showDetailsAllCollections();

    List<GroupDocTreeDto> showDetailsAllCollectionsByGroup();

    List<CollectionTreeDto> showBreadcrumbsCollectionsById(Long idCollection, Long idGroup);
}
