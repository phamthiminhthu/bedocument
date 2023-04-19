package com.hust.edu.vn.services.user;

import com.hust.edu.vn.model.ChangePasswordModel;
import com.hust.edu.vn.model.RecoveryPasswordModel;
import com.hust.edu.vn.model.UserModel;


public interface UserService {

    boolean createAccount(UserModel userModel);

    boolean loginAccount(String email, String password);

    boolean createTokenResetPasswordForUser(String email, String s);

    boolean recoveryPassword(String token, RecoveryPasswordModel recoveryPasswordModel);

    boolean changePassword(ChangePasswordModel recoveryPasswordModel);
}
