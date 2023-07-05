package com.hust.edu.vn.services.collection;

import com.hust.edu.vn.dto.CollectionDto;
import com.hust.edu.vn.entity.Collection;
import com.hust.edu.vn.model.CollectionModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public interface CollectionService {
    boolean createCollection(CollectionModel collectionModel);
    TreeMap<Long, List<Collection>> showCollection();
    boolean updateCollection(Long id, CollectionModel collectionModel);
    boolean deleteCollection(Long id);

    HashMap<String, ArrayList<Object>> showAllDetailsCollectionById(Long id);

    List<CollectionDto> showAllCollectionParent();

    boolean renameCollection(Long id, String name);

    CollectionDto showCollectionById(Long id);

    List<CollectionDto> showAllNameCollectionWithoutGroupDoc();
}
