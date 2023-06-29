package com.hust.edu.vn.controller.document;


import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.dto.TypeDocumentDto;
import com.hust.edu.vn.services.document.TypeDocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/management/document/type")

public class TypeDocumentController {
    private final TypeDocumentService typeDocumentService;

    public TypeDocumentController(TypeDocumentService typeDocumentService) {
        this.typeDocumentService = typeDocumentService;
    }

    @PostMapping("create")
    public ResponseEntity<CustomResponse> addTypeDocument(@RequestParam(value = "documentKey") String documentKey, @RequestParam(value="type") String type){
        boolean status = typeDocumentService.addTypeDocument(documentKey, type);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Add type successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Typename existed. Please other typename");
    }

    @GetMapping("show/all")
    public ResponseEntity<CustomResponse> showAllTypeDocument(@RequestParam(value = "documentKey") String documentKey){
        List<TypeDocumentDto> documentTypeDtoList = typeDocumentService.showAllTypeDocument(documentKey);
        if(documentTypeDtoList == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Please login");
        }
        if(documentTypeDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show all type successfully", documentTypeDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Empty", documentTypeDtoList);
    }

    @GetMapping("find/typeDocument")
    public ResponseEntity<CustomResponse> findDocumentByTypeDocument(@RequestParam(value="typeName") String typeName){
        List<DocumentDto> documentDtoList = typeDocumentService.findDocumentByTypeDocument(typeName);
        if(documentDtoList == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Please login");
        }
        if(documentDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Find type successfully", documentDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Empty", documentDtoList);
    }

    record TypeDocumentModel(Long id, String typeName){
        public Long getId(){
            return id;
        }
        public String getTypeName() { return typeName; }
    }

    @PostMapping("update")
    public ResponseEntity<CustomResponse> updateTypeDocument(@RequestParam(value = "documentKey") String documentKey, @RequestBody TypeDocumentModel typeDocumentModel){
        boolean status = typeDocumentService.updateTypeDocument(documentKey, typeDocumentModel.getId(), typeDocumentModel.getTypeName());
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update failed");
    }

    @PostMapping("delete")
    public ResponseEntity<CustomResponse> deleteTypeDocument(@RequestParam(value = "documentKey") String documentKey, @RequestParam(value="typeName") String typeName){
        boolean status = typeDocumentService.deleteTypeDocument(documentKey, typeName);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Delete failed");
    }
}
