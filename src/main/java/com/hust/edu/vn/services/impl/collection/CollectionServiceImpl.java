package com.hust.edu.vn.services.impl.collection;

import com.hust.edu.vn.dto.CollectionDto;
import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.entity.Collection;
import com.hust.edu.vn.entity.CollectionHasDocument;
import com.hust.edu.vn.entity.GroupDoc;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.model.CollectionModel;
import com.hust.edu.vn.repository.CollectionHasDocumentRepository;
import com.hust.edu.vn.repository.CollectionRepository;
import com.hust.edu.vn.repository.GroupDocRepository;
import com.hust.edu.vn.services.collection.CollectionService;
import com.hust.edu.vn.services.document.DocumentService;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class CollectionServiceImpl implements CollectionService {
    private final GroupDocRepository groupDocRepository;
    private final CollectionRepository collectionRepository;
    private final ModelMapperUtils modelMapperUtils;

    private final CollectionHasDocumentRepository collectionHasDocumentRepository;

    private final BaseUtils baseUtils;

    private final DocumentService documentService;

    public CollectionServiceImpl(CollectionRepository collectionRepository, ModelMapperUtils modelMapperUtils,
                                 CollectionHasDocumentRepository collectionHasDocumentRepository,
                                 BaseUtils baseUtils, DocumentService documentService,
                                 GroupDocRepository groupDocRepository) {
        this.collectionRepository = collectionRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.collectionHasDocumentRepository = collectionHasDocumentRepository;
        this.baseUtils = baseUtils;
        this.documentService = documentService;
        this.groupDocRepository = groupDocRepository;
    }

    @Override
    public boolean createCollection(CollectionModel collectionModel) {
        User user = baseUtils.getUser();
        if (user != null) {
            if(collectionRepository.existsByCollectionNameAndParentCollectionIdAndUserId(collectionModel.getCollectionName(), collectionModel.getParentCollectionId(), user.getId())){
                return false;
            }
            Collection newCollection = modelMapperUtils.mapAllProperties(collectionModel, Collection.class);
            newCollection.setUser(user);
            if(collectionModel.getGroupId() != null){
                groupDocRepository.findById(collectionModel.getGroupId()).ifPresent(newCollection::setGroupDoc);
                return false;
            }
            if(collectionModel.getParentCollectionId() != null){
                Collection collection = collectionRepository.findByIdAndUserId(collectionModel.getParentCollectionId(), user.getId());
                if(collection != null){
                    if(collectionModel.getGroupId() != null){
                        GroupDoc groupDoc = groupDocRepository.findById(collectionModel.getGroupId()).orElse(null);
                        newCollection.setGroupDoc(groupDoc);
                    }
                    collectionRepository.save(newCollection);
                    return true;
                }
                return false;
            }else{
                collectionRepository.save(newCollection);
                return true;
            }
        }
       return false;
    }

    @Override
    public TreeMap<Long, List<Collection>> showCollection() {
        User user = baseUtils.getUser();
        if(user != null) {
            List<Collection> listCollection = collectionRepository.findByUserIdAndGroupDoc(user.getId(), null);
            TreeMap<Long, List<Collection>> result = new TreeMap<>();
            if (listCollection.size() > 0) {
                for (Collection collection : listCollection) {
                    Long idParent = collection.getParentCollectionId();
                    if (idParent != null) {
                        collectionRepository.findById(idParent).ifPresent(collectionParent -> result.computeIfAbsent(collectionParent.getId(), k -> new ArrayList<>()).add(collection));
                    } else {
                        result.put(collection.getId(), null);
                    }
                }
            }
            return result;
        }
        return null;
    }


//    @Override
//    public boolean updateCollection(Long id, CollectionModel collectionModel) {
//        User user = baseUtils.getUser();
//        if(user != null){
//            Collection collection = collectionRepository.findByIdAndUserId(id, user.getId());
//            if(collection != null){
//                if(collection.getCollectionName().equals(collectionModel.getCollectionName())){
//                    if((collectionModel.getParentCollectionId() == null && !collectionRepository.existsByCollectionNameAndParentCollectionIdAndUserId(collection.getCollectionName(), null, user.getId()))  || (collectionRepository.existsByIdAndUserId(collectionModel.getParentCollectionId(), user.getId()))){
//                        collection.setCollectionName(collectionModel.getCollectionName());
//                        collection.setParentCollectionId(collectionModel.getParentCollectionId());
//                        collection.setUpdatedAt(new Date());
//                        collectionRepository.save(collection);
//                        return true;
//                    }
//                    return false;
//                }else{
//                    if(collectionRepository.existsByCollectionNameAndParentCollectionIdAndUserId(collectionModel.getCollectionName(), collectionModel.getParentCollectionId(), user.getId())){
//                        return false;
//                    }else{
//                        if(collectionRepository.existsByIdAndUserId(collectionModel.getParentCollectionId(), user.getId())){
//                            collection.setCollectionName(collectionModel.getCollectionName());
//                            collection.setParentCollectionId(collectionModel.getParentCollectionId());
//                            collection.setUpdatedAt(new Date());
//                            collectionRepository.save(collection);
//                            return true;
//                        }
//                    }
//                }
//            }
//        }
//        return false;
//    }

    @Override
    public boolean renameCollection(Long id, String name) {
        User user = baseUtils.getUser();
        if(user != null){
            Collection collection = collectionRepository.findByIdAndUser(id, user);
            if(collection != null){
                if(collection.getCollectionName().equals(name)){
                    return true;
                }
                if(!collectionRepository.existsByCollectionNameAndParentCollectionIdAndUserId(name, collection.getParentCollectionId(), user.getId())){
                    collection.setCollectionName(name);
                    collection.setUpdatedAt(new Date());
                    collectionRepository.save(collection);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public CollectionDto showCollectionById(Long id) {
        User user = baseUtils.getUser();
        if(user != null){
            Collection collection = collectionRepository.findByIdAndUser(id, user);
            if(collection != null){
                return modelMapperUtils.mapAllProperties(collection, CollectionDto.class);
            }
        }
        return null;
    }

    @Override
    public List<CollectionDto> showAllNameCollectionWithoutGroupDoc() {
        User user = baseUtils.getUser();
        if(user != null){
            List<Collection> collectionList = collectionRepository.findAllByUserAndGroupDoc(user, null);
            List<CollectionDto> collectionDtoList = new ArrayList<>();
            if(collectionList != null && !collectionList.isEmpty()){
                for(Collection collection : collectionList){
                    CollectionDto collectionDto = modelMapperUtils.mapAllProperties(collection, CollectionDto.class);
                    if(collection.getParentCollectionId() != null){
                        collectionRepository.findById(collection.getParentCollectionId()).ifPresent(parentCollection -> collectionDto.setParentCollectionName(parentCollection.getCollectionName()));
                    }
                    collectionDtoList.add(collectionDto);
                }
            }
            return collectionDtoList;
        }
        return null;
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
                        collectionHasDocumentRepository.deleteAll(collectionHasDocuments);
                    }
                    collectionRepository.deleteById(subId);
                }
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public CollectionDto showAllDetailsCollectionById(Long id, Long groupId) {
        User user = baseUtils.getUser();
        if(user != null){
            Collection collection = collectionRepository.findById(id).orElse(null);
            CollectionDto collectionDto = modelMapperUtils.mapAllProperties(collection, CollectionDto.class);
            List<Collection> subCollections;
            if(groupId == null){
                subCollections = collectionRepository.findAllByParentCollectionIdAndUser(collectionDto.getId(), user);
            }else{
                subCollections = collectionRepository.findAllByParentCollectionIdAndGroupDocId(collectionDto.getId(), groupId);
            }
            List<CollectionDto> subCollectionDtoList = new ArrayList<>();
           if(subCollections != null && subCollections.size() > 0){
               for(Collection collection1 : subCollections){
                   subCollectionDtoList.add(modelMapperUtils.mapAllProperties(collection1, CollectionDto.class));
               }
           }
            collectionDto.setSubCollectionDtoList(subCollectionDtoList);
            List<CollectionHasDocument> collectionHasDocuments = collectionHasDocumentRepository.findAllByCollectionOrderByCreatedAtDesc(collection);
            List<DocumentDto> documents = new ArrayList<>();
            if(collectionHasDocuments != null && !collectionHasDocuments.isEmpty()){
                for(CollectionHasDocument collectionHasDocument : collectionHasDocuments){
                    DocumentDto documentDto = documentService.getDocumentModel(collectionHasDocument.getDocument().getDocumentKey());
                    if(documentDto != null){
                        documents.add(documentDto);
                    }
                }
            }
            collectionDto.setDocumentDtoList(documents);
            return collectionDto;
        }
        return null;
    }

    @Override
    public List<CollectionDto> showAllCollectionParent() {
        User user = baseUtils.getUser();
        if(user != null){
            List<Collection> collectionList = collectionRepository.findAllByParentCollectionIdAndUserAndGroupDoc(null, user, null);
            List<CollectionDto> collectionDtoList = new ArrayList<>();
            if(collectionList != null && !collectionList.isEmpty()){
                for(Collection collection : collectionList){
                    collectionDtoList.add(modelMapperUtils.mapAllProperties(collection, CollectionDto.class));
                }
            }
            return collectionDtoList;
        }
        return null;
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
