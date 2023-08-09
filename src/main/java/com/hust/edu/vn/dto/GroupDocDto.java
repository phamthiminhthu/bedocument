package com.hust.edu.vn.dto;

import com.hust.edu.vn.entity.GroupDoc;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * A DTO for the {@link GroupDoc} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDocDto implements Serializable {
    private Long id;
    private UserDto user;
    private String groupName;
    private List<DocumentDto> documentDtoList;
    private List<CollectionDto> collectionDtoList;
    private List<UserDto> userDtoList;
    private Byte statusOwner = 0;
    private Date createdAt;
    private Date updatedAt;
}