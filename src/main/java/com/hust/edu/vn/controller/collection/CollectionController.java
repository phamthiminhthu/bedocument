package com.hust.edu.vn.controller.collection;


import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.entity.Collection;
import com.hust.edu.vn.model.CollectionModel;
import com.hust.edu.vn.services.collection.CollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.TreeMap;

@RestController
@Slf4j
@RequestMapping("/api/v1/owner/management/collection")
public class CollectionController {
     public final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping("create")
    public ResponseEntity<CustomResponse> createCollection(@RequestBody CollectionModel collectionModel){
        boolean status = collectionService.createCollection(collectionModel);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.CREATED, "Create Collection successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Create Collection FAILED");
    }

    @GetMapping("show/all")
    public ResponseEntity<CustomResponse> showCollection(){
        TreeMap<Long, List<Collection>> result = collectionService.showCollection();
        if( result == null){
            return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "Show Collection FAILED");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Show Collection successfully", result);
    }

    @PostMapping("update/{id}")
    public ResponseEntity<CustomResponse> updateCollection(@PathVariable(value="id") Long id, @RequestBody CollectionModel collectionModel){
        boolean status = collectionService.updateCollection(id, collectionModel);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update Collection successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update Collection FAILED");
    }
    @PostMapping("delete/{id}")
    public ResponseEntity<CustomResponse> deleteCollection(@PathVariable(value="id") Long id){
        boolean status = collectionService.deleteCollection(id);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete Collection successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Delete Collection FAILED");
    }


}
