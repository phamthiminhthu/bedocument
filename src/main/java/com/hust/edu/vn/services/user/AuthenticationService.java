package com.hust.edu.vn.services.user;

import com.hust.edu.vn.model.ChangePasswordModel;
import com.hust.edu.vn.model.RecoveryPasswordModel;
import com.hust.edu.vn.model.RegisterModel;


public interface AuthenticationService {

    boolean createAccount(RegisterModel registerModel);

    boolean loginAccount(String email, String password);

    boolean createTokenResetPasswordForUser(String email, String s);

    boolean recoveryPassword(String token, RecoveryPasswordModel recoveryPasswordModel);


}
