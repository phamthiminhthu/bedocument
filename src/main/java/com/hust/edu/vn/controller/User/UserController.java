package com.hust.edu.vn.controller.user;


import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.model.UserModel;
import com.hust.edu.vn.services.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {
    public final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/username/show")
    public ResponseEntity<CustomResponse> getUsernameByEmail(@RequestParam("email") String email){
        String username = userService.getUsernameByToken(email);
        if(username == null){
            return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "Not found user!");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "User exists", username);
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
        UserDto userDto = userService.findByUsername(username);
        if(userDto == null){
            return CustomResponse.generateResponse(HttpStatus.OK, "Not Found");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Found", userDto);
    }
}
