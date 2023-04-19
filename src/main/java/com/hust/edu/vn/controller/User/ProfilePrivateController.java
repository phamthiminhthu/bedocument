package com.hust.edu.vn.controller.User;


import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.model.ProfilePrivateModel;
import com.hust.edu.vn.services.user.ProfilePrivateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/info")
@Slf4j
public class ProfilePrivateController {
    public final ProfilePrivateService profilePrivateService;


    @Autowired
    public ProfilePrivateController(ProfilePrivateService profilePrivateService) {
        this.profilePrivateService = profilePrivateService;
    }

    @GetMapping("profile/{username}")
    public ResponseEntity<CustomResponse> getInfoProfile(@PathVariable String username){
        ProfilePrivateModel profileModel = profilePrivateService.getInfoProfile(username);
        if(profileModel == null){
            return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "Not found user!");
        }
        return CustomResponse.generateResponse(HttpStatus.FOUND, "User exists", profileModel);
    }

    @PostMapping("profile/{id}")
    public ResponseEntity<CustomResponse> updateProfile(@PathVariable Long id, @ModelAttribute ProfilePrivateModel profilePrivateModel){
        boolean status = profilePrivateService.updateProfile(id, profilePrivateModel);
        if(status) return CustomResponse.generateResponse(HttpStatus.OK, "Update user successfully");
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update user failed");
    }

}
