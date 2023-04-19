package com.hust.edu.vn.controller.User;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.model.ProfilePublicModel;
import com.hust.edu.vn.services.user.ProfilePublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/")
public class ProfilePublicController {
    private final ProfilePublicService profilePublicService;

    @Autowired
    public ProfilePublicController(ProfilePublicService profilePublicService) {
        this.profilePublicService = profilePublicService;
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<CustomResponse> displayProfile(@PathVariable String username){
        ProfilePublicModel user = profilePublicService.getProfileModel(username);
        if(user == null){
            return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "Not found!");
        }
        return CustomResponse.generateResponse(HttpStatus.FOUND, user);

    }
}
