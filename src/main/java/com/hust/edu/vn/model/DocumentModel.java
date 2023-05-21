package com.hust.edu.vn.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentModel {
    private String authors;
    private String title;
    private Integer publishingYear;
    public String summary;
    private Byte loved;
    private Byte docsPublic;
    private Byte docsStatus;
    private String note;
    private Date createdAt;
    private Date updatedAt ;
}
