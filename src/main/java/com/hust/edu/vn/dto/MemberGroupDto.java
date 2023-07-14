package com.hust.edu.vn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link MemberGroupDto} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberGroupDto implements Serializable {
    private String username;
    private String image;
    private Byte statusOwner = 0;
    private Date createdAt;
}