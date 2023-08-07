package com.hust.edu.vn.utils;

import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.dto.TagDto;
import com.hust.edu.vn.dto.TypeDocumentDto;
import com.hust.edu.vn.dto.UrlDto;
import com.hust.edu.vn.entity.*;
import com.hust.edu.vn.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class BaseUtils {
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final GroupDocRepository groupDocRepository;
    private final GroupShareUserRepository groupShareUserRepository;

    private final ModelMapperUtils modelMapperUtils;
    private final TagRepository tagRepository;
    private final TypeDocumentRepository typeDocumentRepository;
    private final UrlRepository urlRepository;

    public BaseUtils(UserRepository userRepository, DocumentRepository documentRepository, GroupDocRepository groupDocRepository,
                     GroupShareUserRepository groupShareUserRepository, ModelMapperUtils modelMapperUtils, TagRepository tagRepository, TypeDocumentRepository typeDocumentRepository, UrlRepository urlRepository) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.groupDocRepository = groupDocRepository;
        this.groupShareUserRepository = groupShareUserRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.tagRepository = tagRepository;
        this.typeDocumentRepository = typeDocumentRepository;
        this.urlRepository = urlRepository;
    }

    public User getUser(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email);
    }

    public boolean checkDocument(List<String> listDocumentKey, byte statusDelete){
        User user = getUser();
        if(user != null){
            for(String keyName : listDocumentKey){
                Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(keyName, user, statusDelete);
                if(document == null){
                    return false;
                }
            }
            return true;
        }
        return false;

    }

    public GroupDoc getGroupDoc(User user, Long groupId){
        GroupDoc groupDoc = groupDocRepository.findByIdAndUser(groupId, user);
        if(groupDoc == null){
            GroupShareUser groupShareUser = groupShareUserRepository.findByUserAndGroupId(user, groupId);
            if(groupShareUser != null){
                return groupShareUser.getGroup();
            }
            return null;
        }
        return groupDoc;
    }

//    public DocumentDto getDocumentDto(Document document){
//
//        if (document != null){
//            DocumentDto documentModel = modelMapperUtils.mapAllProperties(document, DocumentDto.class);
//            List<TagDto> tagDtoList = new ArrayList<>();
//            List<Tag> tagList = tagRepository.findAllByDocument(document);
//            if (tagList != null && !tagList.isEmpty()) {
//                for (Tag tag : tagList) {
//                    tagDtoList.add(modelMapperUtils.mapAllProperties(tag, TagDto.class));
//                }
//            }
//            List<TypeDocument> listTypeDocument = typeDocumentRepository.findAllByDocument(document);
//            List<TypeDocumentDto> typeDocumentDtoList = new ArrayList<>();
//            if(listTypeDocument != null && !listTypeDocument.isEmpty()){
//                for (TypeDocument typeDocument : listTypeDocument){
//                    typeDocumentDtoList.add(modelMapperUtils.mapAllProperties(typeDocument, TypeDocumentDto.class));
//                }
//            }
//
//            List<Url> urlList = urlRepository.findAllByDocument(document);
//            List<UrlDto> urlDtoList = new ArrayList<>();
//            if(urlList != null && !urlList.isEmpty()){
//                for(Url url : urlList){
//                    urlDtoList.add(modelMapperUtils.mapAllProperties(url, UrlDto.class));
//                }
//            }
//            documentModel.setTagDtoList(tagDtoList);
//            documentModel.setTypeDocumentsList(typeDocumentDtoList);
//            documentModel.setUrls(urlDtoList);
//            return documentModel;
//        }
//        return null;
//    }


    public List<DocumentDto> getListDocumentsDto(List<Document> documents){
        List<DocumentDto> documentDtoList = new ArrayList<>();
        if(documents != null &&  !documents.isEmpty()){
            for (Document document : documents){
                DocumentDto documentDto = modelMapperUtils.mapAllProperties(document, DocumentDto.class);
                documentDtoList.add(documentDto);
            }
        }
        return documentDtoList;
    }


}
