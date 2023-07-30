package com.hust.edu.vn.services.impl.group;

import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.entity.*;
import com.hust.edu.vn.repository.CollectionHasDocumentRepository;
import com.hust.edu.vn.repository.CollectionRepository;
import com.hust.edu.vn.repository.DocumentRepository;
import com.hust.edu.vn.repository.GroupHasDocumentRepository;
import com.hust.edu.vn.services.document.DocumentService;
import com.hust.edu.vn.services.group.GroupHasDocumentService;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupHasDocumentServiceImpl implements GroupHasDocumentService {

    private final GroupHasDocumentRepository groupHasDocumentRepository;
    private final DocumentService documentService;
    private final BaseUtils baseUtils;
    private final CollectionRepository collectionRepository;
    private final ModelMapperUtils modelMapperUtils;
    private final DocumentRepository documentRepository;
    private final CollectionHasDocumentRepository collectionHasDocumentRepository;

    public GroupHasDocumentServiceImpl(GroupHasDocumentRepository groupHasDocumentRepository, DocumentService documentService, BaseUtils baseUtils,
                                       CollectionRepository collectionRepository, ModelMapperUtils modelMapperUtils,
                                       DocumentRepository documentRepository,
                                       CollectionHasDocumentRepository collectionHasDocumentRepository) {
        this.groupHasDocumentRepository = groupHasDocumentRepository;
        this.documentService = documentService;
        this.baseUtils = baseUtils;
        this.collectionRepository = collectionRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.documentRepository = documentRepository;
        this.collectionHasDocumentRepository = collectionHasDocumentRepository;
    }

    @Override
    public boolean createDocument(Long groupId, Long collectionId, MultipartFile file) {
        User user = baseUtils.getUser();
        if (user != null) {
            DocumentDto documentDto = documentService.uploadDocument(file);
            if (documentDto != null) {
                Document document = modelMapperUtils.mapAllProperties(documentDto, Document.class);
                if (groupId != null) {
                    GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
                    if (groupDoc != null) {
                        if (collectionId != null) {
                            Collection collection = collectionRepository.findByIdAndGroupDoc(collectionId, groupDoc);
                            if (collection != null) {
                                GroupHasDocument groupHasDocument = new GroupHasDocument();
                                groupHasDocument.setGroup(groupDoc);
                                groupHasDocument.setDocument(document);
                                documentRepository.save(document);
                                groupHasDocumentRepository.save(groupHasDocument);
                                CollectionHasDocument collectionHasDocument = new CollectionHasDocument();
                                collectionHasDocument.setCollection(collection);
                                collectionHasDocument.setDocument(document);
                                collectionHasDocumentRepository.save(collectionHasDocument);
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

    @Override
    public List<DocumentDto> showAllDocumentACollection(Long groupId, Long collectionId) {
        User user = baseUtils.getUser();
        if (user != null) {
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if (groupDoc != null) {
                List<GroupHasDocument> groupHasDocumentList = groupHasDocumentRepository.findByGroup(groupDoc);
                List<DocumentDto> documentDtoList = new ArrayList<>();
                if (groupHasDocumentList != null && !groupHasDocumentList.isEmpty()) {
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
        if (user != null) {
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if (groupDoc != null) {
                for (String documentKey : documentsKey) {
                    Document document = documentRepository.findByDocumentKeyAndStatusDelete(documentKey, (byte) 0);
                    if (document != null) {
                       if(collectionId == null){
                           GroupHasDocument groupHasDocument = groupHasDocumentRepository.findByGroupAndDocumentId(groupDoc, document.getId());
                           if (groupHasDocument != null) {
                               groupHasDocumentRepository.delete(groupHasDocument);
                           }
                           List<CollectionHasDocument> collectionHasDocuments = collectionHasDocumentRepository.findByDocument(document);
                           if (collectionHasDocuments != null && collectionHasDocuments.size() > 0) {
                               collectionHasDocumentRepository.deleteAll(collectionHasDocuments);
                           }
                       }else{
                           if(!collectionHasDocumentRepository.existsMultipleCollectionHasDocumentsByDocumentAndGroup(document, groupDoc)){
                               GroupHasDocument groupHasDocument = groupHasDocumentRepository.findByGroupAndDocumentId(groupDoc, document.getId());
                               if (groupHasDocument != null) {
                                   groupHasDocumentRepository.delete(groupHasDocument);
                               }
                           }
                           CollectionHasDocument collectionHasDocument = collectionHasDocumentRepository.findByDocumentAndCollectionId(document, collectionId);
                           if (collectionHasDocument != null) {
                               collectionHasDocumentRepository.delete(collectionHasDocument);
                           }
                       }

                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean moveDocumentToGroup(List<Long> idGroups, List<String> listDocumentKeys) {
        User user = baseUtils.getUser();
        if (user != null) {
            if (idGroups != null && idGroups.size() > 0) {
                for (Long groupId : idGroups) {
                    GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
                    if (groupDoc != null) {
                        if (listDocumentKeys != null && listDocumentKeys.size() > 0) {
                            for (String key : listDocumentKeys) {
                                Document document = documentRepository.findByDocumentKeyAndStatusDelete(key, (byte) 0);
                                if (document != null) {
                                    if (!groupHasDocumentRepository.existsByDocumentAndGroupId(document, groupDoc.getId())) {
                                        GroupHasDocument groupHasDocument = new GroupHasDocument();
                                        groupHasDocument.setDocument(document);
                                        groupHasDocument.setGroup(groupDoc);
                                        groupHasDocumentRepository.save(groupHasDocument);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

}
