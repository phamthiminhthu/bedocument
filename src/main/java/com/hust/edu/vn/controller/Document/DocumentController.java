package com.hust.edu.vn.controller.Document;

import com.hust.edu.vn.services.document.DocumentUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/api/v1/management/document")
public class DocumentController {

    private final DocumentUploadService documentUploadService;

    @Autowired
    public DocumentController(DocumentUploadService documentUploadService) {
        this.documentUploadService = documentUploadService;
    }

    @PostMapping("upload-docs")
    public String uploadDocument(@ModelAttribute(value = "file") MultipartFile file) {
        return this.documentUploadService.uploadFile(file);
    }

}
