package com.hust.edu.vn.services.impl.collection;

import com.hust.edu.vn.dto.CollectionDto;
import com.hust.edu.vn.dto.CollectionHasDocumentDto;
import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.entity.Collection;
import com.hust.edu.vn.entity.CollectionHasDocument;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.repository.CollectionHasDocumentRepository;
import com.hust.edu.vn.repository.CollectionRepository;
import com.hust.edu.vn.repository.DocumentRepository;
import com.hust.edu.vn.services.collection.CollectionHasDocumentService;
import com.hust.edu.vn.services.document.DocumentService;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class CollectionHasDocumentImpl implements CollectionHasDocumentService {
    private final DocumentRepository documentRepository;
    private final CollectionRepository collectionRepository;
    private final CollectionHasDocumentRepository collectionHasDocumentRepository;
    private final DocumentService documentService;

    private final BaseUtils baseUtils;
    private final ModelMapperUtils modelMapperUtils;

    public CollectionHasDocumentImpl(CollectionHasDocumentRepository collectionHasDocumentRepository, DocumentService documentService,
                                     CollectionRepository collectionRepository, ModelMapperUtils modelMapperUtils,
                                     DocumentRepository documentRepository, BaseUtils baseUtils) {
        this.collectionHasDocumentRepository = collectionHasDocumentRepository;
        this.documentService = documentService;
        this.baseUtils = baseUtils;
        this.collectionRepository = collectionRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.documentRepository = documentRepository;
    }

    @Override
    public boolean createDocumentCollection(Long collectionId, MultipartFile file) {
        User user = baseUtils.getUser();
        if(user != null){
            DocumentDto documentDto = documentService.uploadDocument(file);
            if(documentDto != null){
                CollectionHasDocument collectionHasDocument = new CollectionHasDocument();
                Collection collection = collectionRepository.findByIdAndUserId(collectionId, user.getId());
                if(collection != null){
                    collectionHasDocument.setCollection(collection);
                    collectionHasDocument.setDocument(modelMapperUtils.mapAllProperties(documentDto, Document.class));
                    collectionHasDocumentRepository.save(collectionHasDocument);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    @Override
    public CollectionHasDocumentDto getDocumentCollection(Long collectionId) {
        User user = baseUtils.getUser();
        if(user != null){
            Collection collection = collectionRepository.findByIdAndUserId(collectionId, user.getId());
            if(collection != null){
                List<CollectionHasDocument> collectionHasDocument = collectionHasDocumentRepository.findByCollectionIdAndDocumentStatusDelete(collectionId, (byte) 0);
                if(collectionHasDocument != null && collectionHasDocument.size() > 0){
                    List<DocumentDto> documents = new ArrayList<>();
                    for(CollectionHasDocument document : collectionHasDocument){
                        DocumentDto model = modelMapperUtils.mapAllProperties(document.getDocument(), DocumentDto.class);
                        documents.add(model);
                    }

                    return new CollectionHasDocumentDto(modelMapperUtils.mapAllProperties(collection, CollectionDto.class), documents);
                }
                return null;
            }
            return null;
        }
        return null;
    }

    @Override
    public boolean updateCollectionDocument(Long oldCollectionId, Long newCollectionId, String documentKey) {
        User user = baseUtils.getUser();
        if(user != null){
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if(document != null){
                Collection collection = collectionRepository.findByIdAndUserId(oldCollectionId, user.getId());
                Collection newCollection = collectionRepository.findByIdAndUserId(newCollectionId, user.getId());
                if(collection != null && newCollection != null){
                    CollectionHasDocument collectionHasDocument = collectionHasDocumentRepository.findByCollectionAndDocument(collection, document);
                    if(collectionHasDocument != null & !collectionHasDocumentRepository.existsByCollectionAndDocument(newCollection, document)){
                        collectionHasDocument.setCollection(newCollection);
                        collectionHasDocumentRepository.save(collectionHasDocument);
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
    public boolean deleteCollectionDocument(Long collectionId, String documentKey) {
        User user = baseUtils.getUser();
        if(user != null){
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            Collection collection = collectionRepository.findByIdAndUserId(collectionId, user.getId());
            if(document != null && collection != null){
                if(collectionHasDocumentRepository.existsByCollectionAndDocument(collection, document)){
                    CollectionHasDocument collectionHasDocument = collectionHasDocumentRepository.findByCollectionAndDocument(collection, document);
                    collectionHasDocumentRepository.delete(collectionHasDocument);
//                    documentRepository.save(document);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean moveDocumentToCollection(List<Long> idCollections, List<String> documentKeys) {
        User user = baseUtils.getUser();
        if(user != null) {
            if (idCollections != null && !idCollections.isEmpty()) {
                for (Long idCollection : idCollections) {
                    Collection collection = collectionRepository.findByIdAndUser(idCollection, user);
                    if (collection != null) {
                        if (documentKeys != null && !documentKeys.isEmpty()) {
                            for (String documentKey : documentKeys) {
                                Document document = documentRepository.findByDocumentKeyAndStatusDelete(documentKey, (byte) 0);
                                if (document != null) {
                                    if (!collectionHasDocumentRepository.existsByCollectionAndDocument(collection, document)) {
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
            }
            return true;
        }
        return false;
    }

//    @Override
//    public boolean moveDocumentCollection(Long collectionId, List<String> listDocumentKey, List<Long> listCollectionId) {
//        User user =  baseUtils.getUser();
//        if(user != null){
//            boolean check = baseUtils.checkDocument(listDocumentKey, (byte) 0);
//            if(check){
//                for(String key : listDocumentKey){
//                    for(Long id : listCollectionId){
//                        if(!collectionHasDocumentRepository.existsByDocumentDocumentKeyAndCollectionIdAndCollectionUser(key, id, user)){
//                            Collection collection = collectionRepository.findByIdAndUserId(id, user.getId());
//                            if(collection != null){
//                                CollectionHasDocument collectionHasDocument = new CollectionHasDocument();
//                                Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(key, user, (byte) 0);
//                                collectionHasDocument.setCollection(collection);
//                                collectionHasDocument.setDocument(document);
//                                collectionHasDocumentRepository.save(collectionHasDocument);
//                            }
//                        }
//                    }
//                }
//                return true;
//            }
//            return false;
//        }
//        return false;
//    }

}
