package com.hust.edu.vn.controller.group;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.services.group.GroupCollectionHasDocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/management/group/{groupId}")
public class GroupHasDocumentController {

    private final GroupCollectionHasDocumentService groupCollectionHasDocumentService;

    public GroupHasDocumentController(GroupCollectionHasDocumentService groupCollectionHasDocumentService) {
        this.groupCollectionHasDocumentService = groupCollectionHasDocumentService;
    }


    @PostMapping("document/create")
    public ResponseEntity<CustomResponse> createDocument(@PathVariable(value="groupId") Long groupId, @RequestParam(value = "collectionId") Long collectionId, @ModelAttribute(value="file") MultipartFile file){
        boolean status = groupCollectionHasDocumentService.createDocument(groupId, collectionId, file);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Create Document successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Create Document Failed");
    }

//    // checking ~~ show all documents in group
//    @GetMapping("show/all")
//    public ResponseEntity<CustomResponse> showAllDocumentGroup(@PathVariable(value="groupId") Long groupId){
//        List<DocumentDto> documentDtoList = groupCollectionHasDocumentService.showAllDocumentGroup(groupId);
//        if(documentDtoList != null && !documentDtoList.isEmpty()){
//            return CustomResponse.generateResponse(HttpStatus.OK, "Show all documents in group successfully", documentDtoList);
//        }
//        return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "No documents in group");
//    }

    @GetMapping("show/all")
    public ResponseEntity<CustomResponse> showAllDocumentACollection(@PathVariable(value="groupId") Long groupId, @RequestParam(value="collectionId") Long collectionId){
        List<DocumentDto> documentDtoList = groupCollectionHasDocumentService.showAllDocumentACollection(groupId, collectionId);
        if(documentDtoList != null && !documentDtoList.isEmpty()){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show all document of a collection in group successfully", documentDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "Not found documents");
    }

    @PostMapping("document/delete")
    public ResponseEntity<CustomResponse> deleteDocumentGroup(@PathVariable(value="groupId") Long groupId, @RequestParam(value="collectionId") Long collectionId, @RequestBody List<String> documentKeys){
        boolean status = groupCollectionHasDocumentService.deleteDocumentGroup(groupId, collectionId, documentKeys);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Delete Failed");
    }

//
////    // move document of this group/collection to other group/collection
//    @PostMapping("document/move")
//    public ResponseEntity<CustomResponse> moveDocumentsGroup(@PathVariable(value="groupId") Long groupId, @RequestParam(value="collectionId") Long collectionId, @RequestBody List<String> listDocumentKeys){
//        boolean status = groupCollectionHasDocumentService.moveDocumentGroupToCollection(groupId, collectionId, listDocumentKeys);
//        if(status){
//            return CustomResponse.generateResponse(HttpStatus.OK, "Move documents successfully");
//        }
//        return CustomResponse.generateResponse(HttpStatus.OK, "Move documents failed");
//    }

}
