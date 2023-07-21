package com.hust.edu.vn.controller.user;


import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.model.ChangePasswordModel;
import com.hust.edu.vn.model.UserModel;
import com.hust.edu.vn.services.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {
    public final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("profile/{username}")
    public ResponseEntity<CustomResponse> getInfoProfile(@PathVariable String username){
        UserDto userDto = userService.getInfoProfile(username);
        if(userDto == null){
            return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "Not found user!");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "User exists", userDto);
    }

    @PostMapping("update")
    public ResponseEntity<CustomResponse> updateProfile(@ModelAttribute UserModel userModel){
        boolean status = userService.updateProfile(userModel);
        if(status) return CustomResponse.generateResponse(HttpStatus.OK, "Update user successfully");
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update user failed");
    }

    @GetMapping("find/username")
    public ResponseEntity<CustomResponse> findByUsername(@RequestParam(value="username") String username){
        List<UserDto> userDtos = userService.findUsersByUsernameOrFullName(username);
        if(userDtos == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Please login");
        }
        if(userDtos.size() > 0) {
            return CustomResponse.generateResponse(HttpStatus.OK, "Found", userDtos);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Empty", userDtos);
    }

    @GetMapping("find/by-email")
    public ResponseEntity<CustomResponse> findUserByEmail(@RequestParam(value="email") String email){
        UserDto userDto = userService.findByUserByEmail(email);
        if(userDto != null) {
            return CustomResponse.generateResponse(HttpStatus.OK, "Found", userDto);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Not Found", null);
    }


    @GetMapping("information/by-token")
    public ResponseEntity<CustomResponse> getInfo(){
        UserDto userDto = userService.getInformationByToken();
        if(userDto == null){
            return CustomResponse.generateResponse(HttpStatus.OK, "Not Found");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Found", userDto);
    }
    @PostMapping("change-password-account")
    public ResponseEntity<CustomResponse> changePasswordAccount(@RequestBody ChangePasswordModel changePasswordModel){
        boolean status = userService.changePassword(changePasswordModel);
        return CustomResponse.generateResponse(status);
    }

}
