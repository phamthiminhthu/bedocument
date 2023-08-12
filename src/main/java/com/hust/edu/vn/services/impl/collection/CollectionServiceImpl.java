package com.hust.edu.vn.services.impl.collection;

import com.hust.edu.vn.dto.*;
import com.hust.edu.vn.entity.*;
import com.hust.edu.vn.entity.Collection;
import com.hust.edu.vn.model.CollectionModel;
import com.hust.edu.vn.repository.CollectionHasDocumentRepository;
import com.hust.edu.vn.repository.CollectionRepository;
import com.hust.edu.vn.repository.GroupDocRepository;
import com.hust.edu.vn.repository.GroupShareUserRepository;
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
    private final GroupShareUserRepository groupShareUserRepository;
    private final GroupDocRepository groupDocRepository;
    private final CollectionRepository collectionRepository;
    private final ModelMapperUtils modelMapperUtils;

    private final CollectionHasDocumentRepository collectionHasDocumentRepository;

    private final BaseUtils baseUtils;

    private final DocumentService documentService;

    public CollectionServiceImpl(CollectionRepository collectionRepository, ModelMapperUtils modelMapperUtils,
                                 CollectionHasDocumentRepository collectionHasDocumentRepository,
                                 BaseUtils baseUtils, DocumentService documentService,
                                 GroupDocRepository groupDocRepository,
                                 GroupShareUserRepository groupShareUserRepository) {
        this.collectionRepository = collectionRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.collectionHasDocumentRepository = collectionHasDocumentRepository;
        this.baseUtils = baseUtils;
        this.documentService = documentService;
        this.groupDocRepository = groupDocRepository;
        this.groupShareUserRepository = groupShareUserRepository;
    }

    @Override
    public boolean createCollection(CollectionModel collectionModel) {
        User user = baseUtils.getUser();
        if (user != null) {
            if (collectionRepository.existsByCollectionNameAndParentCollectionIdAndGroupDocIdAndUserId(collectionModel.getCollectionName(), collectionModel.getParentCollectionId(), collectionModel.getGroupId(), user.getId())) {
                return false;
            }

            Collection newCollection = modelMapperUtils.mapAllProperties(collectionModel, Collection.class);
            newCollection.setUser(user);
            if (collectionModel.getGroupId() != null) {
                GroupDoc groupDoc = groupDocRepository.findById(collectionModel.getGroupId()).orElse(null);
                if (groupDoc != null) {
                    newCollection.setGroupDoc(groupDoc);
                } else {
                    return false;
                }
            }
            if (collectionModel.getParentCollectionId() != null) {
                Collection collection = collectionRepository.findByIdAndUserId(collectionModel.getParentCollectionId(), user.getId());
                if (collection != null) {
                    if (collectionModel.getGroupId() != null) {
                        GroupDoc groupDoc = groupDocRepository.findById(collectionModel.getGroupId()).orElse(null);
                        newCollection.setGroupDoc(groupDoc);
                    }
                    collectionRepository.save(newCollection);
                    return true;
                }
                return false;
            } else {
                collectionRepository.save(newCollection);
                return true;
            }
        }
        return false;
    }

