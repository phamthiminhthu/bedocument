package com.hust.edu.vn.services.impl.group;

import com.hust.edu.vn.dto.CollectionDto;
import com.hust.edu.vn.entity.*;
import com.hust.edu.vn.model.CollectionModel;
import com.hust.edu.vn.repository.CollectionHasDocumentRepository;
import com.hust.edu.vn.repository.CollectionRepository;
import com.hust.edu.vn.repository.DocumentRepository;
import com.hust.edu.vn.repository.GroupHasDocumentRepository;
import com.hust.edu.vn.services.group.GroupHasCollectionService;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Service
@Slf4j
public class GroupHasCollectionServiceImpl implements GroupHasCollectionService {
    private final DocumentRepository documentRepository;
    private final GroupHasDocumentRepository groupHasDocumentRepository;
    private final BaseUtils baseUtils;
    private final ModelMapperUtils modelMapperUtils;
    private final CollectionRepository collectionRepository;
    private final CollectionHasDocumentRepository collectionHasDocumentRepository;

    public GroupHasCollectionServiceImpl(GroupHasDocumentRepository groupHasDocumentRepository, BaseUtils baseUtils, ModelMapperUtils modelMapperUtils, CollectionRepository collectionRepository,
                                         CollectionHasDocumentRepository collectionHasDocumentRepository,
                                         DocumentRepository documentRepository) {
        this.groupHasDocumentRepository = groupHasDocumentRepository;
        this.baseUtils = baseUtils;
        this.modelMapperUtils = modelMapperUtils;
        this.collectionRepository = collectionRepository;
        this.collectionHasDocumentRepository = collectionHasDocumentRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    public boolean createCollectionGroupDoc(Long groupId, CollectionModel collectionModel) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                if(collectionRepository.existsByGroupDocAndCollectionNameAndParentCollectionId(groupDoc, collectionModel.getCollectionName(), collectionModel.getParentCollectionId())){
                    return false;
                }
                if(collectionModel.getParentCollectionId() == null || collectionRepository.existsByIdAndGroupDoc(collectionModel.getParentCollectionId(), groupDoc )){
                    Collection collection = modelMapperUtils.mapAllProperties(collectionModel, Collection.class);
                    collection.setUser(user);
                    collection.setGroupDoc(groupDoc);
                    collectionRepository.save(collection);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    @Override
    public TreeMap<Long, List<CollectionDto>> showAllCollectionGroupDoc(Long groupId) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                List<Collection> collectionList = collectionRepository.findAllByGroupDocId(groupId);
                TreeMap<Long, List<CollectionDto>> result = new TreeMap<>();
                if(collectionList != null && !collectionList.isEmpty()){
                    for (Collection collection : collectionList){
                        Long idParent = collection.getParentCollectionId();
                        if(idParent != null){
                            collectionRepository.findById(idParent).ifPresent(collectionParent -> result.computeIfAbsent(collectionParent.getId(), k -> new ArrayList<>()).add(modelMapperUtils.mapAllProperties(collection, CollectionDto.class)));
                        }else{
                            result.put(collection.getId(), null);
                        }
                    }
                    return result;
                }
                return null;
            }
            return null;
        }
        return null;
    }

    @Override
    public boolean updateCollectionGroupDoc(Long groupId, Long collectionId, String collectionName) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                Collection collection = collectionRepository.findByIdAndGroupDoc(collectionId, groupDoc);
                if(collection != null) {
                    if(collection.getCollectionName().equals(collectionName)){
                        return true;
                    }
                    if (collectionRepository.existsByGroupDocAndCollectionNameAndParentCollectionId(groupDoc, collectionName, collection.getParentCollectionId())) {
                        return false;
                    }
                    if (collectionName != null) {
                        collection.setCollectionName(collectionName);
                        collectionRepository.save(collection);
                        return true;
                    }
                }
                return false;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean deleteCollectionGroupDoc(Long groupId, Long collectionId) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                Collection collection = collectionRepository.findByIdAndGroupDoc(collectionId, groupDoc);
                if(collection != null){
                    List<CollectionHasDocument> collectionHasDocuments = collectionHasDocumentRepository.findByCollection(collection);
                    if(collectionHasDocuments != null && !collectionHasDocuments.isEmpty()){
                        for(CollectionHasDocument collectionHasDocument : collectionHasDocuments){
                            GroupHasDocument groupHasDocument = groupHasDocumentRepository.findByDocumentAndGroupId(collectionHasDocument.getDocument(), groupId);
                            if(groupHasDocument != null){
                                groupHasDocumentRepository.delete(groupHasDocument);
                            }

                        }
                        collectionHasDocumentRepository.deleteAll(collectionHasDocuments);
                    }
                    List<Collection> collectionList = collectionRepository.findAllByParentCollectionIdAndGroupDocId(collection.getId(), groupId);
                    if(collectionList != null && !collectionList.isEmpty()){
                        for (Collection collection1 : collectionList){
                            deleteCollectionGroupDoc(groupId, collection1.getId());
                        }
                    }
                    collectionRepository.delete(collection);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    @Override
    public List<CollectionDto> showAllCollectionInGroup(Long groupId) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                List<CollectionDto> collectionDtoList = new ArrayList<>();
                List<Collection> collectionList = collectionRepository.findAllByGroupDocId(groupId);
                if(collectionList != null && !collectionList.isEmpty()){
                    for(Collection collection : collectionList){
                        CollectionDto collectionDto = modelMapperUtils.mapAllProperties(collection, CollectionDto.class);
                        if(collectionDto.getParentCollectionId() != null){
                            Collection collectionParent = collectionRepository.findByIdAndGroupDoc(collectionDto.getParentCollectionId(), groupDoc);
                            collectionDto.setParentCollectionName(collectionParent.getCollectionName());
                        }
                        collectionDtoList.add(collectionDto);
                    }
                }
                return collectionDtoList;
            }
        }
        return null;
    }

    @Override
    public boolean moveDocumentsToCollection(Long groupId, List<Long> idCollections, List<String> documentKeys) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                if(idCollections != null && idCollections.size() > 0){
                    for(Long id: idCollections){
                        Collection collection = collectionRepository.findByIdAndGroupDoc(id, groupDoc);
                        if(collection != null){
                          if(documentKeys != null && documentKeys.size() > 0){
                              for(String documentKey :  documentKeys){
                                  Document document = documentRepository.findByDocumentKeyAndStatusDelete(documentKey, (byte) 0);
                                  if(document != null && !collectionHasDocumentRepository.existsByCollectionAndDocument(collection, document)){
                                      CollectionHasDocument collectionHasDocument = new CollectionHasDocument();
                                      collectionHasDocument.setDocument(document);
                                      collectionHasDocument.setCollection(collection);
                                      collectionHasDocumentRepository.save(collectionHasDocument);
                                  }
                              }
                          }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }


}
