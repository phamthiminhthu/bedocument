package com.hust.edu.vn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDocTreeDto implements Serializable {
    private Long id;
    private String groupName;
    private List<CollectionTreeDto> collectionDtoList;
}
