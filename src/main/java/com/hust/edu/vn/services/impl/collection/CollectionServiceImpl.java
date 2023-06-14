package com.hust.edu.vn.services.impl.collection;

import com.hust.edu.vn.entity.Collection;
import com.hust.edu.vn.entity.CollectionHasDocument;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.model.CollectionModel;
import com.hust.edu.vn.repository.CollectionHasDocumentRepository;
import com.hust.edu.vn.repository.CollectionRepository;
import com.hust.edu.vn.repository.DocumentRepository;
import com.hust.edu.vn.services.collection.CollectionService;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class CollectionServiceImpl implements CollectionService {
    private final DocumentRepository documentRepository;
    private final CollectionRepository collectionRepository;
    private final ModelMapperUtils modelMapperUtils;

    private final CollectionHasDocumentRepository collectionHasDocumentRepository;

    private final BaseUtils baseUtils;

    public CollectionServiceImpl(CollectionRepository collectionRepository, ModelMapperUtils modelMapperUtils,
                                 CollectionHasDocumentRepository collectionHasDocumentRepository,
                                 DocumentRepository documentRepository, BaseUtils baseUtils) {
        this.collectionRepository = collectionRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.collectionHasDocumentRepository = collectionHasDocumentRepository;
        this.documentRepository = documentRepository;
        this.baseUtils = baseUtils;
    }

    @Override
    public boolean createCollection(CollectionModel collectionModel) {
        User user = baseUtils.getUser();
        if (user != null) {
            if(collectionRepository.existsByCollectionNameAndParentCollectionIdAndUserId(collectionModel.getCollectionName(), collectionModel.getParentCollectionId(), user.getId())){
                return false;
            }
            if(collectionModel.getParentCollectionId() != null){
                Collection collection = collectionRepository.findByIdAndUserId(collectionModel.getParentCollectionId(), user.getId());
                if(collection != null){
                    Collection newCollection = modelMapperUtils.mapAllProperties(collectionModel, Collection.class);
                    newCollection.setUser(user);
                    collectionRepository.save(newCollection);
                    return true;
                }
                return false;
            }else{
                Collection newCollection = modelMapperUtils.mapAllProperties(collectionModel, Collection.class);
                newCollection.setUser(user);
                collectionRepository.save(newCollection);
                return true;
            }
        }
       return false;
    }

    @Override
    public TreeMap<Long, List<Collection>> showCollection() {
        User user = baseUtils.getUser();
        List<Collection> listCollection = collectionRepository.findByUserIdAndGroupDoc(user.getId(), null);
        if(listCollection.size() > 0){
            TreeMap<Long, List<Collection>> result = new TreeMap<>();
            for (Collection collection : listCollection){
                Long idParent = collection.getParentCollectionId();
                if(idParent != null){
                    collectionRepository.findById(idParent).ifPresent(collectionParent -> result.computeIfAbsent(collectionParent.getId(), k -> new ArrayList<>()).add(collection));
                }else{
                    result.put(collection.getId(), null);
                }
            }
            return result;
        }
        return null;
    }

    @Override
    public boolean updateCollection(Long id, CollectionModel collectionModel) {
        User user = baseUtils.getUser();
        if(user != null){
            Collection collection = collectionRepository.findByIdAndUserId(id, user.getId());
            if(collection != null){
                if(collection.getCollectionName().equals(collectionModel.getCollectionName())){
                    if((collectionModel.getParentCollectionId() == null && !collectionRepository.existsByCollectionNameAndParentCollectionIdAndUserId(collection.getCollectionName(), null, user.getId()))  || (collectionRepository.existsByIdAndUserId(collectionModel.getParentCollectionId(), user.getId()))){
                        collection.setCollectionName(collectionModel.getCollectionName());
                        collection.setParentCollectionId(collectionModel.getParentCollectionId());
                        collection.setUpdatedAt(new Date());
                        collectionRepository.save(collection);
                        return true;
                    }
                    return false;
                }else{
                    if(collectionRepository.existsByCollectionNameAndParentCollectionIdAndUserId(collectionModel.getCollectionName(), collectionModel.getParentCollectionId(), user.getId())){
                        return false;
                    }else{
                        if(collectionRepository.existsByIdAndUserId(collectionModel.getParentCollectionId(), user.getId())){
                            collection.setCollectionName(collectionModel.getCollectionName());
                            collection.setParentCollectionId(collectionModel.getParentCollectionId());
                            collection.setUpdatedAt(new Date());
                            collectionRepository.save(collection);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean deleteCollection(Long id) {
        User user =  baseUtils.getUser();
        if(user != null){
            Collection collection = collectionRepository.findByIdAndUserId(id, user.getId());
            if(collection != null){
                TreeMap<Long, List<Collection>> treeCollection = showCollection();
                Set<Long> collectionIds = getListCollectionId(treeCollection, collection.getId());
                for(Long subId : collectionIds){
                    List<CollectionHasDocument> collectionHasDocuments = collectionHasDocumentRepository.findByCollectionIdAndDocumentStatusDelete(subId, (byte) 0);
                    if(collectionHasDocuments != null && !collectionHasDocuments.isEmpty()){
                        for(CollectionHasDocument collectionHasDocument : collectionHasDocuments){
                            Document document = collectionHasDocument.getDocument();
                            document.setStatusDelete((byte) 1);
                            documentRepository.save(document);
                        }
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    private Set<Long> getListCollectionId(TreeMap<Long, List<Collection>> data, Long idCollection){
        Set<Long> collectionIds = new HashSet<>();
        List<Collection> listSubCollection = data.get(idCollection);
        if(listSubCollection != null && !listSubCollection.isEmpty()){
            for (Collection subCollection : listSubCollection) {
                collectionIds.addAll(getListCollectionId(data, subCollection.getId()));
            }
        }
        collectionIds.add(idCollection);
        return collectionIds;
    }
}
