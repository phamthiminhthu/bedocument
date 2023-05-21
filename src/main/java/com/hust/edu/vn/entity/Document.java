package com.hust.edu.vn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "document")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id", nullable = false)
    private Long id;

    @Column(name = "document_key", nullable = false, unique = true)
    private String documentKey;

    @Column(name = "docs_name", nullable = false)
    private String docsName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title")
    private String title;

    @Column(name = "authors")
    private String authors;

    @Column(name = "loved")
    private Byte loved = 0;

    @Column(name = "docs_public")
    private Byte docsPublic = 0;

    @Column(name = "docs_status")
    private Byte docsStatus = 0;

    @Column(name = "publishing_year")
    private Integer publishingYear;

    @Lob
    @Column(name = "summary")
    private String summary;

    @Lob
    @Column(name = "note")
    private String note;

    @Column(name = "quantity_like")
    private Long quantityLike = 0L;

    @Column(name = "status_delete")
    private Byte statusDelete = 0;

    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date();

    @Column(name = "updated_at", nullable = false)
    private Date updatedAt = new Date();

}