package com.hust.edu.vn.controller.document;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.services.document.DocumentShareUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/management/document/share")
@Slf4j
public class DocumentShareUserController {

    private final DocumentShareUserService documentShareUserService;

    public DocumentShareUserController(DocumentShareUserService documentShareUserService) {
        this.documentShareUserService = documentShareUserService;
    }

    record DocumentShare( String documentKey, List<String> emailUsers){
        public String getDocumentKey(){
            return documentKey;
        }
        public List<String> getEmailUsers(){
            return emailUsers;
        }

    }


    // doing check ~~ share document for list username
    @PostMapping("/")
    public ResponseEntity<CustomResponse> shareDocument(@RequestBody DocumentShare documentShare, HttpServletRequest httpServletRequest) {
        boolean status = documentShareUserService.shareDocument(documentShare.getDocumentKey(), documentShare.getEmailUsers(), applicationUrl(httpServletRequest));
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Share document successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Share document failed");
    }

    // doing check ~~ delete share of users
    @PostMapping("delete")
    public ResponseEntity<CustomResponse> deleteShareDocument(@RequestBody DocumentShare documentShare) {
        boolean status = documentShareUserService.deleteShareDocument(documentShare.getDocumentKey(), documentShare.getEmailUsers());
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete share document successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Delete share document Failed");

    }

    // todo: doing check ~~ kieu data tra ve
    @GetMapping("read/{documentKey}")
    public ResponseEntity<byte[]> readDocument(@PathVariable(value = "documentKey") String documentKey){
        byte[] data = documentShareUserService.loadFileFromS3(documentKey);
        if(data != null ){
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_PDF);
            return ResponseEntity.ok().headers(httpHeaders).body(data);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }
    private String applicationUrl(HttpServletRequest servletRequest ) {
        return "https://"
                + servletRequest.getServerName()
                + ":"
                + servletRequest.getServerPort()
                + servletRequest.getContextPath();
    }
}
