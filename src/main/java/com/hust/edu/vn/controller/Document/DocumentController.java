package com.hust.edu.vn.controller.Document;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.model.DocumentModel;
import com.hust.edu.vn.services.document.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/management/document")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("upload")
    public ResponseEntity<CustomResponse> uploadDocument(@ModelAttribute(value = "file") MultipartFile file) {
        Document document = documentService.uploadDocument(file);
        return CustomResponse.generateResponse(HttpStatus.OK, "Upload successfully", document);
    }
    @GetMapping("show/all")
    public ResponseEntity<CustomResponse> getListDocument(){
        List<DocumentModel> documents = documentService.getListDocument();
        if (documents.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "List all document", documents);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Don't have documents. Please add more documents", documents);
    }
    @GetMapping("show/details/{documentKey}")
    public ResponseEntity<CustomResponse> getDocumentDetails(@PathVariable String documentKey){
        DocumentModel documentModel = documentService.getDocumentModel(documentKey);
        if(documentModel != null){
            return CustomResponse.generateResponse(HttpStatus.OK, "Document Details successfully", documentModel);
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Document doesn't exist");
    }
    @GetMapping("display/{filename}")
    public ResponseEntity<byte[]> displayDocument(@PathVariable("filename") String filename) {
        byte[] data = documentService.loadFileFromS3(filename);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_PDF);
        return ResponseEntity.ok().headers(httpHeaders).body(data);
    }

    @PostMapping("info/update/{documentKey}")
    public ResponseEntity<CustomResponse> updateInformationDocument(@PathVariable(value="documentKey") String documentKey, @RequestBody DocumentModel documentModel){
        boolean status = documentService.updateInformationDocument(documentKey, documentModel);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update failed");
    }

    @PostMapping("content/update/{documentKey}")
    public ResponseEntity<CustomResponse> updateContentDocument(@PathVariable(value="documentKey") String documentKey){
        Document document = documentService.updateContentDocument(documentKey);
        return CustomResponse.generateResponse(HttpStatus.OK, "Update successfully");
    }

    @PostMapping("delete/trash")
    public ResponseEntity<CustomResponse> moveToTrash(@RequestBody List<String> listDocumentKey) {
        boolean status = documentService.moveToTrash(listDocumentKey);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Move To Trash Successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Move To Trash failed");
    }

    @PostMapping("undo")
    public ResponseEntity<CustomResponse> undoDocument(@RequestBody List<String> listDocumentKey){
        boolean status = documentService.undoDocument(listDocumentKey);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Undo Document successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST,  "Undo Document failed");
    }
    @PostMapping ("delete")
    public ResponseEntity<CustomResponse> deleteDocument(@RequestBody List<String> listDocumentKey){
        boolean status = documentService.deleteDocument(listDocumentKey);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete Document successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Delete Document failed");
    }


}
