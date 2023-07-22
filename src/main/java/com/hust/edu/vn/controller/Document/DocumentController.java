package com.hust.edu.vn.controller.document;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.model.DocumentEditModel;
import com.hust.edu.vn.model.DocumentModel;
import com.hust.edu.vn.services.document.DocumentService;
import com.hust.edu.vn.utils.ModelMapperUtils;
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
    private final ModelMapperUtils modelMapperUtils;

    @Autowired
    public DocumentController(DocumentService documentService, ModelMapperUtils modelMapperUtils) {
        this.documentService = documentService;
        this.modelMapperUtils = modelMapperUtils;
    }

    @PostMapping("upload")
    public ResponseEntity<CustomResponse> uploadDocument(@ModelAttribute(value = "file") MultipartFile file) {
        Document document = documentService.uploadDocument(file);
        DocumentDto documentDto = modelMapperUtils.mapAllProperties(document, DocumentDto.class);
        return CustomResponse.generateResponse(HttpStatus.OK, "Upload successfully", documentDto);
    }
    @GetMapping("show/all")
    public ResponseEntity<CustomResponse> getListDocument(){
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
    public ResponseEntity<CustomResponse> getDocumentDetails(@PathVariable("documentKey") String documentKey){
        DocumentDto documentModel = documentService.getDocumentModel(documentKey);
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

    @PostMapping("edit")
    public ResponseEntity<CustomResponse> editDocumentByKey(@RequestParam(value = "documentKey") String documentKey, @RequestBody DocumentEditModel documentEditModel){
        boolean status = documentService.editDocumentByKey(documentKey, documentEditModel);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update failed");
    }

    @PostMapping("update/{documentKey}")
    public ResponseEntity<CustomResponse> updateInformationDocument(@PathVariable(value="documentKey") String documentKey, @RequestBody DocumentModel documentModel){
        boolean status = documentService.updateInformationDocument(documentKey, documentModel);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update failed");
    }
    @PostMapping("update")
    public ResponseEntity<CustomResponse> updateInformationListDocument(@RequestBody List<DocumentModel> listDocumentModel){
        boolean status = documentService.updateInformationListDocument(listDocumentModel);
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
    public ResponseEntity<CustomResponse> moveToTrash(@RequestBody List<String> listDocumentKey) {
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
   public ResponseEntity<CustomResponse> getListDocumentPublic(){
        List<DocumentDto> documents = documentService.getListDocumentPublic();
        if(documents == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if (documents.size() == 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Don't have documents. Please add more documents", documents);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "List all document", documents);
   }

   @GetMapping("show/public/by-username")
   public ResponseEntity<CustomResponse> getListDocumentPublicUserName(@RequestParam("username") String username){
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
   public ResponseEntity<CustomResponse>  getListDocumentPublicFollowing(){
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
   public ResponseEntity<CustomResponse> getListDocumentPublicSuggest(){
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
   public ResponseEntity<CustomResponse> getListSuggestUsers(){
        List<UserDto> usersSuggest = documentService.getListSuggestUsers();
        if(usersSuggest == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if (usersSuggest.size() == 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Don't have documents. Please add more documents", usersSuggest);
        }
       return CustomResponse.generateResponse(HttpStatus.OK, "List user suggest", usersSuggest);
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

    @GetMapping("loved")
    public ResponseEntity<CustomResponse> getListDocumentLoved(){
        List<DocumentDto> documentDtoList = documentService.getListDocumentLoved();
        if(documentDtoList != null && !documentDtoList.isEmpty()){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show Document loved successfully", documentDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "Show Document loved failed");
    }

    @GetMapping("shared/me")
    public ResponseEntity<CustomResponse> getListDocumentShared(){
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
    public ResponseEntity<CustomResponse> getListDocumentCompleted(){
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

    @PostMapping("update/public")
    public ResponseEntity<CustomResponse> updatePublicDocument(@ModelAttribute(value = "documentKey") String documentKey){
        boolean status = documentService.updatePublicDocument(documentKey);
        if (status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update public document successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update Loved document failed");

    }


}
