package com.hust.edu.vn.services.impl.group;

import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.entity.*;
import com.hust.edu.vn.repository.CollectionRepository;
import com.hust.edu.vn.repository.DocumentRepository;
import com.hust.edu.vn.repository.GroupHasDocumentRepository;
import com.hust.edu.vn.services.document.DocumentService;
import com.hust.edu.vn.services.group.GroupCollectionHasDocumentService;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupHasDocumentServiceImpl implements GroupCollectionHasDocumentService {

    private final GroupHasDocumentRepository groupHasDocumentRepository;
    private final DocumentService documentService;
    private final BaseUtils baseUtils;
    private final CollectionRepository collectionRepository;
    private final ModelMapperUtils modelMapperUtils;
    private final DocumentRepository documentRepository;

    public GroupHasDocumentServiceImpl(GroupHasDocumentRepository groupHasDocumentRepository, DocumentService documentService, BaseUtils baseUtils,
                                       CollectionRepository collectionRepository, ModelMapperUtils modelMapperUtils,
                                       DocumentRepository documentRepository) {
        this.groupHasDocumentRepository = groupHasDocumentRepository;
        this.documentService = documentService;
        this.baseUtils = baseUtils;
        this.collectionRepository = collectionRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.documentRepository = documentRepository;
    }

    @Override
    public boolean createDocument(Long groupId, Long collectionId, MultipartFile file) {
        User user = baseUtils.getUser();
        if(user != null){
            Document document = documentService.uploadDocument(file);
            if (document != null) {
                if(groupId != null){
                    GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
                    if (groupDoc != null){
                        if(collectionId != null){
                            Collection collection = collectionRepository.findByIdAndGroupDoc(collectionId, groupDoc);
                            if(collection != null){
                                GroupHasDocument groupHasDocument = new GroupHasDocument();
                                groupHasDocument.setGroup(groupDoc);
                                groupHasDocument.setDocument(document);
                                documentRepository.save(document);
                                groupHasDocumentRepository.save(groupHasDocument);
                                return true;
                            }
                            return false;
                        }
                        GroupHasDocument groupHasDocument = new GroupHasDocument();
                        groupHasDocument.setGroup(groupDoc);
                        groupHasDocument.setDocument(document);
                        documentRepository.save(document);
                        groupHasDocumentRepository.save(groupHasDocument);
                        return true;
                    }
                    return false;
                }
                return false;
            }
            return false;
        }
        return false;
    }

//    @Override
//    public List<DocumentDto> showAllDocumentGroup(Long groupId) {
//        User user = baseUtils.getUser();
//        if (user != null) {
//            GroupDoc groupDoc = groupDocRepository.findByIdAndUser(groupId, user);
//            if(groupDoc != null){
//                List<GroupCollectionHasDocument> documents = groupCollectionHasDocumentRepository.findByGroupAndCollectionId(groupDoc, null);
//                List<DocumentDto> documentsList = new ArrayList<>();
//                if(documents != null && !documents.isEmpty()){
//                    for(GroupCollectionHasDocument document : documents){
//                        documentsList.add(modelMapperUtils.mapAllProperties(document.getDocument(), DocumentDto.class));
//                    }
//                }
//                return documentsList;
//            }
//            return null;
//        }
//        return null;
//    }

    @Override
    public List<DocumentDto> showAllDocumentACollection(Long groupId, Long collectionId) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                List<GroupHasDocument> groupHasDocumentList = groupHasDocumentRepository.findByGroup(groupDoc);
                List<DocumentDto> documentDtoList = new ArrayList<>();
                if(groupHasDocumentList != null && !groupHasDocumentList.isEmpty()){
                    for (GroupHasDocument groupHasDocument : groupHasDocumentList) {
                        documentDtoList.add(modelMapperUtils.mapAllProperties(groupHasDocument.getDocument(), DocumentDto.class));
                    }
                }
                return documentDtoList;
            }
            return null;
        }
        return null;
    }

    @Override
    public boolean deleteDocumentGroup(Long groupId, Long collectionId, List<String> documentsKey) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if (groupDoc != null){
                for(String documentKey : documentsKey){
                    Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
                    if(document != null){
                        GroupHasDocument groupHasDocument = groupHasDocumentRepository.findByGroupAndDocumentId(groupDoc, document.getId());
                        if(groupHasDocument != null){
                            groupHasDocumentRepository.delete(groupHasDocument);
                        }
                    }
                }
                documentService.moveToTrash(documentsKey);
                return true;
            }
            return false;
        }
        return false;
    }

}
