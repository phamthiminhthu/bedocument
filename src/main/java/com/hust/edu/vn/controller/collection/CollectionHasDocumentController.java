package com.hust.edu.vn.controller.collection;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.CollectionHasDocumentDto;
import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.services.collection.CollectionHasDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@Slf4j
@RequestMapping("/api/v1/owner/management/collection/document")
public class CollectionHasDocumentController {

    private final CollectionHasDocumentService collectionHasDocumentService;

    public CollectionHasDocumentController(CollectionHasDocumentService collectionHasDocumentService) {
        this.collectionHasDocumentService = collectionHasDocumentService;
    }

    @PostMapping("create")
    public ResponseEntity<CustomResponse> createDocumentCollection(@RequestParam(value = "collectionId") Long collectionId, @ModelAttribute(value = "file") MultipartFile file){
        boolean status = collectionHasDocumentService.createDocumentCollection(collectionId, file);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Create document in collection successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Create document in collection Failed");
    }

    @GetMapping("show/all")
    public ResponseEntity<CustomResponse> showDocumentCollection(@RequestParam(value = "collectionId") Long collectionId){
        CollectionHasDocumentDto collectionDocuments = collectionHasDocumentService.getDocumentCollection(collectionId);
        if(collectionDocuments != null ){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show all document in collection successfully", collectionDocuments);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Don't have document");
    }

    @PostMapping("update")
    public ResponseEntity<CustomResponse> updateCollectionDocument(@RequestParam(value = "collectionId") Long oldCollectionIdFirst, @RequestParam(value="newCollectionIdSecond") Long newCollectionIdSecond, @RequestParam(value="documentKey") String documentKey){
        boolean status = collectionHasDocumentService.updateCollectionDocument(oldCollectionIdFirst, newCollectionIdSecond, documentKey);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update collection successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update collection successfully");
    }

    @PostMapping("delete")
    public ResponseEntity<CustomResponse> deleteDocumentCollection(@RequestParam(value = "collectionId") Long collectionId, @RequestParam(value="documentKey") String documentKey){
        boolean status = collectionHasDocumentService.deleteCollectionDocument(collectionId, documentKey);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete document in collection successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Delete document in collection FAILED");
    }

    record CollectionsDocumentsList(List<Long> idCollections, List<String> documentKeys ){
        public List<Long> getIdCollections(){
            return idCollections;
        }
        public List<String> getDocumentKeys(){
            return documentKeys;
        }
    }
    @PostMapping("move/documents")
    public ResponseEntity<CustomResponse> moveDocumentToCollection(@RequestBody CollectionsDocumentsList collectionsDocumentsList){
        boolean status = collectionHasDocumentService.moveDocumentToCollection(collectionsDocumentsList.getIdCollections(), collectionsDocumentsList.getDocumentKeys());
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Move document to collection successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Move document to collection failed");
    }


//    record DocumentCollection(List<String> listDocumentKey, List<Long> listCollectionId){
//        public List<String> getListDocumentKey(){
//            return listDocumentKey;
//        }
//        public List<Long> getListCollectionId() { return listCollectionId; }
//    }
//
//
//    @PostMapping("move")
//    public ResponseEntity<CustomResponse> moveDocumentCollection(@PathVariable(value = "collectionId") Long collectionId, @RequestBody DocumentCollection documentCollection){
//        boolean status = collectionHasDocumentService.moveDocumentCollection(collectionId, documentCollection.getListDocumentKey(), documentCollection.getListCollectionId());
//        if(status){
//            return CustomResponse.generateResponse(HttpStatus.OK, "Move Document successfully");
//        }
//        return CustomResponse.generateResponse(HttpStatus.OK, "Move Document failed");
//    }




}
