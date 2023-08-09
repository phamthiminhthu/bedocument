package com.hust.edu.vn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "collection")
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collection_id", nullable = false)
    private Long id;

    @Column(name = "parent_collection_id")
    private Long parentCollectionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private GroupDoc groupDoc;

    @Column(name = "collection_name", nullable = false)
    private String collectionName;

    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date();

    @Column(name = "updated_at", nullable = false)
    private Date updatedAt = new Date();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentCollectionId", cascade = CascadeType.REMOVE)
    private List<Collection> subCollectionDtoList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "collection", cascade = CascadeType.REMOVE)
    private List<CollectionHasDocument> subCollectionDocuments;
}