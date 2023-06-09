package com.hust.edu.vn.services.user;

import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.model.ChangePasswordModel;
import com.hust.edu.vn.model.UserModel;

public interface UserService {
    UserDto getInfoProfile(String username);
    boolean updateProfile(UserModel userModel);
    UserDto findByUsername(String username);

    UserDto getInformationByToken();

    boolean changePassword(ChangePasswordModel changePasswordModel);
}
