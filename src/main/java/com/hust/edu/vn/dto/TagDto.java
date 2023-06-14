package com.hust.edu.vn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link com.hust.edu.vn.entity.Tag} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor

public class TagDto implements Serializable {
    private Long id;
    private DocumentDto document;
    private String tagName;
    private Date createdAt;
    private Date updatedAt;
}