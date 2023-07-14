package com.hust.edu.vn.controller.collection;


import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.CollectionDto;
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
            return CustomResponse.generateResponse(HttpStatus.OK, "Create Collection successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Create Collection FAILED");
    }

    @GetMapping("show/all")
    public ResponseEntity<CustomResponse> showCollection(){
        TreeMap<Long, List<Collection>> result = collectionService.showCollection();
        if( result == null){
            return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "Show Collection FAILED");
        }
        if(result.isEmpty()){
            return CustomResponse.generateResponse(HttpStatus.OK, "No collections", result);

        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Show Collection successfully", result);
    }

    @GetMapping("details/show/all/{id}")
    public ResponseEntity<CustomResponse> showAllDetailsCollectionById(@PathVariable(value = "id") Long id, @RequestParam(value = "groupId") String groupId){
        Long convertValue = null;
        if(groupId != null && !groupId.equals("null")){
            convertValue = Long.parseLong(groupId);
        }
        CollectionDto collectionDto = collectionService.showAllDetailsCollectionById(id, convertValue);
        if(  collectionDto == null){
            return CustomResponse.generateResponse(HttpStatus.OK, "Collection not existed");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Collection existed", collectionDto);
    }

    @GetMapping("show/{id}")
    public ResponseEntity<CustomResponse> showCollectionById(@PathVariable(value = "id") Long id){
        CollectionDto collectionDto = collectionService.showCollectionById(id);
        if( collectionDto == null){
            return CustomResponse.generateResponse(HttpStatus.OK, "Collection not existed");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Collection existed", collectionDto);
    }

    @GetMapping("parent/show/all")
    public ResponseEntity<CustomResponse> showAllCollectionParent(){
        List<CollectionDto> results = collectionService.showAllCollectionParent();
        if(results == null){
            return CustomResponse.generateResponse(HttpStatus.UNAUTHORIZED, "No users existed");
        }
        if(results.size() == 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "No collection", results);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "List collections Parent", results);
    }

    @GetMapping("show/all/name")
    public ResponseEntity<CustomResponse> showAllNameCollectionWithoutGroupDoc(){
        List<CollectionDto> collectionDtoList = collectionService.showAllNameCollectionWithoutGroupDoc();
        if( collectionDtoList == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Show all name collection failed");
        }
        if(collectionDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show all name collection successfully", collectionDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Empty", collectionDtoList);
    }


    @PostMapping("update/{id}")
    public ResponseEntity<CustomResponse> updateCollection(@PathVariable(value="id") Long id, @RequestBody CollectionModel collectionModel){
        boolean status = collectionService.updateCollection(id, collectionModel);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update Collection successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update Collection FAILED");
    }

    @PostMapping("rename/{id}")
    public ResponseEntity<CustomResponse> renameCollection(@PathVariable(value="id") Long id, @RequestParam(value="name") String name){
        boolean status = collectionService.renameCollection(id, name);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update name collection successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update name collection FAILED");
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
