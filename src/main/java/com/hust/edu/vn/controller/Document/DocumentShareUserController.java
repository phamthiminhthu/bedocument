package com.hust.edu.vn.controller.document;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.UserDto;
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

    // doing check ~~ share document for list username
    @PostMapping("/")
    public ResponseEntity<CustomResponse> shareDocument(@RequestParam(value = "documentKey") String documentKey, @RequestBody List<String> emailUsers, HttpServletRequest httpServletRequest) {
        boolean status = documentShareUserService.shareDocument(documentKey, emailUsers);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Share document successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Share document failed");
    }

    @GetMapping("/users/all")
    public ResponseEntity<CustomResponse> getUsersSharedDocuments(@RequestParam(value = "documentKey") String documentKey) {
        List<UserDto> userDtoList = documentShareUserService.getUsersSharedDocuments(documentKey);
        if (userDtoList == null) {
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Not access document");
        }
        if (userDtoList.size() > 0) {
            return CustomResponse.generateResponse(HttpStatus.OK, "List User", userDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "No users");
    }

    // doing check ~~ delete share of users
    @PostMapping("delete/user")
    public ResponseEntity<CustomResponse> deleteShareDocument(@RequestParam(value = "documentKey") String documentKey,
                                                              @RequestParam(value = "id") Long id) {
        boolean status = documentShareUserService.deleteShareDocument(documentKey,  id);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete share document successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Delete share document Failed");
    }

    // todo: doing check ~~ kieu data tra ve
//    @GetMapping("read/{documentKey}")
//    public ResponseEntity<byte[]> readDocument(@PathVariable(value = "documentKey") String documentKey){
//        byte[] data = documentShareUserService.loadFileFromS3(documentKey);
//        if(data != null ){
//            HttpHeaders httpHeaders = new HttpHeaders();
//            httpHeaders.setContentType(MediaType.APPLICATION_PDF);
//            return ResponseEntity.ok().headers(httpHeaders).body(data);
//        }
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
//    }
//    private String applicationUrl(HttpServletRequest servletRequest ) {
//        return "https://"
//                + servletRequest.getServerName()
//                + ":"
//                + servletRequest.getServerPort()
//                + servletRequest.getContextPath();
//    }
}
