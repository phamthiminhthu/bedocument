package com.hust.edu.vn.services.document;

import com.hust.edu.vn.dto.UserDto;

import java.util.List;

public interface DocumentShareUserService {
    boolean shareDocument(String documentKey, List<String> emailUsers);

    boolean deleteShareDocument(String documentKey, Long id);

    List<UserDto> getUsersSharedDocuments(String documentKey);
}
