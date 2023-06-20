package com.hust.edu.vn.controller.document;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.UrlDto;
import com.hust.edu.vn.model.UrlModel;
import com.hust.edu.vn.services.document.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/management/document/url")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("create")
    public ResponseEntity<CustomResponse> createUrl(@RequestParam(value="documentKey") String documentKey, @RequestBody UrlModel urlModel){
        boolean status = urlService.createUrl(documentKey, urlModel);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Created successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Created failed");
    }

    @GetMapping("show/all")
    public ResponseEntity<CustomResponse> showAllUrl(@RequestParam(value="documentKey") String documentKey){
        List<UrlDto> urlDtoList = urlService.showAllUrl(documentKey);
        if(urlDtoList == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if(urlDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "List url", urlDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Empty", urlDtoList);
    }

    @PostMapping("update/{id}")
    public ResponseEntity<CustomResponse> updateUrl(@PathVariable long id, @RequestBody UrlModel urlModel){
        boolean status = urlService.updateUrl(id, urlModel);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Url updated successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Url updated failed");
    }

    @PostMapping("delete/{id}")
    public ResponseEntity<CustomResponse> deleteUrl(@PathVariable Long id){
        boolean status = urlService.deleteUrl(id);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Url delete successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Url delete failed");
    }
}
