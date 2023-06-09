package com.hust.edu.vn.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentModel {
//    @JsonIgnore
    private String documentKey;
    private String authors;
    private String title;
    private Integer publishingYear;
    private String summary;
    private String docsName;
    private Byte loved;
    private Byte docsPublic;
    private Byte docsStatus;
    private String note;
    private Date createdAt;
    private Date updatedAt;
}
