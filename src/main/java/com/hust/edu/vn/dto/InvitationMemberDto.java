package com.hust.edu.vn.dto;

import com.hust.edu.vn.entity.InvitationMember;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link InvitationMember} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitationMemberDto implements Serializable {
    private Long id;
    private String groupName;
    private Long groupId;
    private String email;
    private Date createdAt;
    private Date updatedAt;
}