package com.hust.edu.vn.services.impl.group;

import com.hust.edu.vn.dto.CollectionDto;
import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.dto.GroupDocDto;
import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.entity.*;
import com.hust.edu.vn.repository.*;
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
    private final TokenInviteGroupRepository tokenInviteGroupRepository;

    private final GroupDocRepository groupDocRepository;
    private final ModelMapperUtils modelMapperUtils;
    private final GroupShareUserRepository groupShareUserRepository;
    private final GroupHasDocumentRepository groupHasDocumentRepository;
    private final CollectionRepository collectionRepository;
    private final BaseUtils baseUtils;
    private final DocumentService documentService;

    public GroupDocServiceImpl(GroupDocRepository groupDocRepository, ModelMapperUtils modelMapperUtils, GroupShareUserRepository groupShareUserRepository, GroupHasDocumentRepository groupHasDocumentRepository, CollectionRepository collectionRepository, BaseUtils baseUtils, DocumentService documentService,
                               TokenInviteGroupRepository tokenInviteGroupRepository) {
        this.groupDocRepository = groupDocRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.groupShareUserRepository = groupShareUserRepository;
        this.groupHasDocumentRepository = groupHasDocumentRepository;
        this.collectionRepository = collectionRepository;
        this.baseUtils = baseUtils;
        this.documentService = documentService;
        this.tokenInviteGroupRepository = tokenInviteGroupRepository;
    }

    @Override
    public boolean createGroup(String groupName) {
        User user = baseUtils.getUser();
        if (user != null) {
            GroupDoc groupDoc = new GroupDoc();
            groupDoc.setGroupName(groupName);
            groupDoc.setUser(user);
            groupDocRepository.save(groupDoc);
            return true;
        }
        return false;
    }

    @Override
    public List<GroupDocDto> showAllGroupByOwner() {
        User user = baseUtils.getUser();
        if (user != null) {
            List<GroupDoc> groupDocsList = groupDocRepository.findAllByUser(user);
            List<GroupDocDto> groupDocDtoList = new ArrayList<>();
            if (groupDocsList != null && !groupDocsList.isEmpty()) {
                for (GroupDoc groupDoc : groupDocsList) {
                    GroupDocDto groupDocDto = modelMapperUtils.mapAllProperties(groupDoc, GroupDocDto.class);
                    groupDocDto.setStatusOwner((byte) 1);
                    groupDocDtoList.add(groupDocDto);
                }
            }
            return groupDocDtoList;
        }
        return null;
    }

    @Override
    public String showGroupNameById(Long groupId) {
        User user = baseUtils.getUser();
        if (user != null) {
            GroupDoc groupDoc = groupDocRepository.findById(groupId).orElse(null);
            if (groupDoc != null) {
                return groupDoc.getGroupName();
            }
        }
        return null;
    }

    @Override
    public GroupDocDto showGroupByGroupId(Long groupId) {
        User user = baseUtils.getUser();
        if (user != null) {
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if (groupDoc != null) {
                GroupDocDto groupDocDto = modelMapperUtils.mapAllProperties(groupDoc, GroupDocDto.class);
                List<Collection> collectionList = collectionRepository.findAllByGroupDocIdAndParentCollectionId(groupId, null);
                List<CollectionDto> collectionDtoList = new ArrayList<>();
                if (collectionList != null && !collectionList.isEmpty()) {
                    for (Collection collection : collectionList) {
                        collectionDtoList.add(modelMapperUtils.mapAllProperties(collection, CollectionDto.class));
                    }
                }
                groupDocDto.setCollectionDtoList(collectionDtoList);
                List<GroupHasDocument> groupHasDocumentList = groupHasDocumentRepository.findAllByGroupId(groupId);
                List<DocumentDto> documentDtoList = new ArrayList<>();
                if (groupHasDocumentList != null && !groupHasDocumentList.isEmpty()) {
                    for (GroupHasDocument groupHasDocument : groupHasDocumentList) {
                        DocumentDto documentDto = documentService.getDocumentModel(groupHasDocument.getDocument().getDocumentKey());
                        if (documentDto != null) {
                            documentDtoList.add(documentDto);
                        }
                    }
                }
                groupDocDto.setDocumentDtoList(documentDtoList);
                List<UserDto> userDtoList = new ArrayList<>();
                List<GroupShareUser> groupShareUsers = groupShareUserRepository.findAllByGroupId(groupId);
                if (groupShareUsers != null && groupShareUsers.size() > 0) {
                    for (GroupShareUser groupShareUser : groupShareUsers) {
                        User groupUser = groupShareUser.getUser();
                        if (groupUser != null) {
                            userDtoList.add(modelMapperUtils.mapAllProperties(user, UserDto.class));
                        }
                    }
                    groupDocDto.setUserDtoList(userDtoList);
                }
                if (user == groupDoc.getUser()) {
                    groupDocDto.setStatusOwner((byte) 1);
                }
                return groupDocDto;
            }
            return null;
        }
        return null;
    }

    @Override
    public boolean updateGroupByGroupId(Long groupId, String groupName) {
        User user = baseUtils.getUser();
        if (user != null) {
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if (groupDoc != null) {
                if (groupName.length() > 0) {
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
        if (user != null) {
            GroupDoc groupDoc = groupDocRepository.findByIdAndUser(groupId, user);
            if (groupDoc != null) {
                groupHasDocumentRepository.deleteByGroupId(groupDoc.getId());
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
        if (user != null) {
            List<GroupShareUser> groupShareUsers = groupShareUserRepository.findAllByUser(user);
            List<GroupDocDto> groupDocs = new ArrayList<>();
            if (groupShareUsers != null && !groupShareUsers.isEmpty()) {
                for (GroupShareUser groupShareUser : groupShareUsers) {
                    groupDocs.add(modelMapperUtils.mapAllProperties(groupShareUser.getGroup(), GroupDocDto.class));
                }
            }
            return groupDocs;
        }
        return null;
    }

    @Override
    public List<GroupDocDto> getALLGroups() {
        User user = baseUtils.getUser();
        if (user != null) {
            List<GroupDocDto> listGroupsByMember = showAllGroupMember();
            List<GroupDocDto> listGroupsByOwner = showAllGroupByOwner();
            listGroupsByOwner.addAll(listGroupsByMember);
            return listGroupsByOwner;
        }
        return null;
    }
}
