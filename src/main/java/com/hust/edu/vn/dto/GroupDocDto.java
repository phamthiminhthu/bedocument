package com.hust.edu.vn.dto;

import com.hust.edu.vn.entity.GroupDoc;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

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
    private Date createdAt;
    private Date updatedAt;
}