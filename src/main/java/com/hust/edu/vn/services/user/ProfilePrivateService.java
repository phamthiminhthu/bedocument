package com.hust.edu.vn.services.user;

import com.hust.edu.vn.model.ProfilePrivateModel;

public interface ProfilePrivateService {
    ProfilePrivateModel getInfoProfile(String username);

    boolean updateProfile(Long id, ProfilePrivateModel profilePrivateModel);
}
