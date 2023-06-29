package com.hust.edu.vn.controller.user;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.services.user.FollowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("follow")
    public ResponseEntity<CustomResponse> followUser(@RequestParam(value = "username") String username){
        boolean status = followService.followUser(username);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Follow user successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Follow failed");
    }

    @PostMapping("unfollow")
    public ResponseEntity<CustomResponse> unfollowUser(@RequestParam(value="username") String username){
        boolean status = followService.unfollowUser(username);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "UnFollow user successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "UnFollow failed");
    }

    @GetMapping("follower")
    public ResponseEntity<CustomResponse> getFollower(@RequestParam(value="username") String username){
        List<UserDto> userDtoList = followService.getListFollower(username);
        if(userDtoList == null) {
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Please login account");
        }
        if(userDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "List follower", userDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Don't have no one", userDtoList);
    }

    @GetMapping("following")
    public ResponseEntity<CustomResponse> getUserFollowing(@RequestParam(value="username") String username){
        List<UserDto> userDtoList = followService.getListUserFollowing(username);
        if(userDtoList == null) {
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Please login account");
        }
        if(userDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "List following", userDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Don't have no one", userDtoList);
    }

    @GetMapping("status/following")
    public ResponseEntity<CustomResponse> getStatusFollowing(@RequestParam(value="username") String username){
        int status = followService.getListStatusFollowing(username);
        if(status == 0) {
            return CustomResponse.generateResponse(HttpStatus.OK, "Follow");
        }
        if(status == 1){
            return CustomResponse.generateResponse(HttpStatus.OK, "Unfollow");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Edit Profile");
    }

}
