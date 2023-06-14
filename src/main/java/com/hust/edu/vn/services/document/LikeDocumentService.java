package com.hust.edu.vn.services.document;

import com.hust.edu.vn.dto.UserDto;

import java.util.List;

public interface LikeDocumentService {
    boolean likeDocument(String documentKey);

    List<UserDto> showAllUserLikeDocument(String documentKey);

    boolean unlikeDocument(String documentKey);
}
