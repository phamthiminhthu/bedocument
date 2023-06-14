package com.hust.edu.vn.controller.document;


import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.TypeDocumentDto;
import com.hust.edu.vn.services.document.TypeDocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/management/document/{documentKey}/type")

public class TypeDocumentController {
    private final TypeDocumentService typeDocumentService;

    public TypeDocumentController(TypeDocumentService typeDocumentService) {
        this.typeDocumentService = typeDocumentService;
    }

    @PostMapping("create")
    public ResponseEntity<CustomResponse> addTypeDocument(@PathVariable String documentKey, @RequestParam(value="type") String type){
        boolean status = typeDocumentService.addTypeDocument(documentKey, type);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Add type successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Typename existed. Please other typename");
    }

    @GetMapping("show/all")
    public ResponseEntity<CustomResponse> showAllTypeDocument(@PathVariable String documentKey){
        List<TypeDocumentDto> documentTypeDtoList = typeDocumentService.showAllTypeDocument(documentKey);
        if(documentTypeDtoList == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Please login");
        }
        if(documentTypeDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show all type successfully", documentTypeDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Empty", documentTypeDtoList);

    }

    record TypeDocumentModel(Long id, String typeName){
        public Long getId(){
            return id;
        }
        public String getTypeName() { return typeName; }
    }

    @PostMapping("update")
    public ResponseEntity<CustomResponse> updateTypeDocument(@PathVariable String documentKey,@RequestBody TypeDocumentModel typeDocumentModel){
        boolean status = typeDocumentService.updateTypeDocument(documentKey, typeDocumentModel.getId(), typeDocumentModel.getTypeName());
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update failed");
    }

    @PostMapping("delete")
    public ResponseEntity<CustomResponse> deleteTypeDocument(@PathVariable String documentKey, @RequestParam(value="id") Long id){
        boolean status = typeDocumentService.deleteTypeDocument(documentKey, id);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Delete failed");
    }
}
