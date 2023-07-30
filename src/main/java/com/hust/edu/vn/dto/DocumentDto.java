package com.hust.edu.vn.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class DocumentDto implements Serializable {
    private Long id;
    private String documentKey;
    private String docsName;
    private UserDto user;
    private String title;
    private String authors;
    private Byte loved;
    private Byte docsPublic;
    private Byte docsStatus;
    private Integer publishingYear;
    private String summary;
    private String note;
    private Long quantityLike;
    private Byte statusDelete;
    private Date createdAt;
    private Date updatedAt;

    private List<UrlDto> urls;
    private List<TypeDocumentDto> typeDocumentsList;
    private List<TagDto> tagDtoList;
    private Byte liked = 0;
    private List<DocumentDto> documentsDtoSameHashcode;
}