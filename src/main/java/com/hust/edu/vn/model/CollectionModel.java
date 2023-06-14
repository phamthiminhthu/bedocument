package com.hust.edu.vn.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionModel {
    @NotBlank
    private String collectionName;
    private Long parentCollectionId;
}
