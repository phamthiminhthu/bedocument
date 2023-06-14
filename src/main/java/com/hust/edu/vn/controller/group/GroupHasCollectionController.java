package com.hust.edu.vn.controller.group;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.CollectionDto;
import com.hust.edu.vn.model.CollectionModel;
import com.hust.edu.vn.services.group.GroupHasCollectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/v1/management/group/{groupId}/collection")
public class GroupHasCollectionController {

    private final GroupHasCollectionService groupHasCollectionService;

    public GroupHasCollectionController(GroupHasCollectionService groupHasCollectionService) {
        this.groupHasCollectionService = groupHasCollectionService;
    }

    @PostMapping("create")
    public ResponseEntity<CustomResponse> createCollectionGroupDoc(@PathVariable(value = "groupId") Long groupId,  @RequestBody CollectionModel collectionModel){
        boolean status = groupHasCollectionService.createCollectionGroupDoc(groupId, collectionModel);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Collection group successfully created");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Create Collection group FAILED");
    }

    @GetMapping("show/all")
    public ResponseEntity<CustomResponse> showAllCollectionGroupDoc(@PathVariable(value = "groupId") Long groupId){
        TreeMap<Long, List<CollectionDto>> collectionDtosList = groupHasCollectionService.showAllCollectionGroupDoc(groupId);
        if(collectionDtosList != null && !collectionDtosList.isEmpty()){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show all collection in group successfully", collectionDtosList);
        }
        return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "No collection found");
    }

    //todo: checking ~ update collection in group ( can update collection parent hay k?)
    @PostMapping("update/{collectionId}")
    public ResponseEntity<CustomResponse> updateCollectionGroupDoc(@PathVariable(value = "groupId") Long groupId, @PathVariable(value = "collectionId") Long collectionId,  @RequestBody CollectionModel collectionModel){
        boolean status = groupHasCollectionService.updateCollectionGroupDoc(groupId, collectionId, collectionModel);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update collection in group successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update collection in group Failed");
    }

    //todo: checking ~~ delete collection in group
    @PostMapping("delete/{collectionId}")
    public ResponseEntity<CustomResponse> deleteCollectionGroupDoc(@PathVariable(value = "groupId") Long groupId, @PathVariable(value = "collectionId") Long collectionId){
        boolean status = groupHasCollectionService.deleteCollectionGroupDoc(groupId, collectionId);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete collection in successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Delete collection Failed");
    }

}
