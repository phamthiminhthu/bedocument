package com.hust.edu.vn.services.impl.group;

import com.hust.edu.vn.dto.CollectionDto;
import com.hust.edu.vn.entity.*;
import com.hust.edu.vn.model.CollectionModel;
import com.hust.edu.vn.repository.CollectionHasDocumentRepository;
import com.hust.edu.vn.repository.CollectionRepository;
import com.hust.edu.vn.repository.GroupDocRepository;
import com.hust.edu.vn.repository.GroupHasDocumentRepository;
import com.hust.edu.vn.services.group.GroupCollectionHasDocumentService;
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
    private final GroupHasDocumentRepository groupHasDocumentRepository;
    private final BaseUtils baseUtils;
    private final GroupDocRepository groupDocRepository;
    private final ModelMapperUtils modelMapperUtils;
    private final CollectionRepository collectionRepository;
    private final CollectionHasDocumentRepository collectionHasDocumentRepository;
    private final GroupCollectionHasDocumentService groupCollectionHasDocumentService;

    public GroupHasCollectionServiceImpl(GroupHasDocumentRepository groupHasDocumentRepository, BaseUtils baseUtils, GroupDocRepository groupDocRepository, ModelMapperUtils modelMapperUtils, CollectionRepository collectionRepository,
                                         CollectionHasDocumentRepository collectionHasDocumentRepository, GroupCollectionHasDocumentService groupCollectionHasDocumentService) {
        this.groupHasDocumentRepository = groupHasDocumentRepository;
        this.baseUtils = baseUtils;
        this.groupDocRepository = groupDocRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.collectionRepository = collectionRepository;
        this.collectionHasDocumentRepository = collectionHasDocumentRepository;
        this.groupCollectionHasDocumentService = groupCollectionHasDocumentService;
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
                if(collectionModel.getParentCollectionId() == null || collectionRepository.existsByIdAndUserId(collectionModel.getParentCollectionId(), user.getId())){
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
    public boolean updateCollectionGroupDoc(Long groupId, Long collectionId, CollectionModel collectionModel) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                Collection collection = collectionRepository.findByIdAndGroupDoc(collectionId, groupDoc);
                Collection collectionParent = collectionRepository.findByIdAndGroupDoc(collectionModel.getParentCollectionId(), groupDoc);
                if(collection != null && collectionParent != null && collectionModel.getCollectionName() != null){
                    collection.setCollectionName(collectionModel.getCollectionName());
                    collection.setParentCollectionId(collectionModel.getParentCollectionId());
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
    public boolean deleteCollectionGroupDoc(Long groupId, Long collectionId) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                Collection collection = collectionRepository.findByIdAndGroupDoc(collectionId, groupDoc);
                if(collection != null){
                    collectionRepository.delete(collection);
                    List<GroupHasDocument> groupHasDocumentList = groupHasDocumentRepository.findByGroup(groupDoc);
                    List<String> documentKeys = new ArrayList<>();
                    for (GroupHasDocument groupHasDocument : groupHasDocumentList){
                        documentKeys.add(groupHasDocument.getDocument().getDocumentKey());
                    }
                    groupCollectionHasDocumentService.deleteDocumentGroup(groupId, collectionId, documentKeys);
                    while(collection.getParentCollectionId() != null){
                            collection = collectionRepository.findByIdAndGroupDoc(collection.getParentCollectionId(),groupDoc);
                            collectionRepository.delete(collection);
                            groupCollectionHasDocumentService.deleteDocumentGroup(groupId, collectionId, documentKeys);

                    }
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }
}
