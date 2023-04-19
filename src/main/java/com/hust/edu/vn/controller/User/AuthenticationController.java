package com.hust.edu.vn.controller.User;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.model.ChangePasswordModel;
import com.hust.edu.vn.model.RecoveryPasswordModel;
import com.hust.edu.vn.model.UserModel;
import com.hust.edu.vn.services.user.UserService;
import com.hust.edu.vn.services.impl.user.CustomUserDetailServices;
import com.hust.edu.vn.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthenticationController {
    private final UserService userService ;
    private final JwtUtils jwtUtils;

    private final CustomUserDetailServices userDetailsService;

    @Autowired
    public AuthenticationController(
            UserService userService,
            JwtUtils jwtUtils, CustomUserDetailServices userDetailsService) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }


    @PostMapping("sign-up")
    public ResponseEntity<CustomResponse> signUp(@ModelAttribute UserModel userModel){
        CustomResponse resResult = new CustomResponse();
        resResult.setResponseCode(HttpStatus.BAD_REQUEST.value());
        boolean status = userService.createAccount(userModel);
        if(status){
            final UserDetails userDetail = userDetailsService.loadUserByUsername(userModel.getEmail());
            if (userDetail != null && userDetail.isEnabled()) {
                return CustomResponse.generateResponse(HttpStatus.CREATED, jwtUtils.generateToken(userDetail));
            }
        }
        return CustomResponse.generateResponse(status);

   }


   record LoginModel(String email, String password){
        public String getEmail(){
            return email;
        }
        public String getPassword(){
            return password;
        }

   }

   @PostMapping("login")
    public ResponseEntity<CustomResponse> login(@ModelAttribute LoginModel user){
        boolean status = userService.loginAccount(user.getEmail(), user.getPassword());
        if(status){
            final UserDetails userDetail = userDetailsService.loadUserByUsername(user.getEmail());
            String token = jwtUtils.generateToken(userDetail);
            return CustomResponse.generateResponse(HttpStatus.OK, token);
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST);
   }


    @PostMapping("reset-password")
    public ResponseEntity<CustomResponse> resetPassword(@ModelAttribute("email") String email, HttpServletRequest servletRequest) {
        boolean status = userService.createTokenResetPasswordForUser(email, applicationUrl(servletRequest));
        return CustomResponse.generateResponse(status);
    }


    @PostMapping("change-password-by-token")
    public ResponseEntity<CustomResponse> changePasswordByToken(@RequestParam("token") String token, @RequestBody RecoveryPasswordModel recoveryPasswordModel){
        boolean status = userService.recoveryPassword(token, recoveryPasswordModel);
        return CustomResponse.generateResponse(status);
    }

    private String applicationUrl(HttpServletRequest servletRequest) {
//        SecurityContextHolder.getContext().getAuthentication().getName():
        return "https://"
                + servletRequest.getServerName()
                + ":"
                + servletRequest.getServerPort()
                + servletRequest.getContextPath();
    }

    @PostMapping("change-password-account")
    public ResponseEntity<CustomResponse> changePasswordAccount(@RequestBody ChangePasswordModel changePasswordModel){
        boolean status = userService.changePassword(changePasswordModel);
        return CustomResponse.generateResponse(status);
    }





}
