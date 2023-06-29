package com.hust.edu.vn.services.user;

import com.hust.edu.vn.dto.UserDto;

import java.util.List;

public interface FollowService {
    boolean followUser(String username);

    boolean unfollowUser(String username);

    List<UserDto> getListFollower(String username);

    List<UserDto> getListUserFollowing(String username);

    int getListStatusFollowing(String username);
}
