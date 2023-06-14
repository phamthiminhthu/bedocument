package com.hust.edu.vn.services.collection;

import com.hust.edu.vn.entity.Collection;
import com.hust.edu.vn.model.CollectionModel;

import java.util.List;
import java.util.TreeMap;

public interface CollectionService {
    boolean createCollection(CollectionModel collectionModel);
    TreeMap<Long, List<Collection>> showCollection();
    boolean updateCollection(Long id, CollectionModel collectionModel);
    boolean deleteCollection(Long id);
}
