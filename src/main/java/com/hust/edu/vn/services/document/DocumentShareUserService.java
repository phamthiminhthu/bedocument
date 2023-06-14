package com.hust.edu.vn.services.document;

import java.util.List;

public interface DocumentShareUserService {
    boolean shareDocument(String documentKey, List<String> emailUsers, String link);

    byte[] loadFileFromS3(String documentKey);

    boolean deleteShareDocument(String documentKey, List<String> emailUsers);
}
