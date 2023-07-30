package com.hust.edu.vn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invitation_member")
public class InvitationMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_member_id", nullable = false)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)

    @JoinColumn(name = "group_id", nullable = false)
    private GroupDoc group;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date();

    @Column(name = "updated_at", nullable = false)
    private Date updatedAt = new Date();

}
