package com.hust.edu.vn.controller.group;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.CollectionDto;
import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.services.group.GroupHasDocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/management/group")
public class GroupHasDocumentController {

    private final GroupHasDocumentService groupHasDocumentService;

    public GroupHasDocumentController(GroupHasDocumentService groupHasDocumentService) {
        this.groupHasDocumentService = groupHasDocumentService;
    }


    @PostMapping("{groupId}/document/create")
    public ResponseEntity<CustomResponse> createDocument(@PathVariable(value="groupId") Long groupId, @RequestParam(value = "collectionId") String collectionId, @ModelAttribute(value="file") MultipartFile file){
        Long convertCollectionId = null;
        if(collectionId != null &&  !collectionId.equals("null")){
            convertCollectionId = Long.valueOf(collectionId);
        }
        boolean status = groupHasDocumentService.createDocument(groupId, convertCollectionId, file);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Create Document successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Create Document Failed");
    }

    @GetMapping("{groupId}/show/all")
    public ResponseEntity<CustomResponse> showAllDocumentACollection(@PathVariable(value="groupId") Long groupId, @RequestParam(value="collectionId") Long collectionId){
        List<DocumentDto> documentDtoList = groupHasDocumentService.showAllDocumentACollection(groupId, collectionId);
        if(documentDtoList != null && !documentDtoList.isEmpty()){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show all document of a collection in group successfully", documentDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "Not found documents");
    }

    @PostMapping("{groupId}/document/delete")
    public ResponseEntity<CustomResponse> deleteDocumentGroup(@PathVariable(value="groupId") Long groupId, @RequestParam(value="collectionId") String collectionId, @RequestBody List<String> documentKeys){
        Long convertedCollection = null;
        if(collectionId != null && !collectionId.equals("null")){
            convertedCollection = Long.parseLong(collectionId);
        }
        boolean status = groupHasDocumentService.deleteDocumentGroup(groupId, convertedCollection, documentKeys);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Delete Failed");
    }

    record GroupsDocumentsList(List<Long> idGroups, List<String> documentKeys ){
        public List<Long> getIdGroups(){
            return idGroups;
        }
        public List<String> getDocumentKeys(){
            return documentKeys;
        }
    }

//
//    // move document of this group/collection to other group/collection
    @PostMapping("document/move")
    public ResponseEntity<CustomResponse> moveDocumentsGroup(@RequestBody GroupsDocumentsList groupsDocumentsList){
        boolean status = groupHasDocumentService.moveDocumentToGroup(groupsDocumentsList.getIdGroups(), groupsDocumentsList.getDocumentKeys());
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Move documents successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Move documents failed");
    }

}
