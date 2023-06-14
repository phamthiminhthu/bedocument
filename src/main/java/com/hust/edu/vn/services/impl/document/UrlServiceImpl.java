package com.hust.edu.vn.services.impl.document;

import com.hust.edu.vn.dto.UrlDto;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.TypeDocument;
import com.hust.edu.vn.entity.Url;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.model.UrlModel;
import com.hust.edu.vn.repository.DocumentRepository;
import com.hust.edu.vn.repository.UrlRepository;
import com.hust.edu.vn.services.document.UrlService;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UrlServiceImpl implements UrlService {
    private final BaseUtils baseUtils;
    private final DocumentRepository documentRepository;
    private final UrlRepository urlRepository;
    private final ModelMapperUtils modelMapperUtils;

    public UrlServiceImpl(BaseUtils baseUtils, DocumentRepository documentRepository, UrlRepository urlRepository, ModelMapperUtils modelMapperUtils) {
        this.baseUtils = baseUtils;
        this.documentRepository = documentRepository;
        this.urlRepository = urlRepository;
        this.modelMapperUtils = modelMapperUtils;
    }


    @Override
    public boolean createUrl(String documentKey, UrlModel urlModel) {
        if(urlModel.getUrl() == null || documentKey == null ) return false;
        User user = baseUtils.getUser();
        if(user != null){
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if(document != null){
                if(!urlRepository.existsByDocumentAndUrl(document, urlModel.getUrl())){
                    Url url = modelMapperUtils.mapAllProperties(urlModel, Url.class);
                    urlRepository.save(url);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    @Override
    public List<UrlDto> showAllUrl(String documentKey) {
        if(documentKey == null) return null;
        Document document = documentRepository.findByDocumentKeyAndStatusDelete(documentKey, (byte) 0);
        if(document != null ){
            if(document.getDocsPublic() == 1 || (document.getDocsPublic() == 0 && document.getUser() == baseUtils.getUser())){
                List<Url> urlList = urlRepository.findAllByDocument(document);
                List<UrlDto> urlDtoList = new ArrayList<>();
                if(urlList != null && !urlList.isEmpty()){
                    for(Url url : urlList){
                        urlDtoList.add(modelMapperUtils.mapAllProperties(url, UrlDto.class));
                    }
                }
                return urlDtoList;
            }
            return null;
        }
        return null;
    }

    @Override
    public boolean updateUrl(Long id, UrlModel urlModel) {
        if(urlModel.getUrl() == null || id == null) return false;
        User user = baseUtils.getUser();
        if(user != null){
            Url url = urlRepository.findById(id).orElse(null);
            if(url != null && url.getDocument().getUser() == user){
                url.setUrl(urlModel.getUrl());
                url.setDescription(urlModel.getDescription());
                url.setUpdatedAt(new Date());
                urlRepository.save(url);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean deleteUrl(Long id) {
        if(id == null) return false;
        User user = baseUtils.getUser();
        if(user != null) {
            Url url = urlRepository.findById(id).orElse(null);
            if(url !=null && url.getDocument().getUser() == user){
                urlRepository.delete(url);
                return true;
            }
            return false;
        }
        return false;
    }
}
