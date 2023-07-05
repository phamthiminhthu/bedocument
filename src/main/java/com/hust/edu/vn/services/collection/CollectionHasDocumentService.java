package com.hust.edu.vn.services.collection;

import com.hust.edu.vn.dto.CollectionHasDocumentDto;
import com.hust.edu.vn.model.CollectionModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface CollectionHasDocumentService {
    boolean createDocumentCollection(Long collectionId, MultipartFile file);

    CollectionHasDocumentDto getDocumentCollection(Long collectionId);

    boolean updateCollectionDocument(Long oldCollectionIdFirst, Long newCollectionIdSecond, String documentKey);

    boolean deleteCollectionDocument(Long collectionId, String documentKey);

    boolean moveDocumentToCollection(List<Long> idCollections, List<String> documentKeys);

//    boolean moveDocumentCollection(Long collectionId, List<String> listDocumentKey, List<Long> listCollectionId);
}
