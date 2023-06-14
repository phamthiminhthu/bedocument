package com.hust.edu.vn.dto;

import com.hust.edu.vn.entity.CollectionHasDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * A DTO for the {@link CollectionHasDocument} entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionHasDocumentDto implements Serializable {
    private CollectionDto collection;
    private List<DocumentDto> documentDtoList;
}