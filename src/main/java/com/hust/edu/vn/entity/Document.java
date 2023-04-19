package com.hust.edu.vn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "document")
public class Document {
    @Id
    @Column(name = "document_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title")
    private String title;

    @Column(name = "loved")
    private Byte loved;

    @Column(name = "docs_public")
    private Byte docsPublic;

    @Column(name = "docs_status")
    private Byte docsStatus;

    @Column(name = "publishing_year")
    private Integer publishingYear;

    @Lob
    @Column(name = "summary")
    private String summary;

    @Lob
    @Column(name = "note")
    private String note;

    @Column(name = "quantity_like")
    private Long quantityLike;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}