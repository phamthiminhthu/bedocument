package com.hust.edu.vn.dto;

import com.hust.edu.vn.entity.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link Collection} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionDto implements Serializable {
    private Long id;
    private Long parentCollectionId;
    private UserDto user;
    private String collectionName;
    private Date createdAt;
    private Date updatedAt;
}