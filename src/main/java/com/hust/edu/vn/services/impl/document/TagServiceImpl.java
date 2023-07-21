package com.hust.edu.vn.services.impl.document;

import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.dto.TagDto;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.Tag;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.repository.DocumentRepository;
import com.hust.edu.vn.repository.LikeDocumentRepository;
import com.hust.edu.vn.repository.TagRepository;
import com.hust.edu.vn.services.document.TagService;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class TagServiceImpl implements TagService {
    private final LikeDocumentRepository likeDocumentRepository;

    private final BaseUtils baseUtils;
    private final DocumentRepository documentRepository;
    private final TagRepository tagRepository;
    private final ModelMapperUtils modelMapperUtils;
    public TagServiceImpl(BaseUtils baseUtils, DocumentRepository documentRepository,
                          TagRepository tagRepository, ModelMapperUtils modelMapperUtils,
                          LikeDocumentRepository likeDocumentRepository) {
        this.baseUtils = baseUtils;
        this.documentRepository = documentRepository;
        this.tagRepository = tagRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.likeDocumentRepository = likeDocumentRepository;
    }

    @Override
    public boolean createTag(String documentKey, String tagName) {
        User user = baseUtils.getUser();
        if (user != null) {
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if (document != null && !tagRepository.existsByTagNameAndDocument(tagName, document)) {
                Tag tag = new Tag();
                tag.setTagName(tagName);
                tag.setDocument(document);
                tagRepository.save(tag);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean updateTag(String documentKey, Long id, String tagName) {
        User user = baseUtils.getUser();
        if (user != null) {
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if (document != null && !tagRepository.existsByIdAndDocument(id, document)){
                Tag tag = tagRepository.findByIdAndDocument(id, document);
                tag.setTagName(tagName);
                tag.setUpdatedAt(new Date());
                tagRepository.save(tag);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean deleteTag(String documentKey, String tagName) {
        User user = baseUtils.getUser();
        if (user != null){
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if (document != null){
                Tag tag = tagRepository.findByTagNameAndDocument(tagName, document);
                if (tag != null){
                    tagRepository.delete(tag);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    @Override
    public List<TagDto> showAllTag(String documentKey) {
        User user = baseUtils.getUser();
        if (user != null){
            Document document = documentRepository.findByDocumentKeyAndStatusDelete(documentKey, (byte) 0);
            if (document != null && (document.getDocsPublic() == 1 || document.getUser().equals(user))){
                List<Tag> tagList = tagRepository.findAllByDocument(document);
                List<TagDto> tagDtoList = new ArrayList<>();
                if (tagList != null && !tagList.isEmpty()){
                    for (Tag tag : tagList){
                        tagDtoList.add(modelMapperUtils.mapAllProperties(tag, TagDto.class));
                    }
                }
                return tagDtoList;
            }
            return null;
        }
        return null;
    }

    @Override
    public List<DocumentDto> findDocumentsByTag(String tagName) {
        User user = baseUtils.getUser();
        if(user != null){
            List<Tag> tags = tagRepository.findByTagNameContainingIgnoreCase(tagName);
            List<Document> documentList = new ArrayList<>();
            List<Document> result = new ArrayList<>();
            if(tags != null && !tags.isEmpty()){
                for(Tag tag : tags){
                    if(tag.getDocument().getStatusDelete() == 0 && tag.getDocument().getUser() == user){
                        documentList.add(tag.getDocument());
                    }
                }
                Set<Document> documentListUnique = new HashSet<>(documentList);
                result = new ArrayList<>(documentListUnique);

            }
            return baseUtils.getListDocumentsDto(result);
        }
        return null;

    }

    @Override
    public List<DocumentDto> findDocumentsPublicByTag(String tagName) {
        User user = baseUtils.getUser();
        if(user != null){
            List<Tag> tags = tagRepository.findByTagNameContainingIgnoreCase(tagName);
            List<Document> documentsList = new ArrayList<>();
            List<Document> result = new ArrayList<>();
            List<DocumentDto> documentDtoList = new ArrayList<>();
            if(tags != null && !tags.isEmpty()){
                for(Tag tag : tags){
                    if(tag.getDocument().getStatusDelete() == 0 && tag.getDocument().getDocsPublic() == 1){
                        documentsList.add(tag.getDocument());
                    }
                }
                Set<Document> documentListUnique = new HashSet<>(documentsList);
                result = new ArrayList<>(documentListUnique);
            }
            documentDtoList = baseUtils.getListDocumentsDto(result);
            if(documentDtoList != null && !documentsList.isEmpty()){
                for (DocumentDto documentDto : documentDtoList){
                    if(likeDocumentRepository.existsByUserAndDocumentId(user, documentDto.getId())){
                        documentDto.setLiked((byte) 1);
                    }
                }
            }
            return documentDtoList;
        }
        return null;
    }


}
