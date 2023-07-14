package com.hust.edu.vn.services.group;

import com.hust.edu.vn.dto.DocumentDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GroupHasDocumentService {
    boolean createDocument(Long groupId, Long collectionId, MultipartFile file);

//    List<DocumentDto> showAllDocumentGroup(Long groupId);

    List<DocumentDto> showAllDocumentACollection(Long groupId, Long collectionId);

    boolean deleteDocumentGroup(Long groupId, Long collectionId,  List<String> documentKeys);

    boolean moveDocumentToGroup(List<Long> idGroups, List<String> documentKeys);
}
