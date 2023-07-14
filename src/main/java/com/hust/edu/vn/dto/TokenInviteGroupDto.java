package com.hust.edu.vn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link com.hust.edu.vn.entity.TokenInviteGroup} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenInviteGroupDto implements Serializable {
    private Long id;
    private String email;
    private Date createdAt;
    private Date updatedAt;
}