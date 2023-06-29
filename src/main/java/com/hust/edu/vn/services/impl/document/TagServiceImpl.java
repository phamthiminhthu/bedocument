package com.hust.edu.vn.services.impl.document;

import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.dto.TagDto;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.Tag;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.repository.DocumentRepository;
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

    private final BaseUtils baseUtils;
    private final DocumentRepository documentRepository;
    private final TagRepository tagRepository;
    private final ModelMapperUtils modelMapperUtils;

    public TagServiceImpl(BaseUtils baseUtils, DocumentRepository documentRepository,
                          TagRepository tagRepository, ModelMapperUtils modelMapperUtils) {
        this.baseUtils = baseUtils;
        this.documentRepository = documentRepository;
        this.tagRepository = tagRepository;
        this.modelMapperUtils = modelMapperUtils;
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
        List<Tag> tags = tagRepository.findByTagNameContainingIgnoreCase(tagName);
        List<DocumentDto> documentDtoList = new ArrayList<>();
        List<DocumentDto> result = new ArrayList<>();
        if(tags != null && !tags.isEmpty()){
            for(Tag tag : tags){
                documentDtoList.add(modelMapperUtils.mapAllProperties(tag.getDocument(), DocumentDto.class));
            }
            Set<DocumentDto> documentDtoListUnique = new HashSet<>(documentDtoList);
            log.info(documentDtoListUnique.toString());
            result = new ArrayList<>(documentDtoListUnique);
        }
        return result;
    }

}
