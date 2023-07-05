package com.hust.edu.vn.services.impl.group;

import com.hust.edu.vn.dto.GroupDocDto;
import com.hust.edu.vn.entity.GroupHasDocument;
import com.hust.edu.vn.entity.GroupDoc;
import com.hust.edu.vn.entity.GroupShareUser;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.repository.CollectionRepository;
import com.hust.edu.vn.repository.GroupDocRepository;
import com.hust.edu.vn.repository.GroupHasDocumentRepository;
import com.hust.edu.vn.repository.GroupShareUserRepository;
import com.hust.edu.vn.services.document.DocumentService;
import com.hust.edu.vn.services.group.GroupDocService;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GroupDocServiceImpl implements GroupDocService {

    private final GroupDocRepository groupDocRepository;
    private final ModelMapperUtils modelMapperUtils;
    private final GroupShareUserRepository groupShareUserRepository;
    private final GroupHasDocumentRepository groupHasDocumentRepository;
    private final CollectionRepository collectionRepository;
    private final BaseUtils baseUtils;
    private final DocumentService documentService;

    public GroupDocServiceImpl(GroupDocRepository groupDocRepository, ModelMapperUtils modelMapperUtils, GroupShareUserRepository groupShareUserRepository, GroupHasDocumentRepository groupHasDocumentRepository, CollectionRepository collectionRepository, BaseUtils baseUtils, DocumentService documentService) {
        this.groupDocRepository = groupDocRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.groupShareUserRepository = groupShareUserRepository;
        this.groupHasDocumentRepository = groupHasDocumentRepository;
        this.collectionRepository = collectionRepository;
        this.baseUtils = baseUtils;
        this.documentService = documentService;
    }

    @Override
    public boolean createGroup(String groupName) {
        User user = baseUtils.getUser();
        if(user != null){
                GroupDoc groupDoc = new GroupDoc();
                groupDoc.setGroupName(groupName);
                groupDoc.setUser(user);
                groupDocRepository.save(groupDoc);
                return true;
        }
        return false;
    }

    @Override
    public List<GroupDocDto> showAllGroup() {
        User user = baseUtils.getUser();
        if(user != null){
            List<GroupDoc> groupDocsList = groupDocRepository.findAllByUser(user);
            if (groupDocsList != null && !groupDocsList.isEmpty()){
                List<GroupDocDto> groupDocDtoList = new ArrayList<>();
                for (GroupDoc groupDoc : groupDocsList){
                    GroupDocDto groupDocDto = modelMapperUtils.mapAllProperties(groupDoc, GroupDocDto.class);
                    groupDocDtoList.add(groupDocDto);
                }
                return groupDocDtoList;
            }
            return null;
        }
        return null;
    }

    @Override
    public GroupDocDto showGroupByGroupId(Long groupId) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                return modelMapperUtils.mapAllProperties(groupDoc, GroupDocDto.class);
            }
            return null;
        }
        return null;
    }

    @Override
    public boolean updateGroupByGroupId(Long groupId, String groupName) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                if(groupName.length() > 0){
                    groupDoc.setGroupName(groupName);
                    groupDocRepository.save(groupDoc);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean deleteGroupByGroupId(Long groupId) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = groupDocRepository.findByIdAndUser(groupId, user);
            if (groupDoc != null){
                List<GroupHasDocument> groupHasDocumentList = groupHasDocumentRepository.findAllByGroup(groupDoc);
                List<String> documentKeys = new ArrayList<>();
                if(groupHasDocumentList != null && !groupHasDocumentList.isEmpty()){
                    for(GroupHasDocument groupHasDocument : groupHasDocumentList){
                        documentKeys.add(groupHasDocument.getDocument().getDocumentKey());
                    }
                }
                documentService.moveToTrash(documentKeys);
                groupShareUserRepository.deleteByGroupId(groupDoc.getId());
                collectionRepository.deleteByGroupDocId(groupDoc.getId());
                groupDocRepository.delete(groupDoc);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public List<GroupDocDto> showAllGroupMember() {
        User user = baseUtils.getUser();
        if(user != null){
            List<GroupShareUser> groupShareUsers = groupShareUserRepository.findAllByUser(user);
            if(groupShareUsers != null && !groupShareUsers.isEmpty()){
                List<GroupDocDto> groupDocs = new ArrayList<>();
                for(GroupShareUser groupShareUser: groupShareUsers){
                    groupDocs.add(modelMapperUtils.mapAllProperties(groupShareUser.getGroup(), GroupDocDto.class));
                }
                return groupDocs;
            }
            return null;
        }
        return null;
    }

//    @Override
//    public GroupDocDto showGroupMemberByGroupId(Long groupId) {
//        User user = baseUtils.getUser();
//        if(user != null){
//            GroupShareUser groupShareUser = groupShareUserRepository.findByUserAndGroupId(user, groupId);
//            if(groupShareUser != null){
//                return modelMapperUtils.mapAllProperties(groupShareUser.getGroup(), GroupDocDto.class);
//            }
//            return null;
//        }
//        return null;
//    }
}
