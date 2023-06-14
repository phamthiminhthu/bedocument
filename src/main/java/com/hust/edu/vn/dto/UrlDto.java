package com.hust.edu.vn.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link com.hust.edu.vn.entity.Url} entity
 */
@Data
public class UrlDto implements Serializable {
    private Long id;
    private DocumentDto document;
    private String url;
    private String description;
    private Date createdAt;
    private Date updatedAt;
}