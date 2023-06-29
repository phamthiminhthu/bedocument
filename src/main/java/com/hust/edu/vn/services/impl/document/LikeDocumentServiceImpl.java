package com.hust.edu.vn.services.impl.document;

import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.LikeDocument;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.repository.DocumentRepository;
import com.hust.edu.vn.repository.LikeDocumentRepository;
import com.hust.edu.vn.services.document.LikeDocumentService;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LikeDocumentServiceImpl implements LikeDocumentService {

    private final LikeDocumentRepository likeDocumentRepository;
    private final DocumentRepository documentRepository;
    private final BaseUtils baseUtils;
    private final ModelMapperUtils modelMapperUtils;

    public LikeDocumentServiceImpl(LikeDocumentRepository likeDocumentRepository,
                                   DocumentRepository documentRepository, BaseUtils baseUtils, ModelMapperUtils modelMapperUtils) {
        this.likeDocumentRepository = likeDocumentRepository;
        this.documentRepository = documentRepository;
        this.baseUtils = baseUtils;
        this.modelMapperUtils = modelMapperUtils;
    }

    @Override
    public boolean likeDocument(String documentKey) {
        if(documentKey == null) return false;
        Document document = documentRepository.findByDocumentKeyAndStatusDelete(documentKey, (byte) 0);
        if(document != null){
            User user = baseUtils.getUser();
            if(user != null && document.getDocsPublic() == 1){
                if(!likeDocumentRepository.existsByUserAndDocument(user, document)){
                    LikeDocument likeDocument = new LikeDocument();
                    likeDocument.setDocument(document);
                    likeDocument.setUser(user);
                    document.setQuantityLike(document.getQuantityLike() + 1L);
                    likeDocumentRepository.save(likeDocument);
                    documentRepository.save(document);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    @Override
    public List<UserDto> showAllUserLikeDocument(String documentKey) {
        if(documentKey == null) return null;
        Document document = documentRepository.findByDocumentKeyAndStatusDelete(documentKey, (byte) 0);
        if(document != null && document.getDocsPublic() == 1){
            List<LikeDocument> likeDocuments = likeDocumentRepository.findAllByDocument(document);
            List<UserDto> userDtoList = new ArrayList<>();
            if(likeDocuments != null && !likeDocuments.isEmpty()){
                for(LikeDocument likeDocument : likeDocuments){
                    userDtoList.add(modelMapperUtils.mapAllProperties(likeDocument.getUser(), UserDto.class));
                }
            }
            return userDtoList;
        }
        return null;
    }

    @Override
    public boolean unlikeDocument(String documentKey) {
        if(documentKey == null) return false;
        Document document = documentRepository.findByDocumentKeyAndStatusDelete(documentKey, (byte) 0);
        if(document != null && document.getDocsPublic() == 1){
            User user = baseUtils.getUser();
            if(user != null){
                if(likeDocumentRepository.existsByUserAndDocument(user, document)){
                    LikeDocument likeDocument = likeDocumentRepository.findByUserAndDocument(user, document);
                    document.setQuantityLike(document.getQuantityLike() - 1L);
                    likeDocumentRepository.delete(likeDocument);
                    documentRepository.save(document);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

}
