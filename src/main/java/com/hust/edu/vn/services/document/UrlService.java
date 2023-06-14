package com.hust.edu.vn.services.document;

import com.hust.edu.vn.dto.UrlDto;
import com.hust.edu.vn.model.UrlModel;

import java.util.List;

public interface UrlService {
    boolean createUrl(String documentKey, UrlModel urlModel);

    List<UrlDto> showAllUrl(String documentKey);

    boolean updateUrl(Long id, UrlModel urlModel);

    boolean deleteUrl(Long id);
}
