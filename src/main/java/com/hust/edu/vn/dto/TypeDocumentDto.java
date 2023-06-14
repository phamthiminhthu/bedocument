package com.hust.edu.vn.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link com.hust.edu.vn.entity.TypeDocument} entity
 */
@Data
public class TypeDocumentDto implements Serializable {
    private Long id;
    private DocumentDto document;
    private String typeName;
    private Date createdAt;
    private Date updatedAt;
}