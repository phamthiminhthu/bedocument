package com.hust.edu.vn.services.impl.user;

import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.entity.Follow;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.repository.FollowRepository;
import com.hust.edu.vn.repository.UserRepository;
import com.hust.edu.vn.services.user.FollowService;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FollowServiceImpl implements FollowService {

    private final BaseUtils baseUtils;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ModelMapperUtils modelMapperUtils;

    public FollowServiceImpl(BaseUtils baseUtils, FollowRepository followRepository,
                             UserRepository userRepository, ModelMapperUtils modelMapperUtils) {
        this.baseUtils = baseUtils;
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.modelMapperUtils = modelMapperUtils;
    }

    @Override
    public boolean followUser(String username) {
        User user = baseUtils.getUser();
        if (user != null) {
            User followingUser = userRepository.findByUsername(username);
            if (followingUser != null){
                if (!followRepository.existsByFollowingIdAndFollower(followingUser.getId(), user)){
                    Follow follow = new Follow();
                    follow.setFollowingId(followingUser.getId());
                    follow.setFollower(user);
                    followRepository.save(follow);
                    return true;
                }
                return false;

            }
            return false;
        }
        return false;
    }

    @Override
    public boolean unfollowUser(String username) {
        User user = baseUtils.getUser();
        if (user != null){
            User followingUser = userRepository.findByUsername(username);
            if(followingUser != null){
                if(followRepository.existsByFollowingIdAndFollower(followingUser.getId(), user)){
                    Follow follow = followRepository.findByFollowingIdAndFollower(followingUser.getId(), user);
                    followRepository.delete(follow);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    @Override
    public List<UserDto> getListFollower(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null){
            List<Follow> listFollower = followRepository.findByFollowingId(user.getId());
            List<UserDto> userDtoList = new ArrayList<>();
            if(listFollower != null && !listFollower.isEmpty()){
                for (Follow follow : listFollower){
                    userDtoList.add(modelMapperUtils.mapAllProperties(follow.getFollower(), UserDto.class));
                }
            }
            return userDtoList;
        }
        return null;
    }

    @Override
    public List<UserDto> getListUserFollowing(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null){
            List<Follow> listFollow = followRepository.findByFollower(user);
            List<UserDto> userDtoList = new ArrayList<>();
            if(listFollow  != null && !listFollow .isEmpty()){
                for (Follow follow : listFollow ){
                    User follower = userRepository.findById(follow.getFollowingId()).orElse(null);
                    userDtoList.add(modelMapperUtils.mapAllProperties(follower, UserDto.class));
                }
            }
            return userDtoList;
        }
        return null;
    }

    @Override
    public int getListStatusFollowing(String username) {
        User currentUser = baseUtils.getUser();
        User user = userRepository.findByUsername(username);
        if(user != null){
            if(currentUser != null) {
                if (currentUser.getId().equals(user.getId())) {
                    return 2;
                } else if(followRepository.existsByFollowingIdAndFollower(user.getId(), currentUser)) {
                        return 1;
                }
                return 0;
            }
        }
        return 0;
    }
}
