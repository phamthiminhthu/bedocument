package com.hust.edu.vn.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link com.hust.edu.vn.entity.GroupHasDocument} entity
 */
@Data
public class GroupHasDocumentDto implements Serializable {
    private Long id;
    private GroupDocDto group;
    private DocumentDto document;
    private Date createdAt;
    private Date updatedAt;
}