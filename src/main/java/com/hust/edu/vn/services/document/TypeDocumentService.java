package com.hust.edu.vn.services.document;

import com.hust.edu.vn.dto.TypeDocumentDto;

import java.util.List;

public interface TypeDocumentService {
    boolean addTypeDocument(String documentKey, String type);

    List<TypeDocumentDto> showAllTypeDocument(String documentKey);

    boolean updateTypeDocument(String documentKey, Long id, String typeName);

    boolean deleteTypeDocument(String documentKey, String typeName);
}
