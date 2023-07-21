package com.hust.edu.vn.controller.document;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.dto.TagDto;
import com.hust.edu.vn.services.document.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/management/document/tag")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping("create")
    public ResponseEntity<CustomResponse> createTag(@RequestParam("documentKey") String documentKey, @ModelAttribute(value = "tagName") String tagName) {
        boolean status = tagService.createTag(documentKey, tagName);
        if (status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Created successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Created Failed");
    }

    record TagModel(Long id, String tagName) {
        public Long getId(){
            return id;
        }
        public String getTagName(){
            return tagName;
        }
    }

    @PostMapping("update")
    public ResponseEntity<CustomResponse> updateTag(@RequestParam("documentKey") String documentKey, @ModelAttribute TagModel tagModel){
        boolean status = tagService.updateTag(documentKey, tagModel.getId(), tagModel.getTagName());
        if (status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update Failed");
    }

    @PostMapping("delete")
    public ResponseEntity<CustomResponse> deleteTag(@RequestParam("documentKey") String documentKey, @ModelAttribute(value = "tagName") String tagName){
        boolean status = tagService.deleteTag(documentKey, tagName);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Created Failed");
    }

    @GetMapping("show/all")
    public ResponseEntity<CustomResponse> showAllTag(@RequestParam("documentKey") String documentKey){
        List<TagDto> listTag = tagService.showAllTag(documentKey);
        if(listTag == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if(listTag.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show all successfully", listTag);
        }
        return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "No Tag", listTag);
    }
//
//    @GetMapping("public/show/all")
//    public ResponseEntity<CustomResponse> showAllTagPublic(@RequestParam("documentKey") String documentKey){
//        List<TagDto> listTag = tagService.showAllTagPublic(documentKey);
//        if(listTag == null){
//            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
//        }
//        if(listTag.size() > 0){
//            return CustomResponse.generateResponse(HttpStatus.OK, "Show all successfully", listTag);
//        }
//        return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "No Tag", listTag);
//    }

    @GetMapping("find/documents/all")
    public ResponseEntity<CustomResponse> showAllDocumentsByTag(@RequestParam("tagName") String tagName){
        List<DocumentDto> documentDtoList = tagService.findDocumentsByTag(tagName);
        if(documentDtoList == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if(documentDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show tag successfully", documentDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Tag Empty", documentDtoList);
    }

    @GetMapping("find/documents/public")
    public ResponseEntity<CustomResponse> showAllDocumentsPublicByTag(@RequestParam("tagName") String tagName){
        List<DocumentDto> documentDtoList = tagService.findDocumentsPublicByTag(tagName);
        if(documentDtoList == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if(documentDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show tag successfully", documentDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Tag Empty", documentDtoList);
    }

}
