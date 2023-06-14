package com.hust.edu.vn.services.impl.document;

import com.hust.edu.vn.dto.TypeDocumentDto;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.TypeDocument;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.repository.DocumentRepository;
import com.hust.edu.vn.repository.TypeDocumentRepository;
import com.hust.edu.vn.services.document.TypeDocumentService;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TypeDocumentImpl implements TypeDocumentService {

    private final TypeDocumentRepository typeDocumentRepository;
    private final BaseUtils baseUtils;
    private final DocumentRepository documentRepository;
    private final ModelMapperUtils modelMapperUtils;

    public TypeDocumentImpl(TypeDocumentRepository typeDocumentRepository, BaseUtils baseUtils,
                            DocumentRepository documentRepository, ModelMapperUtils modelMapperUtils) {
        this.typeDocumentRepository = typeDocumentRepository;
        this.baseUtils = baseUtils;
        this.documentRepository = documentRepository;
        this.modelMapperUtils = modelMapperUtils;
    }

    @Override
    public boolean addTypeDocument(String documentKey, String type) {
        User user = baseUtils.getUser();
        if(user != null){
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if(document != null){
                if(!typeDocumentRepository.existsByDocumentAndTypeName(document, type)){
                    TypeDocument typeDocument = new TypeDocument();
                    typeDocument.setDocument(document);
                    typeDocument.setTypeName(type);
                    typeDocumentRepository.save(typeDocument);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    @Override
    public List<TypeDocumentDto> showAllTypeDocument(String documentKey) {
        User user = baseUtils.getUser();
        if(user != null){
            Document document = documentRepository.findByDocumentKeyAndStatusDelete(documentKey, (byte) 0);
            if(document != null && (document.getUser().equals(user) || document.getDocsPublic() == 1)){
                List<TypeDocument> listTypeDocument = typeDocumentRepository.findAllByDocument(document);
                List<TypeDocumentDto> typeDocumentDtoList = new ArrayList<>();
                if(listTypeDocument != null && !listTypeDocument.isEmpty()){
                    for (TypeDocument typeDocument : listTypeDocument){
                        typeDocumentDtoList.add(modelMapperUtils.mapAllProperties(typeDocument, TypeDocumentDto.class));
                    }
                }
                return typeDocumentDtoList;
            }
            return null;
        }
        return null;
    }

    @Override
    public boolean updateTypeDocument(String documentKey, Long id, String typeName) {
        if(typeName == null || id == null) return false;
        User user = baseUtils.getUser();
        if(user != null){
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if(document != null){
                TypeDocument typeDocument = typeDocumentRepository.findById(id).orElse(null);
                if(typeDocument != null){
                    typeDocument.setTypeName(typeName);
                    typeDocument.setUpdatedAt(new Date());
                    typeDocumentRepository.save(typeDocument);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean deleteTypeDocument(String documentKey, Long id) {
        User user = baseUtils.getUser();
        if(user != null) {
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if(document != null){
                TypeDocument typeDocument = typeDocumentRepository.findById(id).orElse(null);
                if(typeDocument != null){
                    typeDocumentRepository.delete(typeDocument);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }
}
