package com.hust.edu.vn.services.user;

import com.hust.edu.vn.model.ProfilePrivateModel;
import org.springframework.web.multipart.MultipartFile;

public interface ProfilePrivateService {
    ProfilePrivateModel getInfoProfile(String username);

    boolean updateProfile(Long id, ProfilePrivateModel profilePrivateModel);

}
