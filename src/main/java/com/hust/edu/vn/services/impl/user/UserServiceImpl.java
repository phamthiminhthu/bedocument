package com.hust.edu.vn.services.impl.user;

import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.model.ChangePasswordModel;
import com.hust.edu.vn.model.UserModel;
import com.hust.edu.vn.repository.FollowRepository;
import com.hust.edu.vn.repository.UserRepository;
import com.hust.edu.vn.services.user.UserService;
import com.hust.edu.vn.utils.AwsS3Utils;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ModelMapperUtils modelMapperUtils;
    private final AwsS3Utils awsS3Utils;
    private final BaseUtils baseUtils;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ModelMapperUtils modelMapperUtils, AwsS3Utils awsS3Utils, BaseUtils baseUtils, PasswordEncoder passwordEncoder,
                           FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.awsS3Utils = awsS3Utils;
        this.baseUtils = baseUtils;
        this.passwordEncoder = passwordEncoder;
        this.followRepository = followRepository;
    }

    @Override
    public UserDto getInfoProfile(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null){
            return modelMapperUtils.mapAllProperties(user, UserDto.class);
        }
        return null;
    }

    @Override
    public boolean updateProfile(UserModel userModel) {
        if(userModel.getEmail() == null || userModel.getUsername() == null) return false;
        User user = baseUtils.getUser();
        if(user != null){
            User updateUser = modelMapperUtils.mapAllProperties(userModel, User.class);
            if(userModel.getAvatar() == null) updateUser.setImage(user.getImage());
            else {
                if(user.getImage() == null) {
                    String url = awsS3Utils.uploadAvatar(userModel.getAvatar(), getRootPath() + "avatar/");
                    updateUser.setImage(url);
                }
                else{
                    awsS3Utils.deleteAvatarFromS3Bucket(user.getImage());
                    String url = awsS3Utils.uploadAvatar(userModel.getAvatar(), getRootPath() + "avatar/");
                    updateUser.setImage(url);
                }
            }
            updateUser.setPassword(user.getPassword());
            updateUser.setId(user.getId());
            updateUser.setRootPath(user.getRootPath());
            updateUser.setUpdatedAt(new Date());
            userRepository.save(updateUser);
            return true;
        }
        return false;

    }

    @Override
    public List<UserDto> findUsersByUsernameOrFullName(String username) {
       User user = baseUtils.getUser();
        if (user != null) {
            List<User> users = userRepository.findByFullnameOrUsernameContainingIgnoreCase(username);
            List<UserDto> result = new ArrayList<>();
            if (users != null && users.size() > 0){
                for(User user1 : users){
                    UserDto userDto = modelMapperUtils.mapAllProperties(user1, UserDto.class);
                    if(user.getUsername().equals(user1.getUsername())){
                        userDto.setFollower((byte) 2);
                    }
                    if(followRepository.existsByFollowingIdAndFollower(user1.getId(), user)){
                        userDto.setFollower((byte) 1);
                    }
                    result.add(userDto);
                }
            }
            return result;
        }
        return null;
    }

    @Override
    public UserDto findByUserByEmail(String email) {
        User user = baseUtils.getUser();
        if (user != null){
            User user1 = userRepository.findByEmail(email);
            if(user1 != null){
                UserDto userDto = modelMapperUtils.mapAllProperties(user1, UserDto.class);
                if(user.getUsername().equals(user1.getUsername())){
                    userDto.setFollower((byte) 2);
                }
                if(followRepository.existsByFollowingIdAndFollower(userDto.getId(), user)){
                    userDto.setFollower((byte) 1);
                }
                return userDto;
            }
        }
        return null;
    }

    @Override
    public UserDto getInformationByToken() {
        User user = baseUtils.getUser();
        if(user!= null){
            return modelMapperUtils.mapAllProperties(user, UserDto.class);
        }
        return null;
    }

    @Override
    public boolean changePassword(ChangePasswordModel changePasswordModel) {
        User user = baseUtils.getUser();
        if (user == null
                || !passwordEncoder.matches(changePasswordModel.getOldPassword(), user.getPassword())
                || !changePasswordModel.getNewPassword().equals(changePasswordModel.getConfirmPassword())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(changePasswordModel.getNewPassword()));
        userRepository.save(user);
        return true;
    }
    public String getRootPath() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email);
        return user.getRootPath();
    }
}
