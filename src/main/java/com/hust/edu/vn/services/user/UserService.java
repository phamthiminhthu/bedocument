package com.hust.edu.vn.services.user;

import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.model.ChangePasswordModel;
import com.hust.edu.vn.model.UserModel;

import java.util.List;

public interface UserService {
    UserDto getInfoProfile(String username);
    boolean updateProfile(UserModel userModel);
    UserDto getInformationByToken();

    boolean changePassword(ChangePasswordModel changePasswordModel);
    List<UserDto> findUsersByUsernameOrFullName(String username);

    UserDto findByUserByEmail(String email);
}
