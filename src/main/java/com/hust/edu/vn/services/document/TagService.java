package com.hust.edu.vn.services.document;


import com.amazonaws.services.applicationinsights.model.Tag;
import com.hust.edu.vn.dto.TagDto;

import java.util.List;

public interface TagService {
    boolean createTag(String documentKey, String tagName);

    boolean updateTag(String documentKey, Long id, String tagName);

    boolean deleteTag(String documentKey, String tagName);

    List<TagDto> showAllTag(String documentKey);
}
