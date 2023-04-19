package com.hust.edu.vn.services.document;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentUploadService {
    String uploadFile(MultipartFile file);
}