//    @Override
//    public TreeMap<Long, List<Collection>> showCollection() {
//        User user = baseUtils.getUser();
//        if(user != null) {
//            List<Collection> listCollection = collectionRepository.findByUserIdAndGroupDoc(user.getId(), null);
//            TreeMap<Long, List<Collection>> result = new TreeMap<>();
//            if (listCollection.size() > 0) {
//                for (Collection collection : listCollection) {
//                    Long idParent = collection.getParentCollectionId();
//                    if (idParent != null) {
//                        collectionRepository.findById(idParent).ifPresent(collectionParent -> result.computeIfAbsent(collectionParent.getId(), k -> new ArrayList<>()).add(collection));
//                    } else {
//                        result.put(collection.getId(), null);
//                    }
//                }
//            }
//            return result;
//        }
//        return null;
//    }


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
        if (user != null) {
            Collection collection = collectionRepository.findByIdAndUser(id, user);
            if (collection != null) {
                if (collection.getCollectionName().equals(name)) {
                    return true;
                }
                if (!collectionRepository.existsByCollectionNameAndParentCollectionIdAndUserId(name, collection.getParentCollectionId(), user.getId())) {
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
        if (user != null) {
            Collection collection = collectionRepository.findByIdAndUser(id, user);
            if (collection != null) {
                return modelMapperUtils.mapAllProperties(collection, CollectionDto.class);
            }
        }
        return null;
    }

    @Override
    public List<CollectionDto> showAllNameCollectionWithoutGroupDoc() {
        User user = baseUtils.getUser();
        if (user != null) {
            List<Collection> collectionList = collectionRepository.findAllByUserAndGroupDoc(user, null);
            List<CollectionDto> collectionDtoList = new ArrayList<>();
            if (collectionList != null && !collectionList.isEmpty()) {
                for (Collection collection : collectionList) {
                    CollectionDto collectionDto = modelMapperUtils.mapAllProperties(collection, CollectionDto.class);
                    if (collection.getParentCollectionId() != null) {
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
        User user = baseUtils.getUser();
        if (user != null) {
            Collection collection = collectionRepository.findByIdAndUserId(id, user.getId());
            if (collection != null) {
//                List<Collection> collectionList = collection.getSubCollectionDtoList();
//                for (Collection subCollection : collectionList) {
//                    List<CollectionHasDocument> collectionHasDocuments = collectionHasDocumentRepository.findByCollectionAndDocumentStatusDelete(subCollection, (byte) 0);
//                    if (collectionHasDocuments != null && !collectionHasDocuments.isEmpty()) {
//                        collectionHasDocumentRepository.deleteAll(collectionHasDocuments);
//                    }
//                }
//                collectionRepository.deleteAll(collectionList);
                collectionRepository.delete(collection);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public CollectionDto showAllDetailsCollectionById(Long id, Long groupId) {
        User user = baseUtils.getUser();
        if (user != null) {
            Collection collection = collectionRepository.findById(id).orElse(null);
            CollectionDto collectionDto = modelMapperUtils.mapAllProperties(collection, CollectionDto.class);
            List<Collection> subCollections;
            if (groupId == null) {
                subCollections = collectionRepository.findAllByParentCollectionIdAndUser(collectionDto.getId(), user);
            } else {
                subCollections = collectionRepository.findAllByParentCollectionIdAndGroupDocId(collectionDto.getId(), groupId);
            }
            List<CollectionDto> subCollectionDtoList = new ArrayList<>();
            if (subCollections != null && subCollections.size() > 0) {
                for (Collection collection1 : subCollections) {
                    subCollectionDtoList.add(modelMapperUtils.mapAllProperties(collection1, CollectionDto.class));
                }
            }
            collectionDto.setSubCollectionDtoList(subCollectionDtoList);
            List<CollectionHasDocument> collectionHasDocuments = collectionHasDocumentRepository.findAllByCollectionOrderByCreatedAtDesc(collection);
            List<DocumentDto> documents = new ArrayList<>();
            if (collectionHasDocuments != null && !collectionHasDocuments.isEmpty()) {
                for (CollectionHasDocument collectionHasDocument : collectionHasDocuments) {
                    DocumentDto documentDto = documentService.getDocumentModel(collectionHasDocument.getDocument().getDocumentKey());
                    if (documentDto != null) {
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
    public List<CollectionDto> getAllCollectionsByUser() {
        User user = baseUtils.getUser();
        if (user != null) {
            List<Collection> collectionList = collectionRepository.findAllByParentCollectionIdAndUserAndGroupDoc(null, user, null);
            List<CollectionDto> collectionDtoList = new ArrayList<>();
            if (collectionList != null && !collectionList.isEmpty()) {
                for (Collection collection : collectionList) {
                    collectionDtoList.add(modelMapperUtils.mapAllProperties(collection, CollectionDto.class));
                }
            }
            return collectionDtoList;
        }
        return null;
    }

    @Override
    public List<CollectionTreeDto> showDetailsAllCollections() {
        User user = baseUtils.getUser();
        if (user != null) {
            List<Collection> collectionsList = collectionRepository.findByUserIdAndGroupDoc(user.getId(), null);
//            List<CollectionTreeDto> collectionTreeDtoList = c
//            if (collectionsList != null && collectionsList.size() > 0) {
//                for (Collection collection : collectionsList) {
//                    if (collection.getParentCollectionId() == null) {
//                        collectionTreeDtoList.add(modelMapperUtils.mapAllProperties(collection, CollectionTreeDto.class));
//                    }
//                }
//            }
            return convertCollectionsToCollectionTreeDtoList(collectionsList);
        }
        return null;
    }

    @Override
    public List<GroupDocTreeDto> showDetailsAllCollectionsByGroup() {
        User user = baseUtils.getUser();
        if (user != null) {
            List<GroupDoc> groupDocs = groupDocRepository.findAllByUser(user);
            List<GroupShareUser> groupShareUsers = groupShareUserRepository.findAllByUser(user);
            if (groupShareUsers != null && groupShareUsers.size() > 0) {
                for (GroupShareUser groupShareUser : groupShareUsers) {
                    groupDocs.add(groupShareUser.getGroup());
                }
            }
            List<GroupDocTreeDto> groupDocTreeDtoList = new ArrayList<>();
            if (groupDocs != null && groupDocs.size() > 0) {
                groupDocs.sort(Comparator.comparing(GroupDoc::getCreatedAt));
                for (GroupDoc groupDoc : groupDocs) {
                    GroupDocTreeDto groupDocTreeDto = new GroupDocTreeDto();
                    groupDocTreeDto.setGroupName(groupDoc.getGroupName());
                    groupDocTreeDto.setId(groupDoc.getId());
                    List<Collection> collectionsListGroupDoc = collectionRepository.findByGroupDoc(groupDoc);
                    List<CollectionTreeDto> collectionTreeDtoList = convertCollectionsToCollectionTreeDtoList(collectionsListGroupDoc);
                    groupDocTreeDto.setCollectionDtoList(collectionTreeDtoList);
                    groupDocTreeDtoList.add(groupDocTreeDto);
                }
            }
            return groupDocTreeDtoList;
        }
        return null;
    }

    @Override
    public List<CollectionTreeDto> showBreadcrumbsCollectionsById(Long idCollection, Long idGroup) {
        User user = baseUtils.getUser();
        if (user != null) {
            List<CollectionTreeDto> result = new ArrayList<>();
            if (idCollection != null && idGroup != null) {
                GroupDoc groupDoc = baseUtils.getGroupDoc(user, idGroup);
                if(groupDoc != null){
                    Collection collection = collectionRepository.findByIdAndGroupDocId(idCollection, idGroup);
                    if(collection != null){
                        List<Collection> collectionsList = collectionRepository.findByGroupDocId(idGroup);
                        List<CollectionTreeDto> collectionTreeDtoList = convertCollectionsToCollectionTreeDtoList(collectionsList);
                        CollectionTreeDto collectionTreeDto = new CollectionTreeDto();
                        collectionTreeDto.setCollectionName(groupDoc.getGroupName());
                        collectionTreeDto.setId(groupDoc.getId());
                        result.add(collectionTreeDto);
                        List<CollectionTreeDto> collectionTreeDtoList1 = getListCollectionTreeDtoList(collectionTreeDtoList, idCollection);
                        if(collectionTreeDtoList1 != null && collectionTreeDtoList1.size() > 0){
                            result.addAll(collectionTreeDtoList1);
                        }
                        return result;
                    }
                }
                return null;
            }

            if(idGroup != null){
                GroupDoc groupDoc = baseUtils.getGroupDoc(user, idGroup);
                if(groupDoc != null){
                    CollectionTreeDto collectionTreeDto = new CollectionTreeDto();
                    collectionTreeDto.setCollectionName(groupDoc.getGroupName());
                    collectionTreeDto.setId(groupDoc.getId());
                    result.add(collectionTreeDto);
                    return result;
                }
                return null;
            }

            if(idCollection != null){
                Collection collection = collectionRepository.findByIdAndUserId(idCollection, user.getId());
                if(collection != null){
                    List<Collection> collectionsList = collectionRepository.findByUserIdAndGroupDocId(user.getId(), null);
                    List<CollectionTreeDto> collectionTreeDtoList = convertCollectionsToCollectionTreeDtoList(collectionsList);
                    return  getListCollectionTreeDtoList(collectionTreeDtoList, idCollection);
                }
                return null;
            }
        }
        return null;
    }

    private List<CollectionTreeDto> getListCollectionTreeDtoList(List<CollectionTreeDto> collectionTreeDtoList, Long idCollection){
        List<CollectionTreeDto> result;
        for(CollectionTreeDto collectionTreeDto : collectionTreeDtoList){
            result = findArrayCollectionTreeDto(collectionTreeDto, idCollection);
            if( result != null && result.size() > 0){
                return result;
            }
        }
        return null;
    }

    private List<CollectionTreeDto> findArrayCollectionTreeDto(CollectionTreeDto collectionTreeDto, Long idCollection){
        if(Objects.equals(idCollection, collectionTreeDto.getId())){
            List<CollectionTreeDto> result = new ArrayList<>();
            result.add(collectionTreeDto);
            return result;
        }
        if(collectionTreeDto.getSubCollectionDtoList() != null && collectionTreeDto.getSubCollectionDtoList().size() > 0){
            for(CollectionTreeDto collectionTreeDto1 : collectionTreeDto.getSubCollectionDtoList()){
                List<CollectionTreeDto> result = findArrayCollectionTreeDto(collectionTreeDto1, idCollection);
                if( result != null && result.size() > 0){
                    List<CollectionTreeDto> result2 = new ArrayList<>();
                    result2.add(collectionTreeDto);
                    result2.addAll(result);
                    return result2;
                }

            }
        }
        return null;
    }

    private List<CollectionTreeDto> convertCollectionsToCollectionTreeDtoList(List<Collection> collectionsList) {
        List<CollectionTreeDto> collectionTreeDtoList = new ArrayList<>();
        if (collectionsList != null && collectionsList.size() > 0) {
            for (Collection collection : collectionsList) {
                if (collection.getParentCollectionId() == null) {
                    collectionTreeDtoList.add(modelMapperUtils.mapAllProperties(collection, CollectionTreeDto.class));
                }
            }
        }
        return collectionTreeDtoList;
    }

}
