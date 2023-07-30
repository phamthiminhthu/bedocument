package com.hust.edu.vn.controller.document;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.model.DocumentEditModel;
import com.hust.edu.vn.services.document.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
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
        DocumentDto documentDto = documentService.uploadDocument(file);
        if(documentDto == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Upload Failed");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Upload successfully", documentDto);
    }
    @GetMapping("show/all")
    public ResponseEntity<CustomResponse> showAllDocument(){
        List<DocumentDto> documents = documentService.getListDocument();
        if(documents == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if (documents.size() == 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Don't have documents. Please add more documents", documents);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "List all document", documents);
    }
    @GetMapping("show/details/{documentKey}")
    public ResponseEntity<CustomResponse> showDocumentDetails(@PathVariable("documentKey") String documentKey){
        DocumentDto documentModel = documentService.getDocumentModel(documentKey);
        if(documentModel != null){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show document details successfully", documentModel);
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Document doesn't exist");
    }
    @GetMapping("display/{filename}")
    public ResponseEntity<byte[]> displayFileDocument(@PathVariable("filename") String filename) {
        byte[] data = documentService.loadFileFromS3(filename);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_PDF);
        return ResponseEntity.ok().headers(httpHeaders).body(data);
    }

    @PostMapping("edit")
    public ResponseEntity<CustomResponse> updateDocument(@RequestParam(value = "documentKey") String documentKey, @RequestBody DocumentEditModel documentEditModel){
        boolean status = documentService.editDocumentByKey(documentKey, documentEditModel);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update failed");
    }
//
//    @PostMapping("update/{documentKey}")
//    public ResponseEntity<CustomResponse> updateInformationDocument(@PathVariable(value="documentKey") String documentKey, @RequestBody DocumentModel documentModel){
//        boolean status = documentService.updateInformationDocument(documentKey, documentModel);
//        if(status){
//            return CustomResponse.generateResponse(HttpStatus.OK, "Update successfully");
//        }
//        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update failed");
//    }
    @PostMapping("update/multi-documents/loved")
    public ResponseEntity<CustomResponse> updateLovedListDocument(@RequestBody List<String> listDocumentKeys){
        boolean status = documentService.updateLovedListDocuments(listDocumentKeys);
        if (status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update Failed");
    }

    @PostMapping("update/multi-documents/public")
    public ResponseEntity<CustomResponse> updatePublicListDocument(@RequestBody List<String> listDocumentKeys){
        boolean status = documentService.updatePublicListDocuments(listDocumentKeys);
        if (status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update Failed");
    }

    @PostMapping("update/multi-documents/completed")
    public ResponseEntity<CustomResponse> updateCompletedListDocument(@RequestBody List<String> listDocumentKeys){
        boolean status = documentService.updateCompletedListDocument(listDocumentKeys);
        if (status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update Failed");
    }

    // todo: update document content on s3 bucket
    @PostMapping("content/update/{documentKey}")
    public ResponseEntity<CustomResponse> updateContentDocument(@PathVariable(value="documentKey") String documentKey){
        Document document = documentService.updateContentDocument(documentKey);
        return CustomResponse.generateResponse(HttpStatus.OK, "Update successfully", document);
    }

    @PostMapping("delete/trash")
    public ResponseEntity<CustomResponse> moveDocumentToTrash(@RequestBody List<String> listDocumentKey) {
        boolean status = documentService.moveToTrash(listDocumentKey);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Move To Trash Successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Move To Trash failed");
    }

    @GetMapping("trash/show/all")
    public ResponseEntity<CustomResponse> showDocumentOnTrash(){
        List<DocumentDto> documents = documentService.getTrashListDocument();
        if(documents == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if(documents.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show list on trash", documents);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "No empty", documents);
    }

   @GetMapping("show/public")
   public ResponseEntity<CustomResponse> showListDocumentPublic(){
        List<DocumentDto> documents = documentService.getListDocumentPublic();
        if(documents == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if (documents.size() == 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Don't have documents. Please add more documents", documents);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "List all document show successfully", documents);
   }

   @GetMapping("show/public/by-username")
   public ResponseEntity<CustomResponse> showListDocumentPublicUserName(@RequestParam("username") String username){
       List<DocumentDto> documents = documentService.getListDocumentPublicByUsername(username);
       if(documents == null){
           return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "No existed");
       }
       if (documents.size() == 0){
           return CustomResponse.generateResponse(HttpStatus.OK, "Don't have documents. Please add more documents", documents);
       }
       return CustomResponse.generateResponse(HttpStatus.OK, "List all document", documents);
   }

   @GetMapping("following/show/all")
   public ResponseEntity<CustomResponse> showListDocumentPublicFollowing(){
       List<DocumentDto> documents = documentService.getListDocumentPublicFollowing();
       if(documents == null){
           return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
       }
       if (documents.size() == 0){
           return CustomResponse.generateResponse(HttpStatus.OK, "Don't have documents. Please add more documents", documents);
       }
       return CustomResponse.generateResponse(HttpStatus.OK, "List all document", documents);
   }

   @GetMapping("suggest/show/all")
   public ResponseEntity<CustomResponse> showListDocumentsSuggested(){
        List<DocumentDto> documents = documentService.getListDocumentPublicSuggest();
        if(documents == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if (documents.size() == 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Don't have documents. Please add more documents", documents);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "List all document", documents);
   }

   @GetMapping("suggest/user/show/all")
   public ResponseEntity<CustomResponse> showListUsersSuggested(){
        List<UserDto> usersSuggest = documentService.getListSuggestUsers();
        if(usersSuggest == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if (usersSuggest.size() == 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Don't have users suggested", usersSuggest);
        }
       return CustomResponse.generateResponse(HttpStatus.OK, "List user suggested", usersSuggest);
   }

    @PostMapping("undo")
    public ResponseEntity<CustomResponse> putBackDocumentsFromTrash(@RequestBody List<String> listDocumentKey){
        boolean status = documentService.undoDocument(listDocumentKey);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Undo Documents successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST,  "Undo Documents failed");
    }
    @PostMapping ("delete")
    public ResponseEntity<CustomResponse> deleteDocument(@RequestBody List<String> listDocumentKey){
        boolean status = documentService.deleteDocument(listDocumentKey);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete Documents successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Delete Documents failed");
    }

    @GetMapping("loved")
    public ResponseEntity<CustomResponse> showListDocumentLoved(){
        List<DocumentDto> documentDtoList = documentService.getListDocumentLoved();
        if(documentDtoList == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Show Document loved failed");
        }
        if(documentDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show Document loved successfully", documentDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Document Loved Empty", documentDtoList);
    }

    @GetMapping("shared/me")
    public ResponseEntity<CustomResponse> showListDocumentShared(){
        List<DocumentDto> documentDtoList = documentService.getListDocumentShared();
        if(documentDtoList == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if(documentDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show Document shared successfully", documentDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Empty", documentDtoList);
    }

    @GetMapping("completed/show/all")
    public ResponseEntity<CustomResponse> showListDocumentCompleted(){
        List<DocumentDto> documentDtoList = documentService.getListDocumentCompleted();
        if(documentDtoList == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if(documentDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show Document completed successfully", documentDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Empty", documentDtoList);
    }

    @PostMapping("loved/update")
    public ResponseEntity<CustomResponse> updateLovedDocument(@RequestParam(value = "documentKey") String documentKey){
        boolean status = documentService.updateLovedDocument(documentKey);
        if (status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update Loved document successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update Loved document failed");
    }

//    @GetMapping("show/tags")
//    public ResponseEntity<CustomResponse> showAllTagsDocument(){
//        List<String> status = documentService.getListTypeDocumentsSuggested();
//        return CustomResponse.generateResponse(HttpStatus.OK, "Update Loved document failed", status);
//    }
//    @PostMapping("update/public")
//    public ResponseEntity<CustomResponse> updatePublicDocument(@ModelAttribute(value = "documentKey") String documentKey){
//        boolean status = documentService.updatePublicDocument(documentKey);
//        if (status){
//            return CustomResponse.generateResponse(HttpStatus.OK, "Update public document successfully");
//        }
//        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update Loved document failed");
//
//    }
//
//    @PostMapping("hash/file")
//    public ResponseEntity<CustomResponse> calculateSHA256Hash(@ModelAttribute(value = "file") MultipartFile file){
//        String dataHash = documentService.calculateSHA256Hash(file);
//        if (dataHash != null){
//            return CustomResponse.generateResponse(HttpStatus.OK, "Hash ok", dataHash);
//        }
//        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Hash failed");
//    }


}
