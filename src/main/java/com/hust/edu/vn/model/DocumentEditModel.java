package com.hust.edu.vn.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentEditModel {
    private DocumentModel documentModel;
    private List<String> tags;
    private List<String> typesDoc;
}
