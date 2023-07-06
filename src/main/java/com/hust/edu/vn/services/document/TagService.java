package com.hust.edu.vn.services.document;


import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.dto.TagDto;

import java.util.List;

public interface TagService {
    boolean createTag(String documentKey, String tagName);

    boolean updateTag(String documentKey, Long id, String tagName);

    boolean deleteTag(String documentKey, String tagName);

    List<TagDto> showAllTag(String documentKey);

    List<DocumentDto> findDocumentsByTag(String tagName);

    List<DocumentDto> findDocumentsPublicByTag(String tagName);
}
