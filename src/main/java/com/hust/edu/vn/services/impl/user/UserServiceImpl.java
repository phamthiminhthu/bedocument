package com.hust.edu.vn.services.impl.user;

import com.hust.edu.vn.entity.ResetPasswordToken;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.model.ChangePasswordModel;
import com.hust.edu.vn.model.RecoveryPasswordModel;
import com.hust.edu.vn.model.UserModel;
import com.hust.edu.vn.repository.ResetPasswordTokenRepository;
import com.hust.edu.vn.repository.UserRepository;
import com.hust.edu.vn.services.user.EmailService;
import com.hust.edu.vn.services.user.UserService;
import com.hust.edu.vn.utils.ModelMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;

    private final UserRepository userRepository;
    private final ModelMapperUtils modelMapperUtils;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;



    @Autowired
    public UserServiceImpl(UserRepository userRepository, ModelMapperUtils modelMapperUtils, PasswordEncoder passwordEncoder,
                           ResetPasswordTokenRepository resetPasswordTokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.passwordEncoder = passwordEncoder;
        this.resetPasswordTokenRepository = resetPasswordTokenRepository;
        this.emailService = emailService;
    }

//    private Collection<? extends GrantedAuthority> getAuthorities(List<String> roles) {
//        List<GrantedAuthority> authorities = new ArrayList<>();
//        for(String role: roles){
//            authorities.add(new SimpleGrantedAuthority(role));
//        }
//        return authorities;
//    }

    @Override
    public boolean createAccount(UserModel userModel) {
        if(userRepository.existsByEmail(userModel.getEmail()) || userRepository.existsByUsername(userModel.getUsername())){
            return false;
        }
        String rootPath = UUID.randomUUID() + "/";
        User user = modelMapperUtils.mapAllProperties(userModel,User.class);
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));
        user.setRootPath(rootPath);
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean loginAccount(String email, String password) {
        User user = userRepository.findByEmail(email);
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public boolean createTokenResetPasswordForUser(String email, String applicationUrl) {
        User user = userRepository.findByEmail(email);
        if (user == null) return false;
        String token = UUID.randomUUID().toString();
        ResetPasswordToken passwordResetToken = new ResetPasswordToken(token, user);
        resetPasswordTokenRepository.save(passwordResetToken);
        return sendPasswordResetTokenMail(user, applicationUrl, token);
    }

    private boolean sendPasswordResetTokenMail(User user, String applicationUrl, String token) {
        String url = applicationUrl
                + "/api/v1/auth/change-password-by-token?token="
                + token;
        return emailService.sendSimpleMessage(user.getEmail(),"Kích hoạt tài khoản",url);
    }

    @Override
    public boolean recoveryPassword(String token, RecoveryPasswordModel recoveryPasswordModel) {
        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findByToken(token);
        if(resetPasswordToken == null) return false;
        User user = resetPasswordToken.getUser();
        user.setPassword(passwordEncoder.encode(recoveryPasswordModel.getPassword()));
        userRepository.save(user);
        resetPasswordTokenRepository.deleteById(resetPasswordToken.getId());
        return true;
    }

    @Override
    public boolean changePassword(ChangePasswordModel changePasswordModel) {
        User user = userRepository.findByEmail(changePasswordModel.getEmail());
        if (user == null
                || !passwordEncoder.matches(changePasswordModel.getOldPassword(), user.getPassword())
                || !changePasswordModel.getNewPassword().equals(changePasswordModel.getConfirmPassword())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(changePasswordModel.getNewPassword()));
        userRepository.save(user);
        return true;
    }


}
