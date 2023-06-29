package com.hust.edu.vn.services.document;

import com.hust.edu.vn.dto.UserDto;

import java.util.List;

public interface DocumentShareUserService {
    boolean shareDocument(String documentKey, List<String> emailUsers, String link);

    byte[] loadFileFromS3(String documentKey);

    boolean deleteShareDocument(String documentKey, Long id);

    List<UserDto> getUsersSharedDocuments(String documentKey);
}
