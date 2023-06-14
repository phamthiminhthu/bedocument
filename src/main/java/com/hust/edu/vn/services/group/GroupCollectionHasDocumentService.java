package com.hust.edu.vn.services.group;

import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.model.DocumentModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GroupCollectionHasDocumentService {
    boolean createDocument(Long groupId, Long collectionId, MultipartFile file);

//    List<DocumentDto> showAllDocumentGroup(Long groupId);

    List<DocumentDto> showAllDocumentACollection(Long groupId, Long collectionId);

    boolean deleteDocumentGroup(Long groupId, Long collectionId,  List<String> documentKeys);

}
