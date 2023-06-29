package com.hust.edu.vn.controller.document;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.services.document.LikeDocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/management/document")
public class LikeDocumentController {

    private final LikeDocumentService likeDocumentService;

    public LikeDocumentController(LikeDocumentService likeDocumentService) {
        this.likeDocumentService = likeDocumentService;
    }

    @PostMapping("like")
    public ResponseEntity<CustomResponse> likeDocument(@RequestParam(value = "document") String documentKey){
        boolean status = likeDocumentService.likeDocument(documentKey);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Like document successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Like document failed");
    }

    @GetMapping("like/show/all")
    public ResponseEntity<CustomResponse> showAllUserLikeDocument(@RequestParam(value = "document") String documentKey){
        List<UserDto> userDtoList = likeDocumentService.showAllUserLikeDocument(documentKey);
        if(userDtoList != null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if(userDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show all user like document", userDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "No one", userDtoList);

    }

    @PostMapping("unlike")
    public ResponseEntity<CustomResponse> unlikeDocument(@RequestParam(value = "document") String documentKey){
        boolean status = likeDocumentService.unlikeDocument(documentKey);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "UnLike document successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "UnLike document failed");
    }
}
